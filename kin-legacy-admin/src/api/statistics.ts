import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

export interface Statistics {
  familyCount: number
  memberCount: number
  userCount: number
  pendingApproval: number
}

export const getStatistics = () => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.globalRole
  
  if (role === 'SUPER_ADMIN') {
    return request.get<Statistics>('/admin/statistics')
  } else {
    return Promise.resolve({
      data: {
        familyCount: 0,
        memberCount: 0,
        userCount: 0,
        pendingApproval: 0
      }
    })
  }
}
