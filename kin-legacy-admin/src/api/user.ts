import request from '@/utils/request'
import type { PageResult } from './admin'

export interface User {
  id: number
  nickname: string
  name: string
  avatar: string
  phone: string
  globalRole: string
  status: string
  createTime: string
}

export const getUserList = (page = 1, size = 10) => {
  return request.get<PageResult<User>>('/admin/user/list/paged', { params: { page, size } })
}

export const updateUser = (id: number, data: { nickname?: string; phone?: string; avatar?: string }) => {
  return request.put(`/admin/user/${id}`, data)
}

export const disableUser = (id: number) => {
  return request.put(`/admin/user/${id}/disable`)
}

export const enableUser = (id: number) => {
  return request.put(`/admin/user/${id}/enable`)
}

export const updateUserName = (name: string) => {
  return request.put('/user/name', { name })
}
