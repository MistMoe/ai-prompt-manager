import request from './request'

export interface PromptItem {
  id: number
  userId: number
  title: string
  content: string
  category: string
  modelType: string
  tags: string
  isFavorite: number
  useCount: number
  createTime: string
  updateTime: string
  deleteStatus: number
}

export interface PromptQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  category?: string
  modelType?: string
  favorite?: number
}

export interface PromptForm {
  title: string
  content: string
  category: string
  modelType: string
  tags: string
  isFavorite?: number
}

export function getPromptPageApi(params: PromptQuery) {
  return request.get('/api/prompts/page', { params })
}

export function createPromptApi(data: PromptForm) {
  return request.post('/api/prompts', data)
}

export function updatePromptApi(id: number, data: PromptForm) {
  return request.put(`/api/prompts/${id}`, data)
}

export function deletePromptApi(id: number) {
  return request.delete(`/api/prompts/${id}`)
}

export function favoritePromptApi(id: number, isFavorite: number) {
  return request.put(`/api/prompts/${id}/favorite`, null, {
    params: { isFavorite }
  })
}

export function usePromptApi(id: number) {
  return request.put(`/api/prompts/${id}/use`)
}