# AI Prompt 管理平台

AI Prompt 管理平台是一个基于 Spring Boot + Vue 3 的前后端分离课程设计项目，主要用于 Prompt 模板的集中管理、分类检索、收藏复用和变量化生成。系统面向个人 Prompt 资产沉淀场景，支持将常用提示词保存为可复用模板，并通过收藏夹与变量填充功能提升 Prompt 的复用效率。

## 一、项目简介

在日常使用 AI 工具时，用户经常会反复编写相似的 Prompt。该项目围绕“Prompt 资产管理”这一场景，设计并实现了一个可登录、可管理、可收藏、可复用的 Prompt 管理平台。

系统主要包括用户认证、Prompt 资产库、收藏夹、模板变量预览、外部模板导入等功能。用户可以将常用 Prompt 统一保存到系统中，并通过关键词搜索、分类筛选、收藏管理和变量生成等方式快速复用。

## 二、技术栈

### 后端技术

* Spring Boot
* MyBatis-Plus
* MySQL
* Maven
* RESTful API
* Token 登录状态校验

### 前端技术

* Vue 3
* TypeScript
* Vite
* Element Plus
* Vue Router
* Pinia
* Axios

## 三、核心功能

### 1. 用户认证

* 用户注册
* 用户登录
* Token 状态保存
* 前端路由访问控制
* 请求拦截器自动携带 Token

### 2. Prompt 资产库

* 新增 Prompt
* 编辑 Prompt
* 删除 Prompt
* Prompt 列表查看
* 关键词搜索
* 分类筛选
* 模型筛选
* 分页展示

### 3. 收藏复用

* 收藏 Prompt
* 取消收藏
* 收藏夹集中管理
* 常用模板快速复用

### 4. 外部模板导入

* 支持选择本地 txt / md 文件
* 读取文件内容并填充到新建 Prompt 表单
* 便于将外部 Prompt 模板沉淀到系统中

### 5. 模板变量生成

* 从收藏夹中选择 Prompt 模板
* 自动识别 `{{变量}}` 占位符
* 根据变量生成表单
* 填写变量内容后生成最终 Prompt
* 支持复制最终 Prompt
* 支持使用次数统计

## 四、项目结构

```text
ai-prompt-manager
├─ demo-files                  示例模板文件
├─ prompt-manager-backend       后端 Spring Boot 项目
├─ prompt-manager-frontend      前端 Vue 3 项目
├─ .gitignore
└─ README.md
```

## 五、本地运行方式

### 1. 数据库准备

先在 MySQL 中创建项目数据库，并导入项目对应的 SQL 文件。

示例数据库名：

```text
prompt_manager
```

然后检查后端配置文件：

```text
prompt-manager-backend/src/main/resources/application.yml
```

确认数据库连接地址、用户名和密码与本地 MySQL 配置一致。

### 2. 启动后端

使用 IntelliJ IDEA 打开后端项目：

```text
prompt-manager-backend
```

等待 Maven 依赖加载完成后，运行 Spring Boot 启动类。

后端默认访问地址一般为：

```text
http://localhost:8080
```

### 3. 启动前端

进入前端目录：

```bash
cd prompt-manager-frontend
```

安装依赖：

```bash
npm install
```

启动开发服务：

```bash
npm run dev
```

浏览器打开终端显示的地址，一般为：

```text
http://localhost:5173
```

## 六、项目亮点

* 采用前后端分离架构，前端负责页面渲染与交互，后端负责接口服务与业务处理。
* 通过 Token 实现登录状态校验，并结合前端路由守卫限制未登录访问。
* 使用 MyBatis-Plus 简化数据库操作，提高增删改查功能开发效率。
* 支持 Prompt 的搜索、筛选、分页、收藏和复用，功能完整度较高。
* 引入 `{{变量}}` 占位符识别与最终 Prompt 生成功能，使系统不只是普通 CRUD 管理平台，而是具有 Prompt 模板复用价值。
* 支持外部模板文件导入，便于用户将已有 Prompt 文档沉淀为系统资产。

## 七、项目说明

本项目为 JavaEE 企业级开发课程设计项目，主要用于展示 Spring Boot、Vue 3、MySQL 与前后端分离开发的综合应用能力。

项目当前主要面向本地运行与课程设计展示场景。若需要线上访问，可进一步将前端部署到 GitHub Pages、Vercel 等静态站点平台，并将后端与数据库部署到服务器或云平台。
