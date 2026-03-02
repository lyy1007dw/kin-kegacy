import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

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
  genealogyId?: number
  genealogyName?: string
  accountRole?: string
  birthPlace?: string
  deathDate?: string
  age?: number
  photos?: string
  relations?: MemberRelation[]
}

export interface MemberRelation {
  memberId: number
  memberName: string
  memberGender: string
  relationType: string
  relationLabel: string
}

export interface MemberQueryParams {
  page?: number
  size?: number
  name?: string
  gender?: string
  birthDateStart?: string
  birthDateEnd?: string
  genealogyId?: number
  createTimeStart?: string
  createTimeEnd?: string
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

export interface MemberEditParams {
  name?: string
  gender?: string
  birthDate?: string
  birthPlace?: string
  deathDate?: string
  bio?: string
  avatar?: string
  genealogyId?: number
  accountRole?: string
}

export interface MemberTransferCheck {
  canTransfer: boolean
  warnings: string[]
  affectedRelations: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export const getMemberList = (params: MemberQueryParams = {}) => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.globalRole
  
  if (role === 'SUPER_ADMIN') {
    return request.get<PageResult<Member>>('/admin/member', { params })
  } else {
    return Promise.resolve({ data: { records: [], total: 0, page: 1, size: 10 } })
  }
}

export const getFamilyMembers = (familyId: number) => {
  return request.get<Member[]>(`/family/${familyId}/members`)
}

export const getMemberDetail = (familyId: number, memberId: number) => {
  return request.get<Member>(`/family/${familyId}/member/${memberId}/detail`)
}

export const updateMember = (familyId: number, memberId: number, data: Partial<Member>) => {
  return request.put(`/family/${familyId}/member/${memberId}`, data)
}

export const deleteMember = (familyId: number, memberId: number) => {
  return request.delete(`/admin/member/${memberId}?familyId=${familyId}`)
}

export const addMember = (data: AddMemberParams) => {
  return request.post('/family/' + data.familyId + '/member', {
    name: data.name,
    gender: data.gender,
    avatar: data.avatar,
    birthDate: data.birthDate,
    bio: data.bio
  })
}

export const updateMemberByAdmin = (memberId: number, data: MemberEditParams) => {
  return request.put(`/admin/member/${memberId}`, data)
}

export const checkMemberTransfer = (memberId: number, targetGenealogyId: number) => {
  return request.get<MemberTransferCheck>('/admin/member/check-transfer', {
    params: { memberId, targetGenealogyId }
  })
}
