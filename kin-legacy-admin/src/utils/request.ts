import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'], {
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
let requests: Array<(token: string) => void> = []

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
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      const errorMsg = res.message || '请求失败'
      message.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
    return res
  },
  async (error) => {
    const userStore = useUserStore()
    
    if (error.response?.status === 401) {
      if (!isRefreshing) {
        isRefreshing = true
        const success = await userStore.refreshTokenHandler()
        isRefreshing = false
        
        if (success) {
          requests.forEach(callback => callback(userStore.token))
          requests = []
          return service(error.config)
        } else {
          window.location.href = '/login'
          return Promise.reject(error)
        }
      } else {
        return new Promise((resolve) => {
          requests.push((token: string) => {
            error.config.headers.Authorization = `Bearer ${token}`
            resolve(service(error.config))
          })
        })
      }
    }
    
    let errorMsg = '网络错误'
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.response?.data?.msg) {
      errorMsg = error.response.data.msg
    } else if (error.message) {
      errorMsg = error.message
    }
    message.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  }
)

export default service
