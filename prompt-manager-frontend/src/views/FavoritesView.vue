<template>
  <div class="favorite-page">
    <section class="favorite-header">
      <div>
        <p class="section-eyebrow">Favorite Prompts</p>
        <h2>收藏夹</h2>
        <p class="section-desc">
          集中管理你常用、值得长期复用的高价值 Prompt。
        </p>
      </div>

      <el-button type="primary" plain @click="fetchList">刷新</el-button>
    </section>

    <section v-loading="loading" class="favorite-grid">
      <article v-for="item in favoriteList" :key="item.id" class="favorite-card">
        <div class="card-top">
          <div>
            <h3>{{ item.title }}</h3>
            <div class="meta-row">
              <span>{{ item.category || '未分类' }}</span>
              <span>{{ item.modelType || '通用' }}</span>
              <span>使用 {{ item.useCount || 0 }} 次</span>
            </div>
          </div>

          <button class="favorite-btn active" @click="cancelFavorite(item)">
            已收藏
          </button>
        </div>

        <p class="prompt-content">
          {{ item.content }}
        </p>

        <div class="tag-row">
          <span v-for="tag in splitTags(item.tags)" :key="tag">
            # {{ tag }}
          </span>
        </div>

        <div class="card-actions">
          <el-button size="small" plain @click="handleCopy(item)">复制</el-button>
          <el-button size="small" plain @click="openEdit(item)">编辑</el-button>
          <el-button size="small" plain type="warning" @click="cancelFavorite(item)">
            取消收藏
          </el-button>
        </div>
      </article>

      <el-empty
        v-if="!loading && favoriteList.length === 0"
        description="暂无收藏 Prompt"
      />
    </section>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="[6, 8, 12]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="编辑收藏 Prompt"
      width="620px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入 Prompt 标题" />
        </el-form-item>

        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="7"
            placeholder="请输入 Prompt 内容，可使用 {{变量}} 作为模板占位符"
          />
        </el-form-item>

        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="写作" value="写作" />
            <el-option label="电商" value="电商" />
            <el-option label="学习" value="学习" />
            <el-option label="编程" value="编程" />
            <el-option label="创意" value="创意" />
          </el-select>
        </el-form-item>

        <el-form-item label="模型类型">
          <el-select v-model="form.modelType" placeholder="请选择模型" style="width: 100%">
            <el-option label="GPT" value="GPT" />
            <el-option label="Claude" value="Claude" />
            <el-option label="通用" value="通用" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，例如：公众号,改写,去AI味" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  favoritePromptApi,
  getPromptPageApi,
  type PromptItem,
  updatePromptApi,
  usePromptApi
} from '../api/prompt'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)

const favoriteList = ref<PromptItem[]>([])
const total = ref(0)
const currentId = ref<number | null>(null)

const query = reactive({
  pageNum: 1,
  pageSize: 8
})

const form = reactive({
  title: '',
  content: '',
  category: '',
  modelType: 'GPT',
  tags: ''
})

