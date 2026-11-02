# Prompt 平台：公共模板与模型调试

本次补齐用户数据隔离、公共模板缓存、Redis Lua 滑动窗口限流、多模型并发调试和模板处理边界。它是按用户隔离的个人工作区，不是组织级多租户系统。

## 升级数据库

已有数据库先备份，再执行一次 `database/2026-11-02-public-prompts.sql`。新增 `is_public`、`share_version` 及查询索引，所有旧模板默认保持私有，不删除或覆盖已有数据。

新安装时先导入原有 `prompt_manager.sql`，再执行上述迁移。不要对已有数据库重新导入完整导出文件，其中含有旧的删表语句。

## 本地启动

需要 Java 17、Maven、MySQL 和 Redis。Redis 缓存故障时公共模板回退数据库；模型调试在限流服务不可用时返回 503，不继续调用收费模型。

如使用 Docker，可启动仅绑定本机端口的独立 Redis：

```powershell
docker run --name prompt-redis -p 127.0.0.1:6379:6379 -d redis:7.4-alpine
```

在 PowerShell 设置环境变量，然后启动后端：

```powershell
cd F:\JavaEEProject
$env:JAVA_HOME = 'D:\db\jdk17.0.19_10'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
# 可选：在已安全配置密钥的环境中启用两个 DeepSeek 模型。
# $env:DEEPSEEK_API_KEY 应由本机环境或安全输入提供，不要写进仓库。
.\scripts\start-backend.ps1 -MavenCommand 'D:\javaweb-troubleshoot-kg-llm\tools\apache-maven-3.9.16\bin\mvn.cmd'
```

启动脚本在没有 `JWT_SECRET` 时生成随机密钥，保存在本地 `.git/prompt-jwt-secret.local`，不会输出或提交。重复启动复用密钥。也可以自行通过 `JWT_SECRET` 提供至少 32 字节的随机密钥，然后直接运行 Maven。删除本地密钥、换密钥或从旧版升级后，需要重新登录。

多实例部署须使用同一个外部 JWT 密钥，不能分别使用各自生成的本地密钥。脚本适用于本项目当前的普通 Git 克隆目录。

Redis 支持 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`；默认连接 `127.0.0.1:6379`。MySQL 继续使用现有 `application.properties` 配置，本次没有连接或迁移实际业务数据库。

前端：

```powershell
cd F:\JavaEEProject\prompt-manager-frontend
npm.cmd install
npm.cmd run dev
```

## 页面与接口

- 个人资产库新增“公开模板 / 取消公开”，公开前确认可分享内容。
- 公共模板页展示已公开内容，可复制到个人库，复制品默认私有且独立编辑。
- 模型调试页从最近 100 条个人模板中选择，按变量生成表单，并发运行最多 3 个模型；分别展示文本、耗时、输入 / 输出 / 总 Token 用量及失败状态。
- 公共模板在本系统中指其他登录用户可见，不是匿名访问。

新增接口均通过现有 JWT 拦截器：

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/public-prompts?page=1&size=12` | 公共模板目录，页大小最多 50 |
| PUT | `/api/public-prompts/{id}/visibility?published=true` | 仅本人可修改公开状态 |
| POST | `/api/public-prompts/{id}/copy` | 将仍处于公开状态的模板复制到个人库 |
| GET | `/api/sandbox/models` | 仅返回模型 ID、名称和是否已配置 |
| POST | `/api/sandbox/run` | 参数为 `promptId`、`variables`、`modelIds` |

## 缓存与用户隔离

私人模板查询和修改均检查所属用户；公开状态修改的 SQL 同样限制用户 ID。调试前读取个人模板时复用同一归属检查，前端不能通过传入其他用户 ID 获得权限。

公共内容使用 `prompt:public:{id}:v{shareVersion}` 缓存 5 分钟。每次读取先从数据库查询公开状态及版本元数据，再读 Redis；只有缓存未命中时读取完整正文。保留这一次元数据查询，是为了避免撤回公开后仍然通过缓存读取内容，而不是宣称请求完全不访问数据库。

