import { createStore } from 'vuex'
import api from '../utils/api'

const store = createStore({
  state: {
    token: uni.getStorageSync('token') || '',
    userId: uni.getStorageSync('userId') || null,
    userInfo: uni.getStorageSync('userInfo') || null,
    currentFamily: null,
    myFamilies: [],
    pendingApprovalCount: 0
  },

  getters: {
    isLoggedIn: (state) => !!state.token && !!state.userId,
    isCreator: (state) => state.currentFamily && state.userInfo && state.currentFamily.creatorId === state.userInfo.id
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    
    SET_USER_ID(state, userId) {
      state.userId = userId
      uni.setStorageSync('userId', userId)
    },
    
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      uni.setStorageSync('userInfo', userInfo)
      if (userInfo && userInfo.id) {
        state.userId = userInfo.id
        uni.setStorageSync('userId', userInfo.id)
      }
    },
    
    CLEAR_AUTH(state) {
      state.token = ''
      state.userId = null
      state.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('userInfo')
    },
    
    SET_CURRENT_FAMILY(state, family) {
      state.currentFamily = family
    },
    
    SET_MY_FAMILIES(state, families) {
      state.myFamilies = families
    },
    
    SET_PENDING_APPROVAL_COUNT(state, count) {
      state.pendingApprovalCount = count
    }
  },

  actions: {
    async wxLogin({ commit }, code) {
      try {
        const res = await api.auth.wxLogin(code)
        commit('SET_TOKEN', res.token)
        commit('SET_USER_INFO', res.user)
        return res
      } catch (error) {
        throw error
      }
    },

    async getUserInfo({ commit }) {
      try {
        const res = await api.auth.getCurrentUser()
        commit('SET_USER_INFO', res)
        return res
      } catch (error) {
        throw error
      }
    },

    logout({ commit }) {
      commit('CLEAR_AUTH')
    },

    async fetchMyFamilies({ commit }) {
      try {
        const res = await api.family.getMine()
        commit('SET_MY_FAMILIES', res)
        if (res && res.length > 0 && !this.state.currentFamily) {
          commit('SET_CURRENT_FAMILY', res[0])
        }
        return res
      } catch (error) {
        throw error
      }
    },

    setCurrentFamily({ commit }, family) {
      commit('SET_CURRENT_FAMILY', family)
    },

    async fetchPendingApprovalCount({ commit }, familyId) {
      try {
        const res = await api.approval.getList(familyId, { status: 'pending', size: 1 })
        commit('SET_PENDING_APPROVAL_COUNT', res.total || 0)
        return res.total || 0
      } catch (error) {
        return 0
      }
    }
  }
})

export default store
