<template>
  <section class="shared-page">
    <h2>公共模板</h2>
    <p>浏览用户主动公开的模板，复制到个人资产库后可独立编辑。</p>
    <el-button :loading="loading" @click="load">刷新模板</el-button>
    <div v-loading="loading" class="shared-grid">
      <article v-for="item in items" :key="item.id">
        <h3>{{ item.title }}</h3>
        <small>{{ item.category || '未分类' }} · {{ item.modelType || '通用' }}</small>
        <pre>{{ item.content }}</pre>
        <el-button :loading="copying.has(item.id)" @click="copy(item)">复制到个人库</el-button>
      </article>
    </div>
    <el-empty v-if="!loading && !items.length" description="暂无公共模板，可在个人资产库公开模板" />
    <el-pagination layout="prev, pager, next, total" :total="total" :page-size="12" :current-page="page" @current-change="changePage" />
  </section>
</template>
<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { publicCatalog, copyPublicPrompt, type PublicPrompt } from '../api/workspace'
const items = ref<PublicPrompt[]>([]), total = ref(0), page = ref(1), loading = ref(false)
const copying = reactive(new Set<number>())
async function load() {
  loading.value = true
  try { const response: any = await publicCatalog(page.value); items.value = response.data.records; total.value = response.data.total }
  catch { items.value = [] }
  finally { loading.value = false }
}
function changePage(value: number) { page.value = value; void load() }
async function copy(item: PublicPrompt) {
  if (copying.has(item.id)) return
  copying.add(item.id)
  try { await copyPublicPrompt(item.id); ElMessage.success('已复制到个人资产库') }
  catch { /* The request interceptor shows the server message. */ }
  finally { copying.delete(item.id) }
}
onMounted(load)
</script>
<style scoped>
.shared-page > p { color: #7b868c; }
.shared-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin: 24px 0; }
article { padding: 24px; background: white; border: 1px solid #dfe8e5; border-radius: 20px; }
pre { white-space: pre-wrap; overflow-wrap: anywhere; max-height: 280px; overflow: auto; line-height: 1.8; font-family: inherit; }
small { color: #7b868c; }
</style>
