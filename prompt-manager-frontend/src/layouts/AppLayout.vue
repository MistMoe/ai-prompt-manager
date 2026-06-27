<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon">AP</div>
        <div>
          <h2>AI Prompt</h2>
          <p>Archive</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <RouterLink to="/dashboard" class="nav-item" active-class="nav-active">
          <span>总览</span>
        </RouterLink>

        <RouterLink to="/prompts" class="nav-item" active-class="nav-active">
          <span>Prompt 资产库</span>
        </RouterLink>

        <RouterLink to="/favorites" class="nav-item" active-class="nav-active">
          <span>收藏夹</span>
        </RouterLink>

        <RouterLink to="/template-preview" class="nav-item" active-class="nav-active">
          <span>模板预览</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <p>Prompt Intelligence Workspace</p>
      </div>
    </aside>

    <main class="app-main">
      <header class="app-header">
        <div>
          <p class="header-eyebrow">Workspace</p>
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="user-box">
          <div class="user-avatar">
            {{ userInitial }}
          </div>

          <div class="user-info">
            <strong>{{ userInfo?.nickName || '用户' }}</strong>
            <span>{{ userInfo?.userName || '未登录' }}</span>
          </div>

          <el-button class="logout-btn" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <section class="app-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const userInitial = computed(() => {
  return userInfo.value?.nickName?.slice(0, 1) || 'A'
})

const pageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/dashboard': '总览',
    '/prompts': 'Prompt 资产库',
    '/favorites': '收藏夹',
    '/template-preview': '模板变量预览'
  }

  return titleMap[route.path] || 'AI Prompt 管理平台'
})

const handleLogout = async () => {
  await ElMessageBox.confirm('确定要退出当前账号吗？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  })

  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  --archive-bg: #f7fbf8;
  --archive-bg-soft: #eef8f4;
  --archive-card: #ffffff;
  --archive-brown: #6f5a52;
  --archive-brown-deep: #5f4a43;
  --archive-brown-soft: #978076;
  --archive-mint: #9fcfc2;
  --archive-mint-light: #cfe9e2;
  --archive-border: #dfe8e5;
  --archive-text: #111827;
  --archive-muted: #7b868c;

  min-height: 100vh;
  display: flex;
  background: var(--archive-bg);
  color: var(--archive-text);
}

/* 左侧骨架 */
.app-sidebar {
  width: 276px;
  min-height: 100vh;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  padding: 34px 22px 28px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 18% 8%, rgba(207, 233, 226, 0.7), transparent 32%),
    linear-gradient(180deg, #f8fcfa 0%, #f1f8f5 100%);
  border-right: 3px solid #d8e3df;
  box-shadow: 1px 0 0 rgba(255, 255, 255, 0.75);
  overflow: hidden;
}

.app-sidebar::after {
  content: "";
  position: absolute;
  width: 240px;
  height: 420px;
  left: -40px;
  bottom: 28px;
  pointer-events: none;
  opacity: 0.24;
  background-image:
    radial-gradient(circle, rgba(151, 128, 118, 0.38) 1.25px, transparent 1.45px),
    radial-gradient(circle, rgba(159, 207, 194, 0.38) 1px, transparent 1.2px);
  background-size: 20px 20px, 34px 34px;
  background-position: 0 0, 10px 12px;
  mask-image: linear-gradient(to top, black 0%, rgba(0, 0, 0, 0.75) 42%, transparent 86%);
}

.sidebar-brand {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 50px auto;
  column-gap: 14px;
  align-items: center;
  margin-bottom: 38px;
}

.brand-icon {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: var(--archive-brown);
  color: #ffffff;
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.04em;
  box-shadow: 0 18px 42px rgba(95, 74, 67, 0.18);
}

.sidebar-brand h2 {
  margin: 0;
  font-size: 23px;
  line-height: 1;
  font-weight: 700;
  letter-spacing: -0.045em;
  color: var(--archive-text);
}

.sidebar-brand p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1;
  letter-spacing: 0.32em;
  text-transform: uppercase;
  color: rgba(123, 104, 97, 0.72);
}

