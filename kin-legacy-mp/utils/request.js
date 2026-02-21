const BASE_URL = 'http://localhost:8080/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const userId = uni.getStorageSync('userId')
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'X-User-Id': userId || '',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            clearAuth()
            reject(new Error('未授权，请重新登录'))
          } else {
            const errorMsg = res.data.message || '系统繁忙，请稍后重试'
            uni.showToast({
              title: `业务异常: ${errorMsg}`,
              icon: 'none'
            })
            reject(new Error(errorMsg))
          }
        } else if (res.statusCode === 401) {
          clearAuth()
          reject(new Error('未授权，请重新登录'))
        } else if (res.data && res.data.message) {
          uni.showToast({
            title: `业务异常: ${res.data.message}`,
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

function clearAuth() {
  uni.removeStorageSync('token')
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
