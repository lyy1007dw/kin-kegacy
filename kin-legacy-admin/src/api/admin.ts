import request from '@/utils/request'
import type { Family } from './family'

export interface User {
  id: number
  nickname: string
  avatar: string
  phone: string
  role: string
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export const getUserList = (page = 1, size = 10) => {
  return request.get<PageResult<User>>('/admin/user/list/paged', { params: { page, size } })
}

export const getNonAdminUsers = () => {
  return request.get<User[]>('/admin/user/list/non-admin')
}

export const getFamilyList = (page = 1, size = 10) => {
  return request.get<PageResult<Family>>('/admin/family/list/paged', { params: { page, size } })
}

export interface Approval {
  id: number
  type: string
  familyId: number
  applicantUserId: number
  applicantName: string
  relationDesc?: string
  memberId?: number
  fieldName?: string
  oldValue?: string
  newValue?: string
  status: string
  createTime: string
}

export const getApprovalList = (params: { page?: number; size?: number; type?: string; status?: string }) => {
  return request.get<PageResult<Approval>>('/admin/approvals', { params })
}

export const handleApproval = (familyId: number, requestId: number, data: { approved: boolean }) => {
  return request.post(`/admin/approval/${familyId}/${requestId}/handle`, { action: data.approved ? 'approve' : 'reject' })
}
