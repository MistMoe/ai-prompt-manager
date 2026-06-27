<template>
  <div class="dashboard-page">
    <section class="dashboard-hero">
      <div>
        <p class="section-eyebrow">Overview</p>
        <h2>Prompt 智能工作台</h2>
        <p class="section-desc">
          汇总你的 Prompt 资产、收藏内容与使用情况，快速进入高频工作流程。
        </p>
      </div>

      <div class="hero-actions">
        <el-button plain @click="goPrompts">进入资产库</el-button>
        <el-button class="yellow-btn" type="primary" @click="goTemplatePreview">模板预览</el-button>
      </div>
    </section>

    <section v-loading="loading" class="stat-grid">
      <article class="stat-card">
        <p>Prompt 总数</p>
        <strong>{{ stats.total }}</strong>
        <span>已沉淀的提示词资产</span>
      </article>

      <article class="stat-card">
        <p>收藏数量</p>
        <strong>{{ stats.favoriteTotal }}</strong>
        <span>高频复用 Prompt</span>
      </article>

      <article class="stat-card">
        <p>累计使用</p>
        <strong>{{ stats.useCount }}</strong>
        <span>复制 / 调用次数统计</span>
      </article>

      <article class="stat-card">
        <p>分类数量</p>
        <strong>{{ stats.categoryCount }}</strong>
        <span>Prompt 主题覆盖范围</span>
      </article>
    </section>

    <section class="dashboard-panels">
      <article class="panel-card">
        <div class="panel-header">
          <div>
            <p class="section-eyebrow">Recent</p>
            <h3>最近更新 Prompt</h3>
          </div>

          <el-button text @click="goPrompts">查看全部</el-button>
        </div>

        <div v-if="recentList.length > 0" class="recent-list">
          <div v-for="item in recentList" :key="item.id" class="recent-item">
            <div>
              <h4>{{ item.title }}</h4>
              <p>{{ item.content }}</p>
            </div>

            <span>{{ item.category || '未分类' }}</span>
          </div>
        </div>

        <el-empty v-else description="暂无 Prompt 数据" />
      </article>

      <article class="panel-card quick-panel">
        <div class="panel-header">
          <div>
            <p class="section-eyebrow">Quick Actions</p>
            <h3>快捷入口</h3>
          </div>
        </div>

        <div class="quick-list">
          <button @click="goPrompts">
            <strong>新建 Prompt</strong>
            <span>沉淀新的提示词模板</span>
          </button>

          <button @click="goFavorites">
            <strong>打开收藏夹</strong>
            <span>复用精选 Prompt</span>
          </button>

          <button @click="goTemplatePreview">
            <strong>模板变量预览</strong>
            <span>填写变量并生成最终 Prompt</span>
          </button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getPromptPageApi, type PromptItem } from '../api/prompt'

const router = useRouter()

const loading = ref(false)
const allPromptList = ref<PromptItem[]>([])
const favoriteTotal = ref(0)

const stats = reactive({
  total: 0,
  favoriteTotal: 0,
  useCount: 0,
  categoryCount: 0
})

const recentList = computed(() => {
  return [...allPromptList.value]
    .sort((a, b) => {
      return new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime()
    })
    .slice(0, 5)
})

const fetchDashboardData = async () => {
  loading.value = true

  try {
    const [allRes, favoriteRes]: any = await Promise.all([
      getPromptPageApi({
        pageNum: 1,
        pageSize: 100
      }),
      getPromptPageApi({
        pageNum: 1,
        pageSize: 1,
        favorite: 1
      })
    ])

    const records: PromptItem[] = allRes.data.records || []

    allPromptList.value = records
    favoriteTotal.value = favoriteRes.data.total || 0

    const categorySet = new Set(
      records
        .map((item) => item.category)
        .filter((category) => category && category.trim())
    )

    stats.total = allRes.data.total || 0
    stats.favoriteTotal = favoriteTotal.value
    stats.useCount = records.reduce((sum, item) => sum + (item.useCount || 0), 0)
    stats.categoryCount = categorySet.size
  } finally {
    loading.value = false
  }
}

const goPrompts = () => {
  router.push('/prompts')
}

const goFavorites = () => {
  router.push('/favorites')
}

