# AI Prompt 管理平台 · 前端阶段交接文档

## 1. 当前阶段说明

项目名称：AI Prompt 管理平台
当前阶段：前端项目初始化已完成，基础依赖已安装，前端目录结构、路由、基础页面占位、全局样式已经搭好。下一步准备进行登录注册接口对接。

前端项目路径：

```text
F:\JavaEEProject\prompt-manager-frontend
```

后端项目路径：

```text
F:\JavaEEProject\prompt-manager-backend
```

目前后端基础接口已经完成，前端正在开始开发。

---

## 2. 当前已完成内容

### 2.1 前端项目创建完成

已经在 `F:\JavaEEProject` 下创建前端项目：

```text
prompt-manager-frontend
```

创建方式：

```bash
npm create vite@latest prompt-manager-frontend -- --template vue-ts
```

项目类型：

```text
Vue3 + Vite + TypeScript
```

说明：

选择 `vue-ts` 是因为后续可能使用 Inspira UI / Vue Bits 组件，这些组件很多是 TypeScript 写法，项目兼容性更好。

---

### 2.2 前端项目已成功启动过

启动命令：

```bash
npm run dev
```

启动成功后地址为：

```text
http://localhost:5173/
```

终端曾显示：

```text
VITE ready
Local: http://localhost:5173/
```

说明 Vue3 前端项目创建成功。

需要保存的截图：

```text
23-Vue3前端项目启动成功.png
```

---

### 2.3 已安装基础依赖

当前已安装：

```bash
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
```

各依赖用途：

| 依赖                      | 用途                   |
| ----------------------- | -------------------- |
| vue-router@4            | 页面路由跳转               |
| pinia                   | 状态管理，后续保存用户信息和 token |
| axios                   | 请求后端接口               |
| element-plus            | 表单、按钮、分页、弹窗等基础 UI 组件 |
| @element-plus/icons-vue | Element Plus 图标      |

注意：

后续如果老师或自己重新打开项目，需要先在前端项目目录执行：

```bash
npm install
```

然后再运行：

```bash
npm run dev
```

---

## 3. 当前前端目录结构

目前 `src` 目录下已经创建了这些文件夹：

```text
src
├── api
├── assets
├── components
├── layouts
├── router
├── stores
├── styles
└── views
```

各目录用途：

| 目录         | 用途                               |
| ---------- | -------------------------------- |
| api        | 后续封装 axios 请求和接口方法               |
| assets     | 存放图片、金属装饰图等静态资源                  |
| components | 存放通用组件，如 PromptCard、Background 等 |
| layouts    | 存放主布局，如侧边栏 + 顶部栏                 |
| router     | 存放 Vue Router 路由配置               |
| stores     | 存放 Pinia 状态管理                    |
| styles     | 存放全局 CSS 样式                      |
| views      | 存放页面级组件                          |

---

## 4. 已修改 / 新增的关键文件

### 4.1 `src/main.ts`

当前功能：

1. 创建 Vue 应用。
2. 引入 Pinia。
3. 引入 Vue Router。
4. 引入 Element Plus。
5. 引入 Element Plus 样式。
6. 引入全局样式 `global.css`。

当前内容应类似：

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

---

### 4.2 `src/App.vue`

当前功能：

只作为路由出口，显示当前页面。

当前内容：

```vue
<template>
  <RouterView />
</template>
```

---

### 4.3 `src/router/index.ts`

当前已经配置基础路由：

```text
/login              登录页
/register           注册页
/dashboard          总览页
/prompts            Prompt 资产库
/favorites          收藏夹
/template-preview   模板变量预览页
```

当前路由守卫逻辑：

1. `/login` 和 `/register` 是公开页面。
2. 其他页面需要检查 `localStorage` 中是否有 `token`。
3. 没有 token 时自动跳转到 `/login`。

说明：

后续登录成功后，需要把后端返回的 token 保存到：

```ts
localStorage.setItem('token', token)
```

这样访问 `/dashboard`、`/prompts` 等页面才不会被拦截。

---

### 4.4 `src/env.d.ts`

由于 TypeScript 一开始不认识 `.vue` 文件，出现过类似报错：

```text
找不到模块 './App.vue' 或其相应的类型声明。
找不到模块 '../views/LoginView.vue' 或其相应的类型声明。
```

已通过新增 `src/env.d.ts` 解决。

当前内容：

```ts
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<{}, {}, any>
  export default component
}
```

同时已经安装 VS Code 插件：

```text
Vue - Official
```

目前 `.vue` 相关报错已经消失。

---

## 5. 已创建的页面文件

### 5.1 登录页

文件：

```text
src/views/LoginView.vue
```

当前状态：

已经创建基础登录页雏形。

页面内容包括：

```text
平台名称
平台定位文案
用户名输入框
密码输入框
登录按钮
跳转注册入口
```

