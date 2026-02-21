import request from '@/utils/request'

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

export const getApprovalList = (familyId: number, type?: string, status?: string, page = 1, size = 10) => {
  return request.get<Approval[]>(`/family/${familyId}/approvals`, {
    params: { type, status, page, size }
  })
}

export const getAllApprovalList = (type?: string, status?: string, page = 1, size = 10) => {
  return request.get<Approval[]>('/admin/approvals', {
    params: { type, status, page, size }
  })
}


