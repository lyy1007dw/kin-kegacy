const BASE_URL = 'http://localhost:8080/api'

/**
 * 上传文件到服务器
 * @param {String} filePath - 文件临时路径
 * @param {String} type - 文件类型 (avatar, common 等)
 * @returns {Promise<Object>} - 上传结果
 */
export const uploadFile = (filePath, type = 'common') => {
  return new Promise((resolve, reject) => {
    const accessToken = uni.getStorageSync('accessToken')

    uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': accessToken ? `Bearer ${accessToken}` : ''
      },
      formData: {
        type: type
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            uni.showToast({
              title: data.message || '上传失败',
              icon: 'none'
            })
            reject(new Error(data.message || '上传失败'))
          }
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
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * 选择并上传图片
 * @param {Object} options - 配置选项
 * @param {Number} options.count - 最多选择数量
 * @param {String} options.sourceType - 图片来源 ['album', 'camera']
 * @param {String} options.type - 上传类型
 * @returns {Promise<Array>} - 上传结果数组
 */
export const chooseAndUploadImage = (options = {}) => {
  const {
    count = 1,
    sourceType = ['album', 'camera'],
    type = 'common'
  } = options

  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: count,
      sourceType: sourceType,
      success: async (res) => {
        const tempFilePaths = res.tempFilePaths

        if (count === 1) {
          // 单文件上传
          try {
            const result = await uploadFile(tempFilePaths[0], type)
            resolve([result])
          } catch (error) {
            reject(error)
          }
        } else {
          // 多文件上传
          const results = []
          for (let i = 0; i < tempFilePaths.length; i++) {
            try {
              const result = await uploadFile(tempFilePaths[i], type)
              results.push(result)
            } catch (error) {
              reject(error)
              return
            }
          }
          resolve(results)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 选择并上传头像
 * @returns {Promise<Object>} - 上传结果
 */
export const uploadAvatar = () => {
  return chooseAndUploadImage({
    count: 1,
    sourceType: ['album', 'camera'],
    type: 'avatar'
  }).then(results => results[0])
}

export default {
  uploadFile,
  chooseAndUploadImage,
  uploadAvatar
}