当前只是静态页面，还没有真正调用后端登录接口。

---

### 5.2 注册页

文件：

```text
src/views/RegisterView.vue
```

当前状态：

已经创建基础注册页雏形。

页面内容包括：

```text
用户名输入框
密码输入框
昵称输入框
注册按钮
返回登录入口
```

当前只是静态页面，还没有真正调用后端注册接口。

---

### 5.3 Dashboard 总览页

文件：

```text
src/views/DashboardView.vue
```

当前状态：

占位页面。

后续计划放：

```text
Prompt 总数
收藏数量
累计使用次数
分类数量
最近更新 Prompt
快捷入口：新建 Prompt / 导入 Prompt / 模板预览
```

---

### 5.4 Prompt 资产库页

文件：

```text
src/views/PromptListView.vue
```

当前状态：

占位页面。

后续计划放：

```text
搜索框
分类筛选
模型筛选
新建 Prompt 按钮
导入 Prompt 按钮
Prompt 卡片网格
分页
```

这是整个前端最核心页面。

---

### 5.5 收藏夹页

文件：

```text
src/views/FavoritesView.vue
```

当前状态：

占位页面。

后续复用 Prompt 资产库样式，只展示收藏数据。

对应后端接口：

```text
GET /api/prompts/page?favorite=1
```

---

### 5.6 模板变量预览页

文件：

```text
src/views/TemplatePreviewView.vue
```

当前状态：

占位页面。

后续计划实现：

```text
选择 Prompt
自动识别 {{变量}}
生成变量输入框
填写变量
实时预览最终 Prompt
一键复制
调用使用次数 +1 接口
```

这是项目区别于普通 CRUD 系统的重要亮点。

---

## 6. 全局样式文件

文件：

```text
src/styles/global.css
```

当前已经写入基础样式，包括：

1. 页面基础字体。
2. body 背景。
3. 登录页布局。
4. 登录 / 注册卡片样式。
5. 占位页面样式。

当前视觉只是第一版雏形，后续会继续调整，不是最终效果。

---

## 7. 当前确定的前端视觉方向

最终风格暂定为：

```text
冷白金属感 AI Prompt 档案馆
```

核心感觉：

```text
冷白背景
局部石墨黑视觉焦点
浅极光氛围
银灰金属装饰
精致卡片边框光效
Prompt 资产库感
```

不做：

```text
普通蓝白后台
大面积毛玻璃
廉价渐变
全黑炫技页面
花哨但影响阅读的动效
```

视觉焦点安排：

| 页面         | 视觉重点                |
| ---------- | ------------------- |
| 登录页 / 注册页  | 浅极光背景 + 登录卡片 + 金属装饰 |
| Dashboard  | 顶部 Hero 区 + 数据卡片    |
| Prompt 资产库 | 卡片网格 + 边框光晕         |
| 模板预览页      | 左右分栏 + 变量填写 + 预览区   |

---

## 8. 已选定 / 候选的视觉组件

### 8.1 Aurora Background 极光背景

来源：

```text
Inspira UI
```

安装命令记录：

```bash
npx shadcn-vue@latest add "https://registry.inspira-ui.com/aurora-background.json"
```

用途：

```text
登录页
注册页
Dashboard 顶部 Hero 区
```

作用：

营造轻量、柔和、低存在感的极光背景，不抢内容。

---

### 8.2 Border Glow 边框光晕卡片

来源：

```text
Vue Bits
```

安装命令记录：

```bash
npx shadcn@latest add https://vue-bits.dev/r/border-glow.json
```

用途：

```text
Prompt 资产卡片
收藏夹卡片
Dashboard 数据卡片
模板变量预览信息卡片
```

设计方向：

```text
白底 / 冷白卡片
浅灰边框
银蓝 / 浅紫灰光晕
光效要克制，不要过亮
```

---

### 8.3 File Upload 文件上传组件

来源：

```text
Inspira UI
```

安装命令记录：

```bash
npx shadcn-vue@latest add "https://registry.inspira-ui.com/file-upload.json"
```

用途：

```text
Prompt 文件导入功能
```

功能设想：

用户上传 `.txt` / `.md` 文件，前端读取文件内容，自动填入新增 Prompt 的内容框。暂时不做后端文件存储，降低开发风险。

流程：

```text
点击导入 Prompt
上传 txt / md 文件
前端读取文本内容
自动填入 Prompt 内容
用户补标题、分类、模型、标签
点击保存
调用 POST /api/prompts 入库
```

---

### 8.4 其他备用组件

备用组件：

```text
Threads 线程背景
Glare Hover 卡片扫光
```

目前暂不优先使用，避免页面动效太多导致风格混乱。

---

## 9. 当前技术路线

前端技术路线：

