# AI Prompt Manager

基于 **Java 17、Spring Boot 3、Vue 3 与 TypeScript** 的 Prompt 资产管理与模型调试平台。将常用提示词保存为可检索、可收藏、可分享的模板，填写变量后，在多个模型中运行同一份 Prompt 并比较结果。

项目由 JavaEE 课程设计扩展而来，当前以本地运行和个人作品集展示为主。

## 核心能力

| 模块 | 已实现能力 |
| --- | --- |
| 用户与个人工作区 | 注册、登录、JWT 鉴权；按用户查询模板，读写时检查资源归属；请求结束后清理 ThreadLocal 上下文 |
| Prompt 资产库 | 新增、编辑、软删除、分页、关键词搜索、分类与模型筛选、收藏和使用次数统计 |
| 动态模板 | 识别 `{{变量}}`，生成表单和响应式预览；变量值按字面替换，不递归解释其中的新占位符 |
| 文件导入 | 导入 TXT、MD、Markdown 文件；严格 UTF-8 解码，失败后尝试 GB18030；限制文件与文本长度 |
| 公共模板 | 用户主动公开模板，其他登录用户可浏览并复制到个人库；复制品默认私有，可独立修改 |
| Redis 缓存 | 缓存公共模板正文；读取前检查数据库中的公开状态与版本，更新或撤回后使旧版本失效 |
| 模型调试 | 最多并发调用 3 个模型，分别展示回答、耗时、输入 / 输出 / 总 Token 用量及失败状态 |
| 调用限流 | Redis Lua 原子执行滑动窗口限流；按实际选择的模型数量扣除名额，超限整批拒绝 |

模板示例：

```text
请根据 {{主题}} 写一段适合 {{平台}} 发布的文案，语气要求 {{风格}}。
```

## 技术栈与架构

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3、TypeScript、Vite 8、Element Plus、Vue Router、Pinia、Axios |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Web / Validation、MyBatis-Plus 3.5.7、JWT |
| 存储 | MySQL 业务数据、Redis 公共正文缓存与滑动窗口记录 |
| 模型调用 | 服务端配置的 Chat Completions 兼容接口、受限线程池并发执行 |
| 验证 | JUnit、MockMvc、H2、临时原生 Redis、本地 HTTP 模拟服务 |

```text
Vue 3 + TypeScript
    │ Axios + Bearer Token
    ▼
Spring Boot
    ├─ JWT 拦截器 → 用户上下文 → 资源归属校验
    ├─ MyBatis-Plus → MySQL：用户、个人模板、公开状态与版本
    ├─ Redis：公共模板正文缓存、Lua 滑动窗口限流
    └─ 有界线程池 → 多个模型接口 → 各模型响应与实际用量
```

模型密钥和接口地址保留在服务端，前端只提交模板 ID、变量和已配置的模型 ID。

## 快速启动

以下命令以仓库根目录为工作目录，Windows 使用 PowerShell。

### 1. 准备环境与代码

- JDK 17、MySQL 8.x、Redis。
- Node.js 满足 `^20.19.0 || >=22.12.0`，这是当前 Vite 8 的运行要求。
- Maven，或项目自带 Maven Wrapper；Wrapper 首次运行需要下载 Maven。

```powershell
git clone https://github.com/MistMoe/ai-prompt-manager.git
cd ai-prompt-manager
```

### 2. 初始化或升级数据库

**全新安装**：从仓库根目录进入 MySQL 客户端，执行：

```sql
CREATE DATABASE IF NOT EXISTS prompt_manager DEFAULT CHARACTER SET utf8mb4;
USE prompt_manager;
SOURCE prompt_manager.sql;
SOURCE database/2026-11-02-public-prompts.sql;
```

**已有数据库**：保留已有数据，在 `prompt_manager` 中仅执行一次迁移：

```sql
USE prompt_manager;
SOURCE database/2026-11-02-public-prompts.sql;
```

迁移新增公开状态、缓存版本和查询索引，旧模板默认私有。`prompt_manager.sql` 含删表语句，已有数据库不要重新导入完整文件；迁移也不要重复执行。