/* 导航 */
.sidebar-nav {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nav-item {
  height: 48px;
  padding: 0 16px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  color: rgba(54, 65, 78, 0.92);
  font-size: 16px;
  font-weight: 500;
  text-decoration: none;
  transition:
    background 0.18s ease,
    color 0.18s ease,
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.nav-item:hover {
  background: rgba(207, 233, 226, 0.58);
  color: var(--archive-brown);
  transform: translateX(2px);
}

.nav-active {
  background: var(--archive-brown);
  color: #ffffff;
  box-shadow: 0 18px 38px rgba(95, 74, 67, 0.2);
}

.nav-active:hover {
  background: var(--archive-brown-deep);
  color: #ffffff;
}

.sidebar-footer {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding: 0 10px;
}

.sidebar-footer p {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(123, 104, 97, 0.58);
}

/* 右侧主区域 */
.app-main {
  flex: 1;
  min-width: 0;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 82% 0%, rgba(207, 233, 226, 0.42), transparent 26%),
    linear-gradient(135deg, #fbfcfc 0%, #f7fbf8 52%, #f3f8f6 100%);
}

.app-main::before {
  content: "";
  position: absolute;
  width: 620px;
  height: 420px;
  right: 42px;
  top: 118px;
  pointer-events: none;
  opacity: 0.2;
  background-image:
    radial-gradient(circle, rgba(151, 128, 118, 0.36) 1.25px, transparent 1.45px),
    radial-gradient(circle, rgba(159, 207, 194, 0.28) 1px, transparent 1.2px);
  background-size: 22px 22px, 36px 36px;
  background-position: 0 0, 11px 12px;
  mask-image: radial-gradient(ellipse at center, black 0%, black 36%, transparent 74%);
}

/* 顶部栏 */
.app-header {
  position: sticky;
  top: 0;
  z-index: 5;
  min-height: 102px;
  padding: 24px 46px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.78) 0%, rgba(248, 252, 250, 0.86) 100%);
  border-bottom: 1px solid var(--archive-border);
  backdrop-filter: blur(14px);
}

.header-eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1;
  letter-spacing: 0.32em;
  text-transform: uppercase;
  color: rgba(123, 104, 97, 0.62);
}

.app-header h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.1;
  font-weight: 650;
  letter-spacing: -0.055em;
  color: var(--archive-brown);
}

/* 用户区 */
.user-box {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  width: 46px;
  height: 46px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  background: var(--archive-brown);
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 16px 34px rgba(95, 74, 67, 0.18);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 78px;
}

.user-info strong {
  font-size: 15px;
  line-height: 1;
  font-weight: 650;
  color: var(--archive-text);
}

.user-info span {
  font-size: 13px;
  line-height: 1;
  color: rgba(123, 104, 97, 0.72);
}

.logout-btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 10px;
  color: var(--archive-brown);
  border-color: rgba(151, 128, 118, 0.28);
  background: rgba(255, 255, 255, 0.72);
}

.logout-btn:hover,
.logout-btn:focus {
  color: #ffffff;
  border-color: var(--archive-brown);
  background: var(--archive-brown);
}

/* 内容区 */
.app-content {
  position: relative;
  z-index: 1;
  padding: 42px 46px 56px;
  box-sizing: border-box;
}

@media (max-width: 980px) {
  .app-shell {
    flex-direction: column;
  }

  .app-sidebar {
    position: relative;
    width: 100%;
    min-height: auto;
    padding: 22px;
  }

  .sidebar-nav {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sidebar-footer {
    display: none;
  }

  .app-header {
    position: relative;
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
    padding: 24px 26px;
  }

  .app-content {
    padding: 28px 22px 42px;
  }
}
</style>