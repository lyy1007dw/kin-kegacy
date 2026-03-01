import { get, post, put, del } from './request'

export default {
  auth: {
    wxLogin: (code) => post('/auth/wx-login', { code }),
    getCurrentUser: () => get('/auth/me'),
    logout: () => post('/auth/logout')
  },

  family: {
    create: (data) => post('/family/create', data),
    join: (data) => post('/family/join', data),
    getById: (id) => get(`/family/${id}`),
    getMine: () => get('/family/mine'),
    getByCode: (code) => get(`/family/code/${code}`),
    getAll: () => get('/family/list'),
    update: (id, data) => put(`/family/${id}`, data),
    delete: (id) => del(`/family/${id}`)
  },

  member: {
    getList: (familyId) => get(`/family/${familyId}/members`),
    getTree: (familyId) => get(`/family/${familyId}/tree`),
    getById: (familyId, memberId) => get(`/family/${familyId}/member/${memberId}`),
    getDetail: (familyId, memberId) => get(`/family/${familyId}/member/${memberId}/detail`),
    add: (familyId, data) => post(`/family/${familyId}/member`, data),
    addChild: (familyId, parentId, data) => post(`/family/${familyId}/member/${parentId}/add-child`, data),
    addParent: (familyId, childId, data) => post(`/family/${familyId}/member/${childId}/add-parent`, data),
    applyEdit: (familyId, memberId, data) => post(`/family/${familyId}/member/${memberId}/edit-request`, data),
    update: (familyId, memberId, data) => put(`/family/${familyId}/member/${memberId}`, data),
    delete: (familyId, memberId) => del(`/family/${familyId}/member/${memberId}`)
  },

  approval: {
    getList: (familyId, params) => get(`/approvals/family/${familyId}`, params),
    getAll: (params) => get('/approvals', params),
    getFamilyApprovals: (familyId, params) => get('/approvals/family/' + familyId, params),
    handle: (familyId, requestId, data) => post(`/approvals/${requestId}/handle?familyId=${familyId}`, data)
  },

  user: {
    getCurrent: () => get('/user/me'),
    update: (id, data) => put(`/user/${id}`, data),
    updateName: (name) => put('/user/name', { name })
  }
}
