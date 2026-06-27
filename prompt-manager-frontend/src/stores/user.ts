import { defineStore } from 'pinia'

interface UserInfo {
  userId: number
  userName: string
  nickName: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as UserInfo | null
  }),

  actions: {
    setLoginInfo(token: string, userInfo: UserInfo) {
      this.token = token
      this.userInfo = userInfo

      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    logout() {
      this.token = ''
      this.userInfo = null

      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})