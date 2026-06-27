<template>
  <div class="template-page">
    <section class="template-hero">
  <div>
    <p class="section-eyebrow">Template Preview</p>
    <h2>模板变量预览</h2>
    <p class="section-desc" v-pre>
      自动识别 Prompt 中的 {{变量}}，填写变量后实时生成最终提示词。
    </p>
  </div>

  <el-button class="yellow-btn" type="primary" @click="copyFinalPrompt">复制最终 Prompt</el-button>
</section>

    <section class="template-layout">
      <article class="panel-card">
        <div class="panel-header">
          <div>
            <p class="section-eyebrow">Select Prompt</p>
            <h3>选择你收藏的模板</h3>
          </div>

          <el-button text @click="fetchPromptList">刷新</el-button>
        </div>

        <el-select
          v-model="selectedId"
          placeholder="请选择你收藏的 Prompt 模板"
          filterable
          style="width: 100%"
          @change="handleSelectPrompt"
        >
          <el-option
            v-for="item in promptList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>

        <div v-if="currentPrompt" class="prompt-origin">
          <div class="origin-top">
            <h4>{{ currentPrompt.title }}</h4>
            <span>{{ currentPrompt.category || '未分类' }}</span>
          </div>

          <p>{{ currentPrompt.content }}</p>

          <div class="tag-row">
            <span v-for="tag in splitTags(currentPrompt.tags)" :key="tag">
              # {{ tag }}
            </span>
          </div>
        </div>

        <el-empty v-else description="暂无收藏模板，请先到资产库收藏 Prompt" />
      </article>

      <article class="panel-card">
        <div class="panel-header">
          <div>
            <p class="section-eyebrow">Variables</p>
            <h3>变量填写</h3>
          </div>

          <span class="variable-count">
            {{ variables.length }} 个变量
          </span>
        </div>

        <div v-if="variables.length > 0" class="variable-form">
          <el-form label-position="top">
            <el-form-item
              v-for="variable in variables"
              :key="variable"
              :label="variable"
            >
              <el-input
                v-model="variableValues[variable]"
                :placeholder="`请输入 ${variable}`"
                clearable
              />
            </el-form-item>
          </el-form>
        </div>

        <el-empty v-else description="当前 Prompt 未识别到变量" />
      </article>
    </section>

    <section class="preview-card">
      <div class="panel-header">
        <div>
          <p class="section-eyebrow">Final Prompt</p>
          <h3>最终 Prompt 预览</h3>
        </div>

        <div class="preview-actions">
          <el-button plain @click="resetVariables">清空变量</el-button>
          <el-button type="primary" @click="copyFinalPrompt">复制</el-button>
        </div>
      </div>

      <div class="final-prompt-box">
        {{ finalPrompt || '请选择 Prompt 模板并填写变量，最终内容会在这里实时生成。' }}
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getPromptPageApi,
  type PromptItem,
  usePromptApi
} from '../api/prompt'

const loading = ref(false)
const promptList = ref<PromptItem[]>([])
const selectedId = ref<number | null>(null)
const currentPrompt = ref<PromptItem | null>(null)

const variableValues = reactive<Record<string, string>>({})

const variables = computed(() => {
  if (!currentPrompt.value?.content) return []

  const reg = /\{\{\s*([^{}]+?)\s*\}\}/g
  const result = new Set<string>()
  let match: RegExpExecArray | null

  while ((match = reg.exec(currentPrompt.value.content)) !== null) {
    result.add(match[1].trim())
  }

  return Array.from(result)
})

const finalPrompt = computed(() => {
  if (!currentPrompt.value?.content) return ''

  let content = currentPrompt.value.content

  variables.value.forEach((variable) => {
    const value = variableValues[variable] || `{{${variable}}}`
    const reg = new RegExp(`\\{\\{\\s*${escapeRegExp(variable)}\\s*\\}\\}`, 'g')
    content = content.replace(reg, value)
  })

  return content
})

