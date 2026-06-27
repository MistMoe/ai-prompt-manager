# AI Prompt 管理平台 · 后端阶段交接文档

## 1. 项目基本信息

项目名称：AI Prompt 管理平台
课程：JavaEE 企业级开发期末项目
当前阶段：后端基础接口开发完成，准备进入前端 Vue3 页面开发阶段。

项目定位：
本项目是一个用于管理 AI Prompt 资产的 Web 平台，支持用户注册登录、JWT 鉴权、Prompt 新增、查询、搜索、编辑、收藏、使用次数统计、逻辑删除等功能。项目不是普通信息管理系统换皮，而是围绕 AI 使用场景设计的提示词资产库，后续前端会重点展示 Prompt 卡片、分类检索、收藏复用、模板变量预览等功能。

---

## 2. 当前已完成进度

### 2.1 环境与数据库

已完成：

1. Java 环境检查完成，当前 Java 版本为 17.0.19。
2. MySQL 环境检查完成，当前 MySQL 版本为 8.0.11。
3. Navicat 成功连接本地 MySQL。
4. 创建数据库：`prompt_manager`。
5. 创建数据表：

    * `sys_user`
    * `prompt`
6. IntelliJ IDEA 创建 Spring Boot 后端项目成功。
7. Maven 依赖同步问题已解决。
8. Spring Boot 后端启动成功，运行端口为 `8080`。
9. 浏览器访问测试接口成功：`GET /api/test/ping`。

### 2.2 用户模块

已完成：

1. 用户注册接口：`POST /api/auth/register`
2. 用户登录接口：`POST /api/auth/login`
3. 登录成功后返回 JWT token。
4. 当前用户信息接口：`GET /api/auth/info`
5. JWT 权限拦截：

    * 不带 token 访问受保护接口，返回 401。
    * 带正确 token 访问受保护接口，返回当前用户信息。

### 2.3 Prompt 管理模块

已完成：

1. 新增 Prompt：`POST /api/prompts`
2. 分页查询 Prompt：`GET /api/prompts/page`
3. 关键词搜索 Prompt：`GET /api/prompts/page?keyword=xxx`
4. 查看 Prompt 详情：`GET /api/prompts/{id}`
5. 编辑 Prompt：`PUT /api/prompts/{id}`
6. 收藏 / 取消收藏 Prompt：`PUT /api/prompts/{id}/favorite?isFavorite=1`
7. 使用次数增加：`PUT /api/prompts/{id}/use`
8. 删除 Prompt：`DELETE /api/prompts/{id}`
9. 删除采用逻辑删除，数据库字段 `delete_status` 从 `0` 改为 `1`。
10. 删除后分页查询不再返回该数据。

---

## 3. 技术栈与版本

后端技术栈：

* Spring Boot 3.3.5
* Java 17
* Maven
* MyBatis-Plus 3.5.7
* MySQL 8.0+
* Lombok
* JWT：jjwt 0.12.6
* Validation：Jakarta Validation

数据库工具：

* Navicat Premium

接口测试工具：

* Apifox

前端计划技术栈：

* Vue 3
* Vite
* Element Plus
* Vue Router
* Pinia
* Axios

---

## 4. Maven 问题处理记录

开发过程中 Maven 曾经出现依赖下载失败问题，主要报错包括：

* `Could not transfer artifact ...`
* `failed to transfer ... during a previous attempt`
* Spring Boot 4 相关依赖与 Spring Boot 3 父版本不兼容

已采取的解决方式：

1. 将 Spring Boot 版本固定为 `3.3.5`。
2. 将 MyBatis-Plus 版本固定为 `3.5.7`。
3. 将 IDEA Maven 设置为使用捆绑 Maven。
4. 配置 Maven 用户设置文件：

```text
C:\Users\97780\.m2\settings.xml
```

5. settings.xml 中配置阿里云镜像。
6. pom.xml 已替换为干净稳定版本，避免 Spring Boot 4 生成的新依赖名称影响项目。

注意：
后续不要轻易重新生成项目，不要随意把 Spring Boot 改成 4.x。课程要求是 Spring Boot 3.x。

---

## 5. 数据库设计

数据库名称：

```sql
prompt_manager
```

