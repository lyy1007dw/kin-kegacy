import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export interface UserInfo {
  id: number
  nickname: string
  avatar: string
  phone: string
  role: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userId = ref<number>(0)
  const userInfo = ref<UserInfo | null>(null)

  const login = async (username: string, password: string) => {
    const res = await request.post<LoginResponse>('/auth/login', {
      username,
      password
    })
    
    token.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    userInfo.value = res.data.userInfo
    userId.value = res.data.userInfo.id
    
    localStorage.setItem('accessToken', token.value)
    localStorage.setItem('refreshToken', refreshToken.value)
    localStorage.setItem('userId', String(userId.value))
    
    return true
  }

  const logout = async () => {
    try {
      await request.post('/auth/logout', {})
    } catch (e) {
      console.error('登出失败', e)
    }
    token.value = ''
    refreshToken.value = ''
    userId.value = 0
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
  }

  const refreshTokenHandler = async () => {
    try {
      const res = await request.post<LoginResponse>('/auth/refresh', {
        refreshToken: refreshToken.value
      })
      token.value = res.data.accessToken
      refreshToken.value = res.data.refreshToken
      localStorage.setItem('accessToken', token.value)
      localStorage.setItem('refreshToken', refreshToken.value)
      return true
    } catch (e) {
      logout()
      return false
    }
  }

  const initStore = () => {
    const savedToken = localStorage.getItem('accessToken')
    const savedRefreshToken = localStorage.getItem('refreshToken')
    const savedUserId = localStorage.getItem('userId')
    if (savedToken && savedUserId) {
      token.value = savedToken
      refreshToken.value = savedRefreshToken || ''
      userId.value = Number(savedUserId)
    }
  }

  initStore()

  return {
    token,
    refreshToken,
    userId,
    userInfo,
    login,
    logout,
    refreshTokenHandler,
    initStore
  }
})
