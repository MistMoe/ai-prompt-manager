import request from './request'
export interface PublicPrompt { id: number; title: string; content: string; category: string; modelType: string; tags: string }
export interface ModelOption { id: string; label: string; configured: boolean }
export interface RunResult {
  modelId: string; label: string; answer: string; elapsedMs: number; error: string | null
  usage: { promptTokens: number | null; completionTokens: number | null; totalTokens: number | null } | null
}
export const publicCatalog = (page: number) => request.get('/api/public-prompts', { params: { page, size: 12 } })
export const copyPublicPrompt = (id: number) => request.post(`/api/public-prompts/${id}/copy`)
export const publishPrompt = (id: number, published: boolean) => request.put(`/api/public-prompts/${id}/visibility`, null, { params: { published } })
export const sandboxModels = () => request.get('/api/sandbox/models')
export const runSandbox = (promptId: number, variables: Record<string,string>, modelIds: string[]) =>
  request.post('/api/sandbox/run', { promptId, variables, modelIds }, { timeout: 35000 })
