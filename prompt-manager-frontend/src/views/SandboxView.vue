<template>
  <section class="sandbox-page">
    <h2>模型调试</h2>
    <p>填写模板变量，在多个模型中并发运行同一份 Prompt，比较响应与用量。</p>
    <div class="sandbox-panel">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="个人模板（最近 100 条）">
          <el-select v-model="selectedId" filterable placeholder="选择模板" :disabled="running" @change="reset">
            <el-option v-for="item in prompts" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-for="name in variables" :key="name" :label="name">
          <el-input v-model="values[name]" :disabled="running" type="textarea" :rows="2" maxlength="10000" />
        </el-form-item>
        <el-form-item label="选择模型（最多 3 个）">
          <el-checkbox-group v-model="selectedModels" :max="3" :disabled="running">
            <el-checkbox v-for="model in models" :key="model.id" :value="model.id" :disabled="!model.configured">
              {{ model.label }}{{ model.configured ? '' : '（未配置）' }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <p v-if="!models.some(model => model.configured)">尚未配置模型服务，请完成服务端配置后开始调试。</p>
        <el-button type="primary" :loading="running" :disabled="!selectedId || !selectedModels.length" @click="run">并发运行</el-button>
      </el-form>
      <h3>最终 Prompt 预览</h3>
      <pre>{{ preview || '选择模板后展示预览' }}</pre>
    </div>
    <div class="result-grid">
      <article v-for="result in results" :key="result.modelId" class="sandbox-panel">
        <h3>{{ result.label }}</h3>
        <p>{{ result.elapsedMs }} ms · 输入 {{ result.usage?.promptTokens ?? '未提供' }} / 输出 {{ result.usage?.completionTokens ?? '未提供' }} / 总计 {{ result.usage?.totalTokens ?? '未提供' }} Tokens</p>
        <el-alert v-if="result.error" :title="result.error" type="error" :closable="false" />
        <pre v-if="result.answer">{{ result.answer }}</pre>
      </article>
    </div>
  </section>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPromptPageApi, type PromptItem } from '../api/prompt'
import { sandboxModels, runSandbox, type ModelOption, type RunResult } from '../api/workspace'
import { templateVariables, previewTemplate } from '../utils/templates.mjs'
const prompts = ref<PromptItem[]>([]), models = ref<ModelOption[]>([]), selectedModels = ref<string[]>([])
const selectedId = ref<number | null>(null), values = reactive<Record<string,string>>({})
const results = ref<RunResult[]>([]), running = ref(false)
const current = computed(() => prompts.value.find(prompt => prompt.id === selectedId.value))
const variables = computed(() => templateVariables(current.value?.content || ''))
const preview = computed(() => previewTemplate(current.value?.content || '', values))
function reset() { Object.keys(values).forEach(key => delete values[key]); results.value = [] }
async function run() {
  if (!selectedId.value || running.value) return
  if (variables.value.some(name => !values[name]?.trim())) { ElMessage.warning('请填写全部模板变量'); return }
  running.value = true; results.value = []
  try { const response: any = await runSandbox(selectedId.value, { ...values }, [...selectedModels.value]); results.value = response.data }
  catch { /* The request interceptor shows the server message. */ }
  finally { running.value = false }
}
onMounted(async () => {
  try {
    const [assets, options]: any[] = await Promise.all([getPromptPageApi({ pageNum: 1, pageSize: 100 }), sandboxModels()])
    prompts.value = assets.data.records; models.value = options.data
    selectedModels.value = models.value.filter(model => model.configured).slice(0, 2).map(model => model.id)
  } catch { /* The request interceptor shows the server message. */ }
})
</script>
<style scoped>
.sandbox-page > p, .sandbox-panel > p { color: #7b868c; }
.sandbox-panel { margin-top: 22px; padding: 26px; border: 1px solid #dfe8e5; border-radius: 20px; background: white; }
.result-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; }
pre { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.8; font-family: inherit; max-height: 560px; overflow: auto; }
.el-select { width: 100%; }
</style>
