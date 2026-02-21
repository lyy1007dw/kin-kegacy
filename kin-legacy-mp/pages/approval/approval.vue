<template>
  <view class="page-container">
    <view v-if="pendingApprovals.length === 0" class="empty-state">
      <view class="empty-icon-wrap">
        <text class="empty-icon-text">审</text>
      </view>
      <text class="empty-text">暂无待审批事项</text>
    </view>

    <view v-else class="approval-list">
      <view 
        class="approval-card" 
        v-for="approval in pendingApprovals" 
        :key="approval.id"
        :class="{ 'card-leaving': leavingId === approval.id }"
      >
        <view class="approval-header">
          <view class="approval-type" :class="'type-' + approval.type">
            <text class="type-text">{{ getTypeText(approval.type) }}</text>
          </view>
          <text class="approval-time">{{ formatTime(approval.createTime) }}</text>
        </view>

        <view class="approval-body">
          <view class="approval-row">
            <text class="row-label">申请人</text>
            <text class="row-value">{{ approval.applicantName }}</text>
          </view>
          
          <view v-if="approval.type === 'join'" class="approval-row">
            <text class="row-label">关系说明</text>
            <text class="row-value">{{ approval.relationDesc }}</text>
          </view>
          
          <view v-else class="approval-change">
            <view class="change-row">
              <text class="change-label">修改字段</text>
              <text class="change-value">{{ getFieldName(approval.fieldName) }}</text>
            </view>
            <view class="change-row">
              <text class="change-label">原内容</text>
              <text class="change-old">{{ approval.oldValue || '无' }}</text>
            </view>
            <view class="change-row">
              <text class="change-label">新内容</text>
              <text class="change-new">{{ approval.newValue }}</text>
            </view>
          </view>
        </view>

        <view class="approval-actions">
          <view class="btn-reject" @click="handleApproval(approval.id, 'reject')">拒绝</view>
          <view class="btn-approve" @click="handleApproval(approval.id, 'approve')">同意</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'
import api from '../../utils/api'

export default {
  data() {
    return {
      pendingApprovals: [],
      leavingId: null
    }
  },

  computed: {
    ...mapState(['currentFamily'])
  },

  onShow() {
    this.loadApprovals()
  },

  onPullDownRefresh() {
    var self = this
    this.loadApprovals().then(function() {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    loadApprovals() {
      var self = this
      return api.approval.getAll({ status: 'pending' }).then(function(res) {
        self.pendingApprovals = res.records || res || []
      }).catch(function(error) {
        console.error('加载审批列表失败', error)
      })
    },

    getTypeText(type) {
      if (type === 'join') return '加入申请'
      if (type === 'modify') return '修改申请'
      return '申请'
    },

    getFieldName(fieldName) {
      var fieldMap = {
        'name': '姓名',
        'birthDate': '出生日期',
        'bio': '简介',
        'avatar': '头像'
      }
      return fieldMap[fieldName] || fieldName
    },

    formatTime(time) {
      if (!time) return ''
      var date = new Date(time)
      var now = new Date()
      var diff = now - date
      
      var minutes = Math.floor(diff / 60000)
      var hours = Math.floor(diff / 3600000)
      var days = Math.floor(diff / 86400000)
      
      if (minutes < 60) {
        return minutes + '分钟前'
      } else if (hours < 24) {
        return hours + '小时前'
      } else if (days < 30) {
        return days + '天前'
      } else {
        return (date.getMonth() + 1) + '月' + date.getDate() + '日'
      }
    },

    handleApproval(id, action) {
      var self = this
      this.leavingId = id
      
      setTimeout(function() {
        var familyId = self.currentFamily && self.currentFamily.id
        api.approval.handle(familyId, id, { 
          action: action === 'approve' ? 'approve' : 'reject' 
        }).then(function() {
          uni.showToast({ 
            title: action === 'approve' ? '已同意' : '已拒绝', 
            icon: 'success' 
          })
          
          self.pendingApprovals = self.pendingApprovals.filter(function(a) { 
            return a.id !== id 
          })
          self.leavingId = null
        }).catch(function(error) {
          self.leavingId = null
        })
      }, 300)
    }
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFFBF5 0%, #F9FAFB 100%);
  padding: 32rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.empty-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(139, 69, 19, 0.2);
}

.empty-icon-text {
  font-size: 64rpx;
  color: #FFFFFF;
  font-weight: 600;
  letter-spacing: 8rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.approval-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.approval-card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.card-leaving {
  opacity: 0;
  transform: translateX(-40rpx);
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.approval-type {
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.type-join {
  background-color: #EFF6FF;
}

.type-join .type-text {
  color: #2563EB;
}

.type-modify {
  background-color: #FFF7ED;
}

.type-modify .type-text {
  color: #EA580C;
}

.type-text {
  font-size: 26rpx;
  font-weight: 500;
}

.approval-time {
  font-size: 24rpx;
  color: #9CA3AF;
}

.approval-body {
  margin-bottom: 20rpx;
}

.approval-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.approval-row:last-child {
  margin-bottom: 0;
}

.row-label {
  width: 140rpx;
  font-size: 28rpx;
  color: #6B7280;
  flex-shrink: 0;
}

.row-value {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2937;
  word-break: break-all;
}

.approval-change {
  background-color: #F9FAFB;
  border-radius: 12rpx;
  padding: 20rpx;
}

.change-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.change-row:last-child {
  margin-bottom: 0;
}

.change-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #6B7280;
  flex-shrink: 0;
}

.change-value {
  font-size: 26rpx;
  font-weight: 500;
  color: #1F2937;
}

.change-old {
  font-size: 26rpx;
  color: #9CA3AF;
  word-break: break-all;
}

.change-new {
  font-size: 26rpx;
  font-weight: 600;
  color: #8B4513;
  word-break: break-all;
}

.approval-actions {
  display: flex;
  gap: 24rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #F3F4F6;
}

.btn-reject {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border: 2rpx solid #E5E7EB;
  border-radius: 44rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
  color: #4B5563;
}

.btn-reject:active {
  background-color: #F3F4F6;
}

.btn-approve {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  border-radius: 44rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
  color: #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(139, 69, 19, 0.2);
}

.btn-approve:active {
  opacity: 0.9;
}
</style>
