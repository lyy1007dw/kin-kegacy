import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { createDiscreteApi } from 'naive-ui'

const { message, dialog } = createDiscreteApi(['message', 'dialog'], {
  configProviderProps: {
    placement: 'top',
    maxWidth: '400px'
  }
})

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let isRefreshing = false
let hasShownAuthError = false

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  async (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      const errorMsg = res.message || '请求失败'
      const errorCode = res.code
      
      if (errorCode === 401 || errorCode === 403) {
        if (!hasShownAuthError) {
          hasShownAuthError = true
          const userStore = useUserStore()
          userStore.logout()
          dialog.error({
            title: errorCode === 401 ? '登录失效' : '权限不足',
            content: errorMsg,
            positiveText: '重新登录',
            maskClosable: false,
            onPositiveClick: () => {
              hasShownAuthError = false
              window.location.href = '/login'
            }
          })
        }
        return Promise.reject(new Error(errorMsg))
      }
      
      if (errorMsg.includes('锁定') || errorMsg.includes('禁用')) {
        dialog.error({
          title: '操作失败',
          content: errorMsg,
          positiveText: '确定',
          maskClosable: false
        })
      } else {
        message.error(errorMsg)
      }
      
      return Promise.reject(new Error(errorMsg))
    }
    return res
  },
  async (error) => {
    const userStore = useUserStore()
    const status = error.response?.status
    const errorMsg = error.response?.data?.message || '网络错误'
    
    if (status === 401 && !hasShownAuthError) {
      hasShownAuthError = true
      userStore.logout()
      dialog.error({
        title: '登录失效',
        content: errorMsg || '登录已过期，请重新登录',
        positiveText: '重新登录',
        maskClosable: false,
        onPositiveClick: () => {
          hasShownAuthError = false
          window.location.href = '/login'
        }
      })
      return Promise.reject(error)
    }
    
    if (status === 403 && !hasShownAuthError) {
      hasShownAuthError = true
      userStore.logout()
      dialog.error({
        title: '权限不足',
        content: errorMsg || '您没有权限访问该功能',
        positiveText: '重新登录',
        maskClosable: false,
        onPositiveClick: () => {
          hasShownAuthError = false
          window.location.href = '/login'
        }
      })
      return Promise.reject(error)
    }
    
    message.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  }
)

export default service
