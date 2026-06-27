import request from './request'

export interface RegisterParams {
  userName: string
  password: string
  nickName: string
}

export interface LoginParams {
  userName: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  userName: string
  nickName: string
}

export interface UserInfo {
  userId: number
  userName: string
  nickName: string
}

export function registerApi(data: RegisterParams) {
  return request.post('/api/auth/register', data)
}

export function loginApi(data: LoginParams) {
  return request.post<{ code: number; message: string; data: LoginResult }>(
    '/api/auth/login',
    data
  )
}

export function getUserInfoApi() {
  return request.get<{ code: number; message: string; data: UserInfo }>(
    '/api/auth/info'
  )
}