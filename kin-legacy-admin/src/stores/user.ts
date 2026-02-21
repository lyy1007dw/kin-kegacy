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
  token: string
  user: UserInfo
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userId = ref<number>(0)
  const userInfo = ref<UserInfo | null>(null)

  const login = async (username: string, password: string) => {
    // 调用后端登录接口
    const res = await request.post<LoginResponse>('/auth/login', {
      username,
      password
    })
    
    token.value = res.data.token
    userInfo.value = res.data.user
    userId.value = res.data.user.id
    
    localStorage.setItem('token', token.value)
    localStorage.setItem('userId', String(userId.value))
    
    return true
  }

  const logout = () => {
    token.value = ''
    userId.value = 0
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
  }

  const initStore = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserId = localStorage.getItem('userId')
    if (savedToken && savedUserId) {
      token.value = savedToken
      userId.value = Number(savedUserId)
    }
  }

  initStore()

  return {
    token,
    userId,
    userInfo,
    login,
    logout,
    initStore
  }
})