### 5.1 用户表 sys_user

表名：`sys_user`

字段：

| 字段名         | 类型           | 说明          |
| ----------- | ------------ | ----------- |
| id          | bigint       | 用户 ID，主键，自增 |
| user_name   | varchar(32)  | 登录用户名，唯一    |
| password    | varchar(128) | 加密后的密码      |
| nick_name   | varchar(32)  | 昵称          |
| create_time | datetime     | 创建时间        |

说明：

* 注册时会向该表插入用户数据。
* 密码不是明文保存，而是经过 MD5 加盐处理后保存。
* 当前测试用户为：

    * user_name：`admin`
    * password：`123456`
    * nick_name：`管理员`

### 5.2 Prompt 表 prompt

表名：`prompt`

字段：

| 字段名           | 类型           | 说明                          |
| ------------- | ------------ | --------------------------- |
| id            | bigint       | Prompt ID，主键，自增             |
| user_id       | bigint       | 所属用户 ID                     |
| title         | varchar(64)  | Prompt 标题                   |
| content       | text         | Prompt 正文内容，支持 `{{变量}}` 占位符 |
| category      | varchar(32)  | 分类                          |
| model_type    | varchar(32)  | 适用模型                        |
| tags          | varchar(128) | 标签，多个标签用逗号分隔                |
| is_favorite   | tinyint      | 是否收藏，0 否，1 是                |
| use_count     | int          | 使用次数                        |
| create_time   | datetime     | 创建时间                        |
| update_time   | datetime     | 更新时间                        |
| delete_status | tinyint      | 逻辑删除标记，0 正常，1 删除            |

说明：

* 删除接口不是物理删除，而是逻辑删除。
* MyBatis-Plus 通过 `@TableLogic` 处理 `delete_status` 字段。
* 分页查询默认只返回 `delete_status = 0` 的数据。

---

## 6. 后端目录结构

当前后端项目名称：

```text
prompt-manager-backend
```

主要目录结构：

```text
src/main/java/com/example
├── common
│   ├── Result
│   ├── GlobalExceptionHandler
│   └── UserContext
├── config
│   ├── JwtInterceptor
│   ├── WebConfig
│   └── MybatisPlusConfig
├── controller
│   ├── TestController
│   ├── AuthController
│   └── PromptController
├── dto
│   ├── UserRegisterDTO
│   ├── UserLoginDTO
│   ├── PromptCreateDTO
│   └── PromptUpdateDTO
├── entity
│   ├── SysUser
│   └── Prompt
├── mapper
│   ├── SysUserMapper
│   └── PromptMapper
├── service
│   ├── SysUserService
│   └── PromptService
├── service.impl
│   ├── SysUserServiceImpl
│   └── PromptServiceImpl
├── utils
│   └── JwtUtil
├── vo
│   ├── LoginVO
│   └── UserInfoVO
└── PromptManagerBackendApplication
```

说明：

* `controller`：接收前端 / Apifox 请求。
* `service`：业务逻辑接口。
* `service.impl`：业务逻辑实现。
* `mapper`：数据库操作接口。
* `entity`：数据库表对应的实体类。
* `dto`：接收请求参数。
* `vo`：返回给前端的数据对象。
* `common`：统一返回类、异常处理、当前用户上下文。
* `config`：拦截器、跨域、MyBatis-Plus 分页配置。
* `utils`：工具类，目前主要是 JWT 工具类。

---

## 7. 后端核心配置

### 7.1 application.properties

位置：

```text
src/main/resources/application.properties
```

主要配置内容：

