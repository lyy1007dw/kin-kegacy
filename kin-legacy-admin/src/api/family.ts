import request from '@/utils/request'
import type { Family, FamilyDetail } from '@/types/family'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export const getFamilyList = (page = 1, size = 10) => {
  return request.get<PageResult<Family>>('/admin/family/list/paged', { params: { page, size } })
}

export const getMyFamilyList = () => {
  return request.get<Family[]>('/family/mine')
}

export const getFamilyById = (id: number) => {
  return request.get<FamilyDetail>(`/family/${id}`)
}

export const createFamily = (data: { name: string; description?: string; avatar?: string }) => {
  return request.post('/family/create', data)
}

export const updateFamily = (id: number, data: { name?: string; description?: string; avatar?: string }) => {
  return request.put(`/family/${id}`, data)
}

export const deleteFamily = (id: number) => {
  return request.delete(`/family/${id}`)
}
