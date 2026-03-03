import request from '@/utils/request'

export interface UploadResult {
  fileId: string
  fileName: string
  fileSize: number
  fileUrl: string
  thumbnailUrl: string
}

export function uploadFile(file: File, type: string = 'common'): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