const escapeRegExp = (str: string) => {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

const fetchPromptList = async () => {
  loading.value = true

  try {
    const res: any = await getPromptPageApi({
     pageNum: 1,
     pageSize: 100,
     favorite: 1
    })

    promptList.value = res.data.records || []

    if (!selectedId.value && promptList.value.length > 0) {
      selectedId.value = promptList.value[0].id
      handleSelectPrompt(selectedId.value)
    }
  } finally {
    loading.value = false
  }
}

const handleSelectPrompt = (id: number) => {
  currentPrompt.value = promptList.value.find((item) => item.id === id) || null
  resetVariables()
}

const resetVariables = () => {
  Object.keys(variableValues).forEach((key) => {
    delete variableValues[key]
  })

  variables.value.forEach((variable) => {
    variableValues[variable] = ''
  })
}

const splitTags = (tags: string) => {
  if (!tags) return []
  return tags
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const copyFinalPrompt = async () => {
  if (!currentPrompt.value) {
    ElMessage.warning('请先选择一个 Prompt 模板')
    return
  }

  await navigator.clipboard.writeText(finalPrompt.value)
  await usePromptApi(currentPrompt.value.id)

  ElMessage.success('最终 Prompt 已复制')
}

onMounted(() => {
  fetchPromptList()
})
</script>

<style scoped>
.template-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.template-hero {
  min-height: 180px;
  padding: 34px;
  border-radius: 30px;
  background:
    radial-gradient(circle at 82% 20%, rgba(255, 210, 225, 0.62), transparent 30%),
    radial-gradient(circle at 20% 18%, rgba(205, 226, 255, 0.72), transparent 32%),
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

.template-hero h2 {
  margin: 0;
  font-size: 42px;
  letter-spacing: -0.07em;
  color: #111827;
}

.section-desc {
  margin: 12px 0 0;
  color: #5f6b7a;
  line-height: 1.8;
}

.template-layout {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.panel-card,
.preview-card {
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

.variable-count {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 13px;
}

.prompt-origin {
  margin-top: 18px;
  padding: 18px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid #eef0f3;
}

.origin-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.origin-top h4 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.origin-top span {
  padding: 5px 10px;
  border-radius: 999px;
  background: #ffffff;
  color: #6b7280;
  font-size: 12px;
}

.prompt-origin p {
  margin: 14px 0 0;
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.tag-row span {
  color: #6b7280;
  font-size: 12px;
}

.variable-form {
  max-height: 460px;
  overflow: auto;
  padding-right: 4px;
}

.preview-card {
  min-height: 280px;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

.final-prompt-box {
  min-height: 180px;
  padding: 22px;
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(248, 250, 252, 0.95), rgba(255, 255, 255, 0.95));
  border: 1px solid #eef0f3;
  color: #111827;
  line-height: 1.9;
  white-space: pre-wrap;
}

.template-hero {
  position: relative !important;
  overflow: hidden !important;
  background-color: #edf7f1 !important;
  background-image:
    radial-gradient(circle, rgba(151, 128, 118, 0.24) 0 3px, transparent 3px),
    radial-gradient(circle, rgba(151, 128, 118, 0.16) 0 3px, transparent 3px) !important;
  background-size: 48px 48px !important;
  background-position:
    0 0,
    24px 24px !important;
  background-repeat: repeat !important;
  border-color: #dfe8e5 !important;
}

.template-hero::before,
.template-hero::after {
  content: none !important;
  display: none !important;
}

.template-hero > * {
  position: relative;
  z-index: 1;
}

/* ===== 薄巧风：模板变量预览页面统一覆盖 ===== */

.template-page {
  color: #111827;
}

.template-page .section-eyebrow,
.template-page .page-eyebrow,
.template-page .panel-eyebrow {
  color: rgba(123, 104, 97, 0.68) !important;
  letter-spacing: 0.32em;
}

.template-page h1,
.template-page h2,
.template-page h3 {
  color: #111827;
  letter-spacing: -0.055em;
}

/* 下面两张主要白卡 */
.template-panel,
.preview-panel,
.variable-panel,
.variables-panel,
.select-panel,
.result-panel,
.template-card,
.variable-card,
.prompt-panel,
.final-panel {
  border: 1px solid rgba(232, 224, 219, 0.95) !important;
  background: rgba(255, 255, 255, 0.94) !important;
  border-radius: 18px !important;
  box-shadow:
    0 20px 54px rgba(116, 101, 94, 0.07),
    0 1px 0 rgba(255, 255, 255, 0.95) inset !important;
}

/* 如果页面里两张卡是布局直接子元素，这段兜底 */
.template-main > section,
.template-grid > section,
.preview-grid > section,
.template-layout > section {
  border: 1px solid rgba(232, 224, 219, 0.95) !important;
  background: rgba(255, 255, 255, 0.94) !important;
  border-radius: 18px !important;
  box-shadow:
    0 20px 54px rgba(116, 101, 94, 0.07),
    0 1px 0 rgba(255, 255, 255, 0.95) inset !important;
}

/* 左侧原始 Prompt 预览内容块 */
.prompt-preview,
.original-prompt,
.template-content,
.prompt-content-box,
.preview-content {
  border: 1px solid rgba(232, 224, 219, 0.82) !important;
  background: rgba(248, 252, 250, 0.86) !important;
  border-radius: 16px !important;
  color: rgba(55, 65, 81, 0.9) !important;
}

/* 变量数量胶囊，比如“4 个变量” */
.variable-count,
.count-badge,
.badge {
  background: rgba(237, 247, 241, 0.92) !important;
  color: rgba(111, 90, 82, 0.78) !important;
  border: 1px solid rgba(232, 224, 219, 0.75) !important;
}

/* 标签 */
.tag-row span,
.tags-row span,
.template-tags span,
.prompt-tags span {
  color: rgba(111, 90, 82, 0.76) !important;
}

/* Element Plus 输入框 / 下拉框 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 10px !important;
  background: rgba(255, 255, 255, 0.86) !important;
  box-shadow:
    0 0 0 1px rgba(232, 224, 219, 0.95) inset,
    0 10px 24px rgba(116, 101, 94, 0.04) !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow:
    0 0 0 1px rgba(190, 170, 160, 0.72) inset,
    0 12px 28px rgba(116, 101, 94, 0.06) !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused),
:deep(.el-textarea__inner:focus) {
  box-shadow:
    0 0 0 1px rgba(111, 90, 82, 0.72) inset,
    0 14px 30px rgba(116, 101, 94, 0.08) !important;
}

/* 表单 label */
:deep(.el-form-item__label) {
  color: rgba(123, 104, 97, 0.86) !important;
}

/* 主按钮：先把“复制最终 Prompt”也改成灰棕 */
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

/* 普通按钮，比如刷新 */
:deep(.el-button.is-plain),
.refresh-btn {
  border-color: rgba(232, 224, 219, 0.95) !important;
  color: #6f5a52 !important;
  background: rgba(255, 255, 255, 0.82) !important;
  border-radius: 9px !important;
}

:deep(.el-button.is-plain:hover),
.refresh-btn:hover {
  border-color: rgba(190, 170, 160, 0.72) !important;
  background: rgba(255, 255, 255, 0.96) !important;
  color: #5f4a43 !important;
}

/* 最终 Prompt 输出区，如果有 */
.final-prompt,
.final-output,
.output-box,
.result-content {
  border: 1px solid rgba(232, 224, 219, 0.82) !important;
  background:
    linear-gradient(135deg, rgba(237, 247, 241, 0.72), rgba(255, 255, 255, 0.94)) !important;
  border-radius: 16px !important;
  color: rgba(55, 65, 81, 0.92) !important;
}

.yellow-btn {
  background: #fff1b8 !important;
  border-color: #fff1b8 !important;
  color: #5f4a43 !important;
  box-shadow: 0 12px 26px rgba(199, 181, 110, 0.2) !important;
}

.yellow-btn:hover,
.yellow-btn:focus {
  background: #f0d772 !important;
  border-color: #e9d171 !important;
  color: #ffffff !important;
}
.variable-count,
.count-badge,
.badge {
  background: #fff1b8 !important;
  color: #5f4a43 !important;
  border: 1px solid rgba(255, 243, 194, 0.72) !important;
}
</style>