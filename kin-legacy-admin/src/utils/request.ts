import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['X-User-Id'] = userStore.userId
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
      const message = useMessage()
      const errorMsg = res.message || '请求失败'
      message.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
    return res
  },
  (error) => {
    const message = useMessage()
    let errorMsg = '网络错误'
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.message) {
      errorMsg = error.message
    }
    message.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  }
)

export default service
