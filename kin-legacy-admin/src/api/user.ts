import request from '@/utils/request'
import type { PageResult } from './admin'

export interface User {
  id: number
  nickname: string
  avatar: string
  phone: string
  role: string
  createTime: string
}

export const getUserList = (page = 1, size = 10) => {
  return request.get<PageResult<User>>('/user/list/paged', { params: { page, size } })
}

export const updateUser = (id: number, data: { nickname?: string; phone?: string; avatar?: string }) => {
  return request.put(`/user/${id}`, data)
}

export const deleteUser = (id: number) => {
  return request.delete(`/user/${id}`)
}