```properties
spring.application.name=prompt-manager-backend

server.port=8080

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/prompt_manager?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的MySQL密码

mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

注意：

* `spring.datasource.password` 要填写本机 MySQL 的 root 密码。
* 项目启动端口是 `8080`。

### 7.2 启动类

启动类：

```text
PromptManagerBackendApplication.java
```

关键注解：

```java
@MapperScan("com.example.mapper")
@SpringBootApplication
```

其中：

* `@MapperScan("com.example.mapper")` 用于扫描 MyBatis-Plus 的 Mapper 接口。
* 如果缺少该注解，Mapper 注入可能失败。

---

## 8. 已完成接口清单

### 8.1 测试接口

请求方式：

```text
GET
```

请求地址：

```text
http://localhost:8080/api/test/ping
```

说明：
用于测试后端是否启动成功。

返回示例：

```text
后端启动成功，AI Prompt 管理平台连接正常
```

---

### 8.2 用户注册接口

请求方式：

```text
POST
```

请求地址：

```text
http://localhost:8080/api/auth/register
```

是否需要 token：否

请求体：

```json
{
  "userName": "admin",
  "password": "123456",
  "nickName": "管理员"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

说明：

* 如果用户名已存在，会返回错误信息。
* 注册成功后，用户数据会写入 `sys_user` 表。

---

### 8.3 用户登录接口

请求方式：

```text
POST
```

请求地址：

```text
http://localhost:8080/api/auth/login
```

是否需要 token：否

请求体：

```json
{
  "userName": "admin",
  "password": "123456"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "JWT_TOKEN",
    "userId": 1,
    "userName": "admin",
    "nickName": "管理员"
  }
}
```

说明：

* 登录成功后返回 token。
* 后续所有受保护接口都要在 Headers 中携带 token。

---

### 8.4 获取当前用户信息接口

请求方式：

```text
GET
```

请求地址：

```text
http://localhost:8080/api/auth/info
```

是否需要 token：是

Headers：

```text
Authorization: Bearer 你的token
```

成功返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "userName": "admin",
    "nickName": "管理员"
  }
}
```

不带 token 返回：

```json
{
  "code": 401,
  "message": "未登录或Token为空",
  "data": null
}
```

说明：
该接口用于验证 JWT 权限控制功能。

---

### 8.5 新增 Prompt 接口

请求方式：

```text
POST
```

请求地址：

```text
http://localhost:8080/api/prompts
```

是否需要 token：是

Headers：

```text
Authorization: Bearer 你的token
```

请求体示例：

```json
{
  "title": "深度文章结构化写作 Prompt",
  "content": "你是一名具备社会观察、心理分析和内容策划能力的深度写作者。请围绕主题「{{主题}}」生成一篇适合公众号/小红书长文的文章。要求：1）先提炼一个有冲突感的核心观点；2）文章结构包含：日常场景切入、问题本质分析、案例延展、情绪共鸣、方法建议、结尾升华；3）语言自然、有画面感，避免模板化和AI腔；4）观点要克制，不要使用绝对化判断；5）输出完整标题、开头、正文大纲和一段示范正文。补充背景：{{背景信息}}；目标读者：{{目标读者}}；期望风格：{{风格}}。",
  "category": "写作",
  "modelType": "GPT",
  "tags": "深度写作,公众号,结构化创作,去AI味"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

说明：

* 新增时自动绑定当前登录用户 ID。
* `is_favorite` 默认 0。
* `use_count` 默认 0。
* `delete_status` 默认 0。

---

### 8.6 分页查询 Prompt 接口

请求方式：

```text
GET
```

请求地址：

```text
http://localhost:8080/api/prompts/page?pageNum=1&pageSize=8
```

是否需要 token：是

Headers：

```text
Authorization: Bearer 你的token
```

成功返回结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 0,
    "size": 8,
    "current": 1,
    "pages": 0
  }
}
```

说明：

* 查询结果只返回当前登录用户自己的 Prompt。
* 已逻辑删除的数据不会返回。
* 支持分页，默认 `pageNum=1`，`pageSize=8`。

---

### 8.7 关键词搜索 Prompt 接口

请求方式：

```text
GET
```

请求地址示例：

```text
http://localhost:8080/api/prompts/page?pageNum=1&pageSize=8&keyword=公众号
```

是否需要 token：是

说明：

* `keyword` 会匹配标题、标签、内容。
* 可用于前端搜索框。

---

### 8.8 查看 Prompt 详情接口

请求方式：

```text
GET
```

请求地址：

```text
http://localhost:8080/api/prompts/1
```

是否需要 token：是

说明：

* 只能查看当前用户自己的 Prompt。
* 如果访问不存在或无权限的 Prompt，会返回错误信息。

---

### 8.9 编辑 Prompt 接口

请求方式：

```text
PUT
```

请求地址：

```text
http://localhost:8080/api/prompts/1
```

是否需要 token：是

请求体示例：

```json
{
  "title": "公众号文章去AI味改写 Prompt",
  "content": "请你扮演一名{{角色}}，将下面这段文字改写成{{风格}}风格。要求表达自然、有情绪张力，避免机械化和AI味，保留原文核心观点：{{原文}}",
  "category": "写作",
  "modelType": "GPT",
  "tags": "公众号,改写,去AI味,爆款文案",
  "isFavorite": 0
}
```

成功返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 8.10 收藏 Prompt 接口

请求方式：

```text
PUT
```

请求地址：

```text
http://localhost:8080/api/prompts/1/favorite?isFavorite=1
```

是否需要 token：是

说明：

* `isFavorite=1` 表示收藏。
* `isFavorite=0` 表示取消收藏。

---

### 8.11 使用次数增加接口

请求方式：

```text
PUT
```

请求地址：

```text
http://localhost:8080/api/prompts/1/use
```

是否需要 token：是

说明：

* 每调用一次，`use_count` 自动加 1。
* 后续前端“复制 Prompt”按钮可以调用该接口。

---

### 8.12 删除 Prompt 接口

请求方式：

```text
DELETE
```

请求地址：

```text
http://localhost:8080/api/prompts/1
```

是否需要 token：是

说明：

* 删除采用逻辑删除。
* 数据库中该行不会物理消失，而是 `delete_status` 改成 1。
* 分页查询不会再返回已删除数据。

---

## 9. 当前数据库测试数据状态

当前 `prompt` 表中有 7 条数据：

1. id = 1 的数据是之前测试删除接口时产生的数据，目前：

    * `delete_status = 1`
    * 表示逻辑删除成功。
2. id = 2 至 id = 7 是后续新增的专业 Prompt 测试数据，目前：

    * `delete_status = 0`
    * 可用于前端列表展示。

当前保留的专业测试 Prompt 包括：

1. 深度文章结构化写作 Prompt
2. 电商主图卖点提炼 Prompt
3. 学习笔记转考试复习 Prompt
4. 课程实验报告优化 Prompt
5. 代码报错定位与修复 Prompt
6. 内容风格拆解与复刻 Prompt

这些数据比普通演示数据更适合展示平台价值，后续前端页面可以用它们来展示“Prompt 资产库”的专业感。

---

## 10. 已保存 / 应保存截图清单

建议截图统一放入文件夹：

```text
JavaEE项目截图
```

目前建议保存的截图：

```text
01-数据库表结构-Navicat.png
02-后端项目初始目录结构.png
03-SpringBoot后端启动成功.png
04-浏览器访问测试接口成功.png
05-后端分层目录结构.png
06-Apifox注册接口测试成功.png
07-Navicat用户注册数据入库.png
08-Apifox登录接口返回token.png
09-Apifox未带Token访问被拦截.png
10-Apifox携带Token访问用户信息成功.png
11-Apifox新增Prompt接口成功.png
12-Navicat新增Prompt数据入库.png
13-Apifox分页查询Prompt成功.png
14-Apifox关键词搜索Prompt成功.png
15-Apifox查看Prompt详情成功.png
16-Apifox编辑Prompt接口成功.png
17-Apifox收藏Prompt接口成功.png
18-Apifox使用次数增加接口成功.png
19-Apifox删除Prompt接口成功.png
20-Apifox删除后分页查询无记录.png
21-Navicat-Prompt逻辑删除状态.png
22-Navicat-Prompt专业测试数据准备完成.png
```

说明：

* 不是所有截图都必须放进最终 Word 报告。
* 最终报告中可以挑选重点截图。
* 但开发阶段建议全部保存，方便后面筛选。

---

## 11. 当前后端完成情况对照课程评分

### 基础功能

| 评分项       | 当前完成情况                                                      |
| --------- | ----------------------------------------------------------- |
| 系统主题与功能完整 | 已完成 AI Prompt 管理平台主题，功能完整                                   |
| 项目结构规范    | 已按 controller、service、mapper、entity、dto、vo、config、common 分层 |
| 页面渲染与布局   | 后续前端完成                                                      |
| 注册功能      | 已完成                                                         |
| 登录功能      | 已完成                                                         |
| 数据创建      | Prompt 新增接口已完成                                              |
| 数据查看      | Prompt 分页查询、搜索、详情已完成                                        |
| 数据编辑      | Prompt 编辑接口已完成                                              |
| 数据删除      | Prompt 逻辑删除接口已完成                                            |
| 课程设计报告    | 后续整理 Word 报告                                                |

### 加分项

| 加分项     | 当前完成情况          |
| ------- | --------------- |
| 用户权限控制  | 已完成 JWT 登录拦截    |
| 搜索功能    | 已完成关键词搜索        |
| 分页功能    | 已完成分页查询         |
| 页面美观    | 后续前端完成          |
| 文件上传下载  | 暂未做，可作为后续拓展     |
| 前后端数据校验 | 后端已做基础校验，前端后续补充 |

---

## 12. 后续开发计划

下一阶段进入前端开发。

### 12.1 前端项目初始化

计划创建项目：

```text
prompt-manager-frontend
```

技术栈：

```text
Vue3 + Vite + Element Plus + Vue Router + Pinia + Axios
```

建议项目位置：

```text
F:\JavaEEProject\prompt-manager-frontend
```

### 12.2 前端页面计划

需要开发的页面：

1. 登录页
2. 注册页
3. 首页 / Dashboard
4. Prompt 列表页
5. 新增 Prompt 弹窗
6. 编辑 Prompt 弹窗
7. Prompt 详情 / 预览弹窗
8. 模板变量预览功能
9. 搜索与分页
10. 收藏筛选

### 12.3 前端接口对接顺序

建议顺序：

1. 封装 Axios
2. 实现登录接口
3. 保存 token 到 localStorage
4. 配置请求拦截器，自动携带 Authorization
5. 配置路由守卫，未登录跳转登录页
6. 实现 Prompt 列表页
7. 实现新增 Prompt
8. 实现编辑 Prompt
9. 实现删除 Prompt
10. 实现收藏和使用次数
11. 实现模板变量预览

---

## 13. 后续继续开发时的启动步骤

如果窗口崩了，或者明天继续开发，按下面步骤恢复现场。

### 13.1 启动数据库

一般 MySQL 服务会自动启动。
如果连接失败，打开 Windows 服务，找到 MySQL 或 MySQL80，手动启动。

### 13.2 打开 Navicat

连接：

```text
local_mysql
```

数据库：

```text
prompt_manager
```

确认表：

```text
sys_user
prompt
```

### 13.3 打开 IntelliJ IDEA

打开项目：

```text
F:\JavaEEProject\prompt-manager-backend
```

### 13.4 启动后端

运行：

```text
PromptManagerBackendApplication
```

控制台出现以下内容说明成功：

```text
Tomcat started on port 8080
Started PromptManagerBackendApplication
```

### 13.5 测试后端是否正常

浏览器访问：

```text
http://localhost:8080/api/test/ping
```

正常返回：

```text
后端启动成功，AI Prompt 管理平台连接正常
```

### 13.6 获取 token

Apifox 请求：

```text
POST http://localhost:8080/api/auth/login
```

Body：

```json
{
  "userName": "admin",
  "password": "123456"
}
```

复制返回的 token。

### 13.7 访问受保护接口

Headers 添加：

```text
Authorization: Bearer 你的token
```

示例测试接口：

```text
GET http://localhost:8080/api/prompts/page?pageNum=1&pageSize=8
```

如果返回 Prompt 列表，说明后端恢复成功。

---

## 14. 当前阶段结论

目前 AI Prompt 管理平台后端基础功能已经完成，具备完整的用户体系、权限控制和 Prompt 数据管理能力。系统已经覆盖课程要求中的注册、登录、数据创建、数据查看、数据编辑、数据删除等核心评分点，同时完成了 JWT 权限控制、关键词搜索、分页查询等加分项功能。

下一阶段重点是开发前端页面，将现有后端接口可视化呈现出来，使系统从“接口可用”升级为“页面可操作”。前端完成后，再补充模板变量预览功能，使项目区别于普通 CRUD 管理系统，更像一个真正可复用的 Prompt 资产管理平台。
