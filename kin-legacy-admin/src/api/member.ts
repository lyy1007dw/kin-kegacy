import request from '@/utils/request'

export interface Member {
  id: number
  familyId: number
  familyName?: string
  userId: number
  name: string
  gender: string
  avatar: string
  birthDate: string
  bio: string
  isCreator: number
  createTime: string
}

export interface AddMemberParams {
  userId: number
  familyId: number
  name: string
  gender: string
  avatar?: string
  birthDate?: string
  bio?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export const getMemberList = (page = 1, size = 10) => {
  return request.get<PageResult<Member>>('/admin/member/list/paged', { params: { page, size } })
}

export const getFamilyMembers = (familyId: number) => {
  return request.get<Member[]>(`/family/${familyId}/members`)
}

export const updateMember = (familyId: number, memberId: number, data: Partial<Member>) => {
  return request.put(`/family/${familyId}/member/${memberId}`, data)
}

export const deleteMember = (familyId: number, memberId: number) => {
  return request.delete(`/family/${familyId}/member/${memberId}`)
}

export const addMember = (data: AddMemberParams) => {
  return request.post('/admin/member', data)
}
