# AI Prompt Manager

一个用于管理、收藏和复用 Prompt 模板的前后端分离项目。

项目基于 **Spring Boot + Vue 3** 开发，主要解决日常使用 AI 工具时 Prompt 难以沉淀、难以复用、难以分类管理的问题。用户可以把常用 Prompt 保存为模板，并通过搜索、收藏、变量填充等方式快速生成可直接使用的最终 Prompt。

## Preview

> 当前项目主要用于课程设计与本地演示，线上部署版本后续补充。

## Features

* 用户注册、登录与登录状态校验
* Prompt 模板新增、编辑、删除
* Prompt 列表分页展示
* 关键词搜索、分类筛选、模型筛选
* Prompt 收藏与取消收藏
* 收藏夹模板集中管理
* 本地 txt / md 模板文件导入
* 自动识别 `{{变量}}` 占位符
* 根据变量生成最终 Prompt
* 复制 Prompt 与使用次数统计

## Tech Stack

**Frontend**

* Vue 3
* TypeScript
* Vite
* Element Plus
* Vue Router
* Pinia
* Axios

**Backend**

* Spring Boot
* MyBatis-Plus
* MySQL
* Maven
* RESTful API

## Project Structure

```text
ai-prompt-manager
├─ prompt-manager-backend      # Spring Boot 后端项目
├─ prompt-manager-frontend     # Vue 3 前端项目
├─ demo-files                  # 示例 Prompt 模板文件
├─ .gitignore
└─ README.md
```

## Getting Started

### 1. Clone

```bash
git clone https://github.com/MistMoe/ai-prompt-manager.git
cd ai-prompt-manager
```

### 2. Database

在 MySQL 中创建数据库，并导入项目 SQL 文件。

示例数据库名：

```text
prompt_manager
```

然后修改后端配置文件中的数据库连接信息：

```text
prompt-manager-backend/src/main/resources/application.yml
```

主要检查：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/prompt_manager
    username: root
    password: your_password
```

### 3. Backend

使用 IntelliJ IDEA 打开后端项目：

```text
prompt-manager-backend
```

等待 Maven 依赖加载完成后，运行 Spring Boot 启动类。

默认后端地址：

```text
http://localhost:8080
```

### 4. Frontend

进入前端目录：

```bash
cd prompt-manager-frontend
```

安装依赖：

```bash
npm install
```

启动项目：

```bash
npm run dev
```

浏览器打开终端中显示的地址，通常是：

```text
http://localhost:5173
```

## Main Modules

### Prompt Library

用于管理全部 Prompt 模板，支持新增、编辑、删除、搜索、筛选和分页查看。

### Favorites

用于集中保存高频使用的 Prompt，便于快速查找和复用。

### Template Preview

支持选择收藏模板，自动识别模板中的 `{{变量}}`，填写变量后生成最终 Prompt。

例如：

```text
请根据 {{主题}} 写一段适合 {{平台}} 发布的文案，语气要求 {{风格}}。
```

填写变量后可生成完整 Prompt，并一键复制使用。

### File Import

支持从本地选择 txt / md 文件，将外部 Prompt 内容快速导入到新建模板表单中。

## Notes

这个项目最初来自 JavaEE 企业级开发课程设计，但实现时采用了前后端分离结构，并围绕 Prompt 资产管理场景补充了收藏复用、变量识别和模板生成等功能。

项目当前以本地运行和功能展示为主。如果需要在线访问，可以继续部署前端页面，并将后端服务与 MySQL 数据库部署到云服务器。
