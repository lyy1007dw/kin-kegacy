const BASE_URL = 'http://localhost:8080/api'

let isRefreshing = false
let requests: Array<() => void> = []

const request = (options) => {
  return new Promise((resolve, reject) => {
    const accessToken = uni.getStorageSync('accessToken')
    const refreshToken = uni.getStorageSync('refreshToken')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': accessToken ? `Bearer ${accessToken}` : '',
        ...options.header
      },
      success: async (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            if (!isRefreshing && refreshToken) {
              isRefreshing = true
              try {
                const refreshRes = await refreshTokenApi(refreshToken)
                if (refreshRes) {
                  requests.forEach(callback => callback())
                  requests = []
                  const newToken = uni.getStorageSync('accessToken')
                  options.header['Authorization'] = `Bearer ${newToken}`
                  uni.request({
                    ...options,
                    header: options.header,
                    success: (res) => resolve(res.data.data),
                    fail: reject
                  })
                  isRefreshing = false
                  return
                }
              } catch (e) {
                isRefreshing = false
              }
            }
            clearAuth()
            reject(new Error('未授权，请重新登录'))
          } else {
            const errorMsg = res.data.message || '系统繁忙，请稍后重试'
            uni.showToast({
              title: errorMsg,
              icon: 'none'
            })
            reject(new Error(errorMsg))
          }
        } else if (res.statusCode === 401) {
          if (!isRefreshing && refreshToken) {
            isRefreshing = true
            try {
              const refreshRes = await refreshTokenApi(refreshToken)
              if (refreshRes) {
                requests.forEach(callback => callback())
                requests = []
                const newToken = uni.getStorageSync('accessToken')
                options.header['Authorization'] = `Bearer ${newToken}`
                uni.request({
                  ...options,
                  header: options.header,
                  success: (res) => resolve(res.data.data),
                  fail: reject
                })
                isRefreshing = false
                return
              }
            } catch (e) {
              isRefreshing = false
            }
          }
          clearAuth()
          reject(new Error('未授权，请重新登录'))
        } else if (res.data && res.data.message) {
          uni.showToast({
            title: res.data.message,
            icon: 'none'
          })
          reject(new Error(res.data.message))
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

const refreshTokenApi = (refreshToken) => {
  return new Promise((resolve) => {
    uni.request({
      url: BASE_URL + '/auth/refresh',
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: { refreshToken },
      success: (res) => {
        if (res.data.code === 200) {
          uni.setStorageSync('accessToken', res.data.data.accessToken)
          uni.setStorageSync('refreshToken', res.data.data.refreshToken)
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

function clearAuth() {
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userId')
  uni.removeStorageSync('userInfo')
  uni.reLaunch({
    url: '/pages/login/login'
  })
}

export const get = (url, data) => {
  return request({ url, method: 'GET', data })
}

export const post = (url, data) => {
  return request({ url, method: 'POST', data })
}

export const put = (url, data) => {
  return request({ url, method: 'PUT', data })
}

export const del = (url, data) => {
  return request({ url, method: 'DELETE', data })
}

export default {
  get,
  post,
  put,
  del,
  request
}
