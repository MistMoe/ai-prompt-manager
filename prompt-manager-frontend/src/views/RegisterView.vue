<template>
  <div class="auth-page">
    <section class="auth-intro">
      <div class="brand-row">
        <div class="brand-mark">AP</div>
        <div class="brand-copy">
          <p class="brand-title">AI Prompt</p>
          <p class="brand-subtitle">ARCHIVE</p>
        </div>
      </div>

      <p class="eyebrow">AI PROMPT ARCHIVE</p>
      <h1>
        把闪过的灵感，<br />
        存成随手可用的资产。
      </h1>
      <p class="intro-text">
        注册一个账号，开始搭建你自己的提示词库。
      </p>
    </section>

    <section class="auth-card">
      <p class="card-label">CREATE ACCOUNT</p>
      <h2>注册</h2>
      <p class="card-desc">创建你的专属账号</p>

      <el-form label-position="top" @keyup.enter="handleRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>

        <el-button
          class="full-btn"
          type="primary"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>

      <p class="switch-text">
        已有账号？
        <RouterLink to="/login">返回登录</RouterLink>
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi } from '../api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  userName: '',
  password: '',
  nickName: ''
})

const handleRegister = async () => {
  if (!form.userName.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }

  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  if (!form.nickName.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }

  loading.value = true

  try {
    await registerApi({
      userName: form.userName,
      password: form.password,
      nickName: form.nickName
    })

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  --archive-brown: #86695f;
  --archive-brown-deep: #4d3c37;
  --archive-brown-soft: #7b6861;
  --archive-mint: #8fc7b9;
  --archive-mint-light: #b7ded4;
  --archive-border: #d8e3df;

  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 54%) 420px;
  align-items: center;
  justify-content: center;
  column-gap: 88px;
  padding: 48px 9vw;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 14% 18%, rgba(221, 242, 238, 0.7), transparent 28%),
    radial-gradient(circle at 88% 12%, rgba(238, 247, 244, 0.95), transparent 30%),
    linear-gradient(135deg, #fbfcfc 0%, #f7faf9 48%, #f5f8f7 100%);
  color: #111827;
}

.auth-page::before {
  content: "";
  position: absolute;
  width: 620px;
  height: 460px;
  right: 3vw;
  top: 7vh;
  pointer-events: none;
  opacity: 0.58;
  background-image:
    radial-gradient(circle, rgba(132, 221, 199, 0.72) 1.7px, transparent 1.8px),
    radial-gradient(circle, rgba(183, 222, 212, 0.52) 1px, transparent 1.15px);
  background-size: 22px 22px, 36px 36px;
  background-position: 0 0, 9px 11px;
  mask-image: radial-gradient(ellipse at 50% 45%, black 0%, black 32%, transparent 74%);
}

.auth-page::after {
  content: "";
  position: absolute;
  width: 680px;
  height: 390px;
  left: 2vw;
  bottom: -5vh;
  pointer-events: none;
  opacity: 0.42;
  background-image:
    radial-gradient(circle, rgba(1, 195, 146, 0.841) 1.35px, transparent 1.5px),
    radial-gradient(circle, rgba(216, 227, 223, 0.68) 0.95px, transparent 1.15px);
  background-size: 17px 17px, 32px 32px;
  background-position: 0 0, 10px 12px;
  mask-image: linear-gradient(to top, black 0%, rgba(0, 0, 0, 0.78) 28%, transparent 82%);
}

.auth-intro {
  position: relative;
  z-index: 1;
  max-width: 690px;
}

.brand-row {
  display: grid;
  grid-template-columns: 56px 210px;
  column-gap: 18px;
  align-items: start;
  height: 56px;
  margin-bottom: 36px;
}

.brand-mark {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: var(--archive-brown);
  color: #ffffff;
  font-size: 22px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: -0.04em;
  box-shadow: 0 20px 48px rgba(95, 74, 67, 0.18);
}

.brand-copy {
  position: relative;
  height: 56px;
}