```text
Vue3
Vite
TypeScript
Vue Router
Pinia
Axios
Element Plus
Inspira UI / Vue Bits 局部视觉组件
自定义 CSS
```

说明：

1. Vue3 + Vite 是主技术路线。
2. Element Plus 用于稳定业务组件，如表单、按钮、弹窗、分页、消息提示。
3. Inspira UI / Vue Bits 用于局部视觉增强，不作为全部页面的主组件库。
4. 后续所有组件代码都会拉到项目本地，老师打开项目可以看到源码。
5. 最终交作业时不用提交 `node_modules`，但必须保留 `package.json` 和 `package-lock.json`。

---

## 10. 明天继续开发时的恢复步骤

### 10.1 打开项目

用 VS Code 打开：

```text
F:\JavaEEProject
```

确认左侧有两个项目：

```text
prompt-manager-backend
prompt-manager-frontend
```

### 10.2 打开前端终端

在 VS Code 终端中进入前端项目：

```bash
cd /d F:\JavaEEProject\prompt-manager-frontend
```

如果已经在 `F:\JavaEEProject`，则输入：

```bash
cd prompt-manager-frontend
```

确认终端路径为：

```text
PS F:\JavaEEProject\prompt-manager-frontend>
```

### 10.3 启动前端

```bash
npm run dev
```

打开：

```text
http://localhost:5173/login
```

查看登录页是否正常。

### 10.4 启动后端

用 IDEA 打开后端：

```text
F:\JavaEEProject\prompt-manager-backend
```

运行：

```text
PromptManagerBackendApplication
```

后端端口：

```text
8080
```

测试后端：

```text
http://localhost:8080/api/test/ping
```

---

## 11. 下一步开发计划

下一步从这里开始：

### 第一步：运行检查

检查这些地址：

```text
http://localhost:5173/login
http://localhost:5173/register
http://localhost:5173/dashboard
```

预期效果：

1. `/login` 显示登录页。
2. `/register` 显示注册页。
3. 未登录访问 `/dashboard` 会自动跳回 `/login`。

如果正常，保存截图：

```text
24-前端基础路由与登录页雏形.png
```

---

### 第二步：封装 axios

计划新建：

```text
src/api/request.ts
```

功能：

1. 配置后端基础地址：

```text
http://localhost:8080
```

2. 请求时自动携带 token：

```text
Authorization: Bearer token
```

3. 后端返回 401 时自动跳转登录页。

---

### 第三步：封装用户接口

计划新建：

```text
src/api/auth.ts
```

包含：

```text
register
login
getUserInfo
```

对应后端接口：

```text
POST /api/auth/register
POST /api/auth/login
GET /api/auth/info
```

---

### 第四步：实现真实登录 / 注册

登录成功后：

```text
保存 token 到 localStorage
保存用户信息
跳转 /dashboard
```

注册成功后：

```text
提示注册成功
跳转 /login
```

---

### 第五步：做主布局

计划新建：

```text
src/layouts/AppLayout.vue
```

包括：

```text
左侧导航栏
顶部用户区
主内容区域
退出登录按钮
```

导航项：

```text
总览
Prompt 资产库
收藏夹
模板预览
```

---

### 第六步：接 Prompt 资产库接口

后续重点页面：

```text
src/views/PromptListView.vue
```

需要对接：

```text
GET /api/prompts/page
POST /api/prompts
PUT /api/prompts/{id}
DELETE /api/prompts/{id}
PUT /api/prompts/{id}/favorite
PUT /api/prompts/{id}/use
```

---

## 12. 当前注意事项

1. 不要删除 `src/env.d.ts`，否则 `.vue` 类型报错可能回来。
2. 不要删除 `package-lock.json`，它记录当前依赖版本。
3. 当前登录页只是静态页面，还不能真正登录。
4. 当前路由守卫已经写好，所以没有 token 时访问 `/dashboard` 会跳回 `/login`，这是正常现象。
5. 之后如果登录成功后跳转失败，优先检查 `localStorage` 里有没有 `token`。
6. 前端开发时要保证后端也在运行，否则接口请求会失败。
7. 视觉组件先不要急着装，等登录注册接口接通后再接 Aurora / Border Glow。

---

## 13. 当前阶段结论

前端项目已经成功创建，并完成了基础工程结构搭建。当前已安装 Vue Router、Pinia、Axios、Element Plus 等核心依赖，完成了路由配置、登录页、注册页、Dashboard、Prompt 资产库、收藏夹和模板变量预览页的基础文件创建。

目前前端还处于“骨架阶段”，页面暂时没有真正接入后端数据。下一步开发重点是封装 Axios 请求、实现登录注册接口联调，并在登录成功后进入 Dashboard。完成登录闭环后，再继续开发主布局、Prompt 卡片列表、文件导入和模板变量预览等核心功能。