const goTemplatePreview = () => {
  router.push('/template-preview')
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-hero {
  min-height: 220px;
  padding: 34px;
  border-radius: 30px;
  background:
    radial-gradient(circle at 80% 18%, rgba(255, 210, 225, 0.62), transparent 30%),
    radial-gradient(circle at 22% 20%, rgba(205, 226, 255, 0.72), transparent 32%),
    linear-gradient(135deg, #ffffff 0%, #f7f8fb 100%);
  border: 1px solid rgba(229, 231, 235, 0.9);
  box-shadow: 0 26px 80px rgba(17, 24, 39, 0.06);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.section-eyebrow {
  margin: 0 0 8px;
  color: #9ca3af;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.dashboard-hero h2 {
  margin: 0;
  max-width: 720px;
  font-size: 46px;
  letter-spacing: -0.07em;
  color: #111827;
}

.section-desc {
  margin: 12px 0 0;
  max-width: 660px;
  color: #5f6b7a;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.stat-card {
  padding: 24px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 60px rgba(17, 24, 39, 0.05);
}

.stat-card p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.stat-card strong {
  display: block;
  margin-top: 16px;
  font-size: 42px;
  line-height: 1;
  letter-spacing: -0.06em;
  color: #111827;
}

.stat-card span {
  display: block;
  margin-top: 12px;
  color: #9ca3af;
  font-size: 13px;
}

.dashboard-panels {
  display: grid;
  grid-template-columns: 1.5fr 0.9fr;
  gap: 18px;
}

.panel-card {
  padding: 24px;
  border-radius: 26px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 60px rgba(17, 24, 39, 0.05);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.panel-header h3 {
  margin: 0;
  font-size: 24px;
  letter-spacing: -0.05em;
  color: #111827;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #eef0f3;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  align-items: center;
}

.recent-item h4 {
  margin: 0;
  font-size: 16px;
  color: #111827;
}

.recent-item p {
  margin: 8px 0 0;
  max-width: 760px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recent-item span {
  padding: 5px 10px;
  border-radius: 999px;
  background: #ffffff;
  color: #6b7280;
  font-size: 12px;
}

.quick-panel {
  min-height: 360px;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-list button {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  background:
    radial-gradient(circle at 92% 12%, rgba(205, 226, 255, 0.45), transparent 28%),
    #ffffff;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.quick-list button:hover {
  transform: translateY(-3px);
  border-color: #c7d2fe;
  box-shadow: 0 20px 50px rgba(17, 24, 39, 0.08);
}

.quick-list strong {
  display: block;
  color: #111827;
  font-size: 16px;
}

.quick-list span {
  display: block;
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.dashboard-hero {
  position: relative !important;
  overflow: hidden !important;
  background-color: #edf7f1 !important;
  background-image:
    radial-gradient(circle, rgba(151, 128, 118, 0.24) 0 3px, transparent 3px),
    radial-gradient(circle, rgba(151, 128, 118, 0.18) 0 3px, transparent 3px) !important;
  background-size: 44px 44px !important;
  background-position:
    0 0,
    22px 22px !important;
  background-repeat: repeat !important;
}

.stat-card,
.recent-panel,
.quick-panel,
.quick-card,
.recent-item {
  border: 1px solid rgba(232, 224, 219, 0.95) !important;
  background: rgba(255, 255, 255, 0.94) !important;
  box-shadow:
    0 20px 54px rgba(116, 101, 94, 0.07),
    0 1px 0 rgba(255, 255, 255, 0.95) inset !important;
}

.stat-card:hover,
.quick-card:hover,
.recent-item:hover {
  border-color: rgba(190, 170, 160, 0.72) !important;
  background: rgba(255, 255, 255, 0.98) !important;
  box-shadow:
    0 24px 62px rgba(116, 101, 94, 0.1),
    0 1px 0 rgba(255, 255, 255, 0.98) inset !important;
}

.stat-card strong,
.stat-card .stat-value,
.stat-number {
  color: #6f5a52 !important;
}

.quick-card h3,
.quick-card strong,
.recent-item h3,
.recent-item strong {
  color: #111827;
}

.quick-card p,
.recent-item p,
.stat-card p {
  color: rgba(123, 104, 97, 0.68) !important;
}

.dashboard-hero {
  border-color: rgba(232, 224, 219, 0.95) !important;
  box-shadow:
    0 22px 60px rgba(116, 101, 94, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.82) inset !important;
}

.yellow-btn {
  background: #fff1b8 !important;
  border-color: #e3d49a !important;
  color: #5f4a43 !important;
  box-shadow: 0 12px 26px rgba(199, 181, 110, 0.2) !important;
}

.yellow-btn:hover,
.yellow-btn:focus {
  background: #f0d772 !important;
  border-color: #e9d171 !important;
  color: #ffffff !important;
}
</style>