.brand-title {
  position: absolute;
  left: 0;
  top: 5px;
  margin: 0;
  font-size: 27px;
  line-height: 1;
  font-weight: 700;
  letter-spacing: -0.05em;
  color: #111827;
}

.brand-subtitle {
  position: absolute;
  left: 0;
  top: 39px;
  margin: 0;
  font-size: 14px;
  line-height: 1;
  letter-spacing: 0.36em;
  color: rgba(123, 104, 97, 0.72);
}

.eyebrow {
  margin: 0 0 24px;
  font-size: 13px;
  line-height: 1;
  letter-spacing: 0.34em;
  color: rgba(123, 104, 97, 0.68);
  text-transform: uppercase;
}

.auth-intro h1 {
  margin: 0;
  max-width: 680px;
  font-size: clamp(38px, 4.2vw, 62px);
  line-height: 1.1;
  font-weight: 300;
  letter-spacing: +0.05em;
  color: var(--archive-brown);
}

.intro-text {
  max-width: 500px;
  margin: 28px 0 0;
  font-size: 18px;
  line-height: 1.85;
  color: rgba(123, 104, 97, 0.82);
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 32px 34px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(216, 227, 223, 0.96);
  box-shadow:
    0 32px 80px rgba(31, 41, 55, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
  backdrop-filter: blur(10px);
}

.card-label {
  margin: 0 0 16px;
  font-size: 12px;
  line-height: 1;
  letter-spacing: 0.32em;
  color: rgba(123, 104, 97, 0.7);
}

.auth-card h2 {
  margin: 0;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 560;
  letter-spacing: -0.055em;
  color: var(--archive-brown);
}

.card-desc {
  margin: 10px 0 26px;
  font-size: 15px;
  color: rgba(123, 104, 97, 0.78);
}

:deep(.el-form-item) {
  margin-bottom: 17px;
}

:deep(.el-form-item__label) {
  padding-bottom: 7px;
  font-size: 13px;
  font-weight: 400;
  color: rgba(123, 104, 97, 0.86);
}

:deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow:
    0 0 0 1px rgba(205, 215, 213, 0.9) inset,
    0 10px 24px rgba(148, 163, 184, 0.06);
  transition:
    background 0.2s ease,
    box-shadow 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.94);
  box-shadow:
    0 0 0 1px rgba(143, 199, 185, 0.78) inset,
    0 12px 28px rgba(148, 163, 184, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow:
    0 0 0 1px rgba(95, 74, 67, 0.72) inset,
    0 14px 30px rgba(100, 116, 139, 0.1);
}

.full-btn {
  width: 100%;
  height: 44px;
  margin-top: 4px;
  border: none;
  border-radius: 10px;
  background: var(--archive-brown);
  --el-button-bg-color: var(--archive-brown);
  --el-button-border-color: var(--archive-brown);
  --el-button-hover-bg-color: var(--archive-brown-deep);
  --el-button-hover-border-color: var(--archive-brown-deep);
  --el-button-active-bg-color: var(--archive-brown-deep);
  --el-button-active-border-color: var(--archive-brown-deep);
  font-weight: 500;
  letter-spacing: 0.12em;
  box-shadow: 0 18px 36px rgba(95, 74, 67, 0.22);
}

.full-btn:hover,
.full-btn:focus {
  background: var(--archive-brown-deep);
}

.switch-text {
  margin: 24px 0 0;
  text-align: center;
  font-size: 14px;
  color: rgba(123, 104, 97, 0.76);
}

.switch-text a {
  margin-left: 4px;
  color: var(--archive-brown);
  font-weight: 600;
  text-decoration: none;
}

.switch-text a:hover {
  text-decoration: underline;
}

@media (max-width: 980px) {
  .auth-page {
    min-height: auto;
    grid-template-columns: 1fr;
    row-gap: 42px;
    padding: 40px 28px;
  }

  .auth-intro {
    max-width: 100%;
  }

  .auth-intro h1 {
    font-size: 38px;
  }

  .auth-card {
    max-width: 420px;
  }
}
</style>