正文更新、公开状态修改原子递增版本并清理旧缓存；软删除后元数据查询无法再命中。即使缓存删除失败或旧读请求晚写回，后续请求也无法再通过数据库校验访问旧版本。已经取得公开状态快照的在途请求可能完成读取，无法撤销此前已公开、被复制的内容。

Redis 故障后短暂暂停缓存访问，公共目录继续从数据库读取，避免每条模板反复等待连接超时。收藏、使用次数仅修改自身字段；使用次数在 SQL 中原子递增，不回写旧的公开状态或版本。

## 限流与并发调试

默认每用户 60 秒内最多 10 次模型调用，一次选择 2 个模型消耗 2 个名额。Lua 使用 Redis `TIME`，在一次原子执行中清理过期记录、检查总量、写入本批次请求并设置 TTL；超限整批拒绝，返回 429。可通过 `sandbox.rate-limit`、`sandbox.window-ms` 调整。

调试使用 6 个工作线程、长度 12 的等待队列，最多接纳 6 个调试请求。先提交全部所选模型，再在统一 25 秒预算内收集结果；连接超时 3 秒、读取超时 20 秒。某个模型失败不会丢弃其他模型的成功结果。异常和排队过载也计入已接纳的调用名额；这是一项调用次数控制，不是金额预算系统。

调用前在请求线程完成鉴权与模板渲染，显式把用户 ID 传给异步任务，工作线程设置身份后在 `finally` 清理，避免假设 ThreadLocal 自动跨线程传播。中断会取消待完成任务，但已经发出的外部请求不保证立即停止计费。

模型地址、模型名称和 API 密钥由服务端 `sandbox.models` 配置。默认提供 `deepseek-chat` 与 `deepseek-reasoner` 两个条目，均读取 `DEEPSEEK_API_KEY`；未配置时页面明确禁用，浏览器不会拿到密钥和地址。可增加其他兼容 Chat Completions 协议的服务条目。

Token 统计读取提供方的 `usage`，缺失则展示“未提供”，不按字符数冒充 Token。此版本展示单次运行用量，不存储累计账单，也不估算费用。没有给出并发性能提升或缓存命中率等未经测量的指标。

## 模板处理

前后端均单次替换 `{{变量}}`。变量名含正则字符、变量值含 `$&`、反斜线或其他占位符时保持字面内容，替换结果不会递归解释。后端对变量缺失、数量、变量值长度和最终正文长度进行限制；最终正文最多 20000 字。

TXT / Markdown 导入先严格 UTF-8 解码，失败才按 GB18030 回退。合法 UTF-8 中的 `�` 不触发误判；UTF-16 BOM 文件要求转换，拒绝含 NUL 的二进制内容。文件最多 100 KB，文本最多 20000 字。编码回退不能保证识别所有无 BOM 编码，不宣称“无缝迁移任意文件”。

## 验证

2026-11-02 本地验证：14 项后端测试全部通过（0 failures、0 errors、0 skipped）；前端模板 / 编码测试、TypeScript 检查与生产构建通过；PowerShell 启动脚本语法检查通过。没有执行实际业务数据库迁移，也没有启动真实业务服务。

```powershell
mvn.cmd -f prompt-manager-backend/pom.xml test
cd prompt-manager-frontend
npm.cmd run test:templates
npm.cmd run build
```

后端接口测试使用独立 H2 数据库和临时端口的原生 Redis，不连接用户实际数据库或 Redis。覆盖用户越权、公开 / 撤回、缓存版本、复制隔离、Lua 并发限流、批次计数、过期清理和模型配置隐藏。模型协议测试使用本地 HTTP 服务；并发测试通过两个任务相互等待的屏障验证，未串行冒充并发。

真实 DeepSeek 服务尚未使用用户密钥联调，模型回答质量与真实用量仍需现场验收。生产环境原有的密码存储等问题不属于此次完整安全审计范围，不能将这次改造描述为生产安全认证。

参考：[Redis 限流说明](https://redis.io/docs/latest/develop/use-cases/rate-limiter/)、[Spring Data Redis 模板](https://docs.spring.io/spring-data/redis/reference/redis/template.html)。
