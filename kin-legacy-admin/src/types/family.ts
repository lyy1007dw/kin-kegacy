export interface Family {
  id: number
  name: string
  code: string
  avatar: string
  description: string
  creatorId: number
  memberCount: number
  createTime: string
}

export interface FamilyDetail extends Family {
  // 详情可能包含更多字段
}