const fetchList = async () => {
  loading.value = true

  try {
    const res: any = await getPromptPageApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      favorite: 1
    })

    favoriteList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const splitTags = (tags: string) => {
  if (!tags) return []
  return tags
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const openEdit = (item: PromptItem) => {
  currentId.value = item.id

  form.title = item.title
  form.content = item.content
  form.category = item.category
  form.modelType = item.modelType
  form.tags = item.tags

  dialogVisible.value = true
}

const handleSave = async () => {
  if (!currentId.value) return

  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  if (!form.content.trim()) {
    ElMessage.warning('请输入 Prompt 内容')
    return
  }

  saving.value = true

  try {
    await updatePromptApi(currentId.value, {
      title: form.title,
      content: form.content,
      category: form.category,
      modelType: form.modelType,
      tags: form.tags,
      isFavorite: 1
    })

    ElMessage.success('修改成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const cancelFavorite = async (item: PromptItem) => {
  await favoritePromptApi(item.id, 0)
  ElMessage.success('已取消收藏')
  fetchList()
}

const handleCopy = async (item: PromptItem) => {
  await navigator.clipboard.writeText(item.content)
  await usePromptApi(item.id)

  ElMessage.success('已复制 Prompt 内容')
  fetchList()
}

const handlePageChange = (page: number) => {
  query.pageNum = page
  fetchList()
}

const handleSizeChange = (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.favorite-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.favorite-header {
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

.favorite-header h2 {
  margin: 0;
  font-size: 38px;
  letter-spacing: -0.06em;
  color: #111827;
}

.section-desc {
  margin: 10px 0 0;
  color: #6b7280;
}

.favorite-grid {
  min-height: 360px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.favorite-card {
  min-height: 260px;
  padding: 22px;
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 60px rgba(17, 24, 39, 0.05);
  display: flex;
  flex-direction: column;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.favorite-card:hover {
  transform: translateY(-4px);
  border-color: #c7d2fe;
  box-shadow: 0 24px 70px rgba(17, 24, 39, 0.1);
}

.card-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.favorite-card h3 {
  margin: 0;
  font-size: 19px;
  line-height: 1.35;
  letter-spacing: -0.04em;
  color: #111827;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.meta-row span {
  padding: 4px 8px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
}

.favorite-btn {
  flex-shrink: 0;
  height: 30px;
  padding: 0 10px;
  border: 1px solid #111827;
  border-radius: 999px;
  background: #111827;
  color: #ffffff;
  cursor: pointer;
}

.prompt-content {
  margin: 18px 0 0;
  color: #4b5563;
  line-height: 1.8;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
  padding-top: 18px;
}

.tag-row span {
  color: #6b7280;
  font-size: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}

/* ===== 薄巧风：收藏夹页面统一覆盖 ===== */

.favorites-page,
.favorite-page {
  color: #111827;
}

.favorites-page .page-eyebrow,
.favorite-page .page-eyebrow,
.favorites-page .section-eyebrow,
.favorite-page .section-eyebrow {
  color: rgba(123, 104, 97, 0.68) !important;
  letter-spacing: 0.32em;
}

.favorites-page h1,
.favorite-page h1,
.favorites-page h2,
.favorite-page h2 {
  color: #6f5a52 !important;
  font-weight: 650;
  letter-spacing: -0.055em;
}

/* 收藏夹顶部标题区域不要套大白框 */
.favorites-toolbar,
.favorite-toolbar,
.page-header,
.favorite-header {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

/* 收藏卡片：精选感白卡 */
.favorite-card,
.favorites-card,
.collection-card,
.prompt-card {
  border: 1px solid rgba(232, 224, 219, 0.95) !important;
  background: rgba(255, 255, 255, 0.94) !important;
  border-radius: 18px !important;
  box-shadow:
    0 20px 54px rgba(116, 101, 94, 0.07),
    0 1px 0 rgba(255, 255, 255, 0.95) inset !important;
}

.favorite-card:hover,
.favorites-card:hover,
.collection-card:hover,
.prompt-card:hover {
  transform: translateY(-3px);
  border-color: rgba(190, 170, 160, 0.72) !important;
  background: rgba(255, 255, 255, 0.98) !important;
  box-shadow:
    0 26px 68px rgba(116, 101, 94, 0.11),
    0 1px 0 rgba(255, 255, 255, 0.98) inset !important;
}

/* 卡片标题 */
.favorite-card h3,
.favorites-card h3,
.collection-card h3,
.prompt-card h3 {
  color: #111827 !important;
  font-weight: 650;
}

/* 分类、模型、使用次数小胶囊 */
.meta-row span,
.card-meta span,
.favorite-meta span {
  background: rgba(237, 247, 241, 0.9) !important;
  color: rgba(111, 90, 82, 0.78) !important;
  border: 1px solid rgba(232, 224, 219, 0.72);
}

/* 正文内容 */
.prompt-content,
.favorite-content,
.card-content {
  color: rgba(55, 65, 81, 0.86) !important;
}

/* 标签 */
.tag-row span,
.tags-row span,
.favorite-tags span {
  color: rgba(111, 90, 82, 0.76) !important;
}

/* Element Plus 输入框/选择框，如果收藏夹里有搜索筛选也一起统一 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 10px !important;
  background: rgba(255, 255, 255, 0.82) !important;
  box-shadow:
    0 0 0 1px rgba(232, 224, 219, 0.95) inset,
    0 10px 24px rgba(116, 101, 94, 0.04) !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow:
    0 0 0 1px rgba(190, 170, 160, 0.72) inset,
    0 12px 28px rgba(116, 101, 94, 0.06) !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused) {
  box-shadow:
    0 0 0 1px rgba(111, 90, 82, 0.72) inset,
    0 14px 30px rgba(116, 101, 94, 0.08) !important;
}

/* 主按钮改灰棕 */
:deep(.el-button--primary) {
  background: #6f5a52 !important;
  border-color: #6f5a52 !important;
  color: #ffffff !important;
  border-radius: 10px !important;
  box-shadow: 0 14px 30px rgba(111, 90, 82, 0.18);
}

:deep(.el-button--primary:hover),
:deep(.el-button--primary:focus) {
  background: #5f4a43 !important;
  border-color: #5f4a43 !important;
}

/* 普通按钮 */
:deep(.el-button.is-plain) {
  border-color: rgba(232, 224, 219, 0.95) !important;
  color: #6f5a52 !important;
  background: rgba(255, 255, 255, 0.82) !important;
  border-radius: 9px !important;
}

:deep(.el-button.is-plain:hover) {
  border-color: rgba(190, 170, 160, 0.72) !important;
  background: rgba(255, 255, 255, 0.96) !important;
  color: #5f4a43 !important;
}

/* 取消收藏按钮：比删除温和一点 */
.unfavorite-btn,
.cancel-favorite-btn,
.favorite-btn.active {
  background: #6f5a52 !important;
  border-color: #6f5a52 !important;
  color: #ffffff !important;
  border-radius: 999px !important;
}

.favorite-btn {
  border: 1px solid rgba(232, 224, 219, 0.95) !important;
  background: rgba(255, 255, 255, 0.82) !important;
  color: #6f5a52 !important;
  border-radius: 999px !important;
}

.favorite-btn:hover {
  border-color: rgba(190, 170, 160, 0.72) !important;
  background: rgba(237, 247, 241, 0.88) !important;
}

/* 空状态也顺一下 */
:deep(.el-empty__description p) {
  color: rgba(123, 104, 97, 0.68) !important;
}

/* 收藏夹：已收藏小按钮改成奶油黄 */
.favorite-btn.active,
button.favorite-btn.active,
.prompt-card .favorite-btn.active {
  background: #fff1b8 !important;
  border-color: #fff1b8 !important;
  color: #5f4a43 !important;
  box-shadow: 0 8px 18px rgba(199, 181, 110, 0.18) !important;
}

.favorite-btn.active:hover,
button.favorite-btn.active:hover,
.prompt-card .favorite-btn.active:hover {
  background: #f0d772 !important;
  border-color: #e9d171 !important;
  color: #ffffff !important;
}
</style>