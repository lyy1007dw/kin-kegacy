import request from '@/utils/request'

export interface Statistics {
  familyCount: number
  memberCount: number
  userCount: number
  pendingApproval: number
}

export const getStatistics = () => {
  return request.get<Statistics>('/statistics')
}