实际配置文件是 [application.properties](prompt-manager-backend/src/main/resources/application.properties)。检查 `spring.datasource.url`、`username`、`password`，也可以通过 Spring Boot 环境变量覆盖，例如 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`。真实凭据不要提交到 Git。

### 3. 配置 Redis 与模型

Redis 默认连接 `127.0.0.1:6379`，可通过 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 覆盖。已有 Redis 实例可直接使用；安装了 Docker 时，也可以启动本机实例：

```powershell
docker run --name prompt-redis -p 127.0.0.1:6379:6379 -d redis:7.4-alpine
```

模型调试默认提供 `deepseek-chat` 与 `deepseek-reasoner` 两个条目，读取 `DEEPSEEK_API_KEY`。在启动后端的同一进程环境中提供密钥；未提供时仍可使用资产管理功能，模型选项会显示“未配置”。

Redis 不可用时，公共模板回退数据库；模型调试返回 503，避免在限流失效时继续调用收费接口。

### 4. 启动后端

确认 Java 17 已加入环境。推荐从仓库根目录使用启动脚本和自带 Wrapper：

```powershell
.\scripts\start-backend.ps1 -MavenCommand (Join-Path $PWD 'prompt-manager-backend/mvnw.cmd')
```

如果 `mvn.cmd` 已在 PATH 中，也可以直接运行：

```powershell
.\scripts\start-backend.ps1
```

脚本在没有 `JWT_SECRET` 时生成随机签名密钥，保存在本地 `.git/prompt-jwt-secret.local`，重复启动复用，不输出或上传。**直接用 IDEA 或 Maven 启动时，必须自行提供至少 32 字节的随机 `JWT_SECRET`。** 更换密钥后需要重新登录，多实例应配置相同的外部密钥。

默认后端地址为 `http://localhost:8080`，可用 [连通性接口](http://localhost:8080/api/test/ping) 检查服务启动情况；该接口不代表 MySQL、Redis 和模型均已连通。

### 5. 启动前端

新开终端，从仓库根目录执行：

```powershell
cd prompt-manager-frontend
npm.cmd ci
npm.cmd run dev
```

访问终端显示的地址，通常为 [登录页](http://localhost:5173/login)，可注册自己的账号。当前 Axios 基地址是 `http://localhost:8080`；部署到其他地址时，需要同步修改 [request.ts](prompt-manager-frontend/src/api/request.ts)。

## 页面与使用流程

| 页面 | 路径 | 用途 |
| --- | --- | --- |
| 总览 | `/dashboard` | 查看个人模板、收藏、使用次数及最近更新 |
| Prompt 资产库 | `/prompts` | 创建、导入、检索、收藏模板，控制是否公开 |
| 收藏夹 | `/favorites` | 集中复用已收藏模板 |
| 模板预览 | `/template-preview` | 选择收藏模板，填写变量并复制最终 Prompt |
| 公共模板 | `/public-prompts` | 浏览其他用户主动公开的模板，复制到个人库 |
| 模型调试 | `/sandbox` | 选择个人模板和模型，比较同一份 Prompt 的运行结果 |

建议演示顺序：注册并登录 → 导入模板 → 填写变量 → 公开模板 → 换账号复制 → 配置密钥后并发调试。再分别验证他人私有模板不可访问、撤回公开后不可复制、超限返回 429。

## 关键设计与边界

- **用户隔离**：以用户 ID 和资源归属校验隔离个人模板；当前没有组织、成员或租户模型。
- **缓存撤回**：每次先查数据库中的公开状态与版本，再按版本读取 Redis 正文。保留一次元数据查询，防止旧缓存绕过撤回状态。
- **限流口径**：默认每用户 60 秒内最多 10 次模型调用；选择 2 个模型消耗 2 个名额。它限制调用次数，不限制金额，也不是 Token 预算。
- **并发与用量**：先提交全部所选模型，再在统一 25 秒预算内收集结果；某个模型失败不会丢弃其他结果。Token 读取提供方 `usage`，缺失显示“未提供”，不估算累计账单。
- **模板处理**：变量按字面单次替换，支持 `constructor`、`__proto__` 等名称；文件最多 100 KB，正文最多 20000 字。编码回退不能保证识别所有无 BOM 文件。

完整接口、缓存一致性、时间预算和配置细节见 [实现与验收说明](docs/prompt-sandbox-implementation.md)。

## 测试与构建

从仓库根目录执行后端测试：

```powershell
.\prompt-manager-backend\mvnw.cmd -f prompt-manager-backend/pom.xml test
```

前端检查与构建：

```powershell
cd prompt-manager-frontend
npm.cmd run test:templates
npm.cmd run build
```

**2026-11-02 验证结果**：14 项后端测试全部通过；前端模板 / 编码回归、TypeScript 检查和生产构建通过；启动脚本语法检查通过。

后端测试使用独立 H2 和临时端口的原生 Redis，覆盖越权、缓存版本、公开 / 撤回、复制隔离和 Lua 并发限流；模型协议使用本地 HTTP 模拟服务验证，不连接实际业务数据库。

## 当前限制与后续方向

真实模型尚未使用用户密钥联调；回答质量、真实调用耗时及用量需要另外验收。当前以本地演示为主，模型用量只展示单次结果，调试页加载最近 100 条个人模板。

后续可补充部署编排、批量导入导出和历史用量记录。生产部署还需处理现有密码存储等安全问题，不能把当前测试结果当作生产安全认证。

## 项目结构

```text
ai-prompt-manager/
├─ prompt-manager-backend/    # 认证、个人资产、公共缓存、限流、模型调试
├─ prompt-manager-frontend/   # Vue 页面、变量处理与编码导入
├─ prompt_manager.sql         # 新安装使用的原始数据导出，含删表语句
├─ database/                  # 数据库升级脚本
├─ scripts/                   # 本地后端启动与 JWT 密钥生成
└─ docs/                      # 实现、配置与验收细节
```
