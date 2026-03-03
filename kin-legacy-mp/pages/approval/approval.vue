<template>
  <view class="jpu-page-container">
    <!-- Tab切换 -->
    <view class="jpu-tabs">
      <view 
        class="jpu-tab" 
        :class="{ 'jpu-tab-active': activeTab === 'pending' }"
        @click="switchTab('pending')"
      >
        <text>待审批</text>
        <view v-if="pendingCount > 0" class="jpu-badge">{{ pendingCount }}</view>
      </view>
      <view 
        class="jpu-tab" 
        :class="{ 'jpu-tab-active': activeTab === 'approved' }"
        @click="switchTab('approved')"
      >
        <text>已通过</text>
      </view>
      <view 
        class="jpu-tab" 
        :class="{ 'jpu-tab-active': activeTab === 'rejected' }"
        @click="switchTab('rejected')"
      >
        <text>已拒绝</text>
      </view>
    </view>

    <view v-if="!currentFamily" class="jpu-empty-state">
      <view class="jpu-empty-icon-wrap">
        <text class="jpu-empty-icon-text">谱</text>
      </view>
      <text class="jpu-empty-text">请先选择一个家谱</text>
    </view>

    <view v-else-if="loading" class="loading-wrap">
      <text>加载中...</text>
    </view>

    <view v-else-if="approvals.length === 0" class="jpu-empty-state">
      <view class="jpu-empty-icon-wrap">
        <text class="jpu-empty-icon-text">审</text>
      </view>
      <text class="jpu-empty-text">{{ emptyText }}</text>
    </view>

    <view v-else class="jpu-approval-list">
      <view 
        class="jpu-approval-card" 
        v-for="approval in approvals" 
        :key="approval.id"
        :class="{ 'jpu-card-leaving': leavingId === approval.id }"
      >
        <view class="jpu-approval-header">
          <view class="jpu-approval-left">
            <text :class="getStatusClass(approval.status)">{{ getStatusText(approval.status) }}</text>
            <text class="jpu-approval-family">{{ approval.familyName }}</text>
          </view>
          <text class="jpu-approval-applicant">{{ approval.applicantName }} 具禀</text>
        </view>

        <view class="jpu-approval-content">
          <view v-if="approval.type === 'join'">
            <view v-if="approval.joinType === 'add_child'" class="jpu-join-type">
              <text class="jpu-type-tag">录入子嗣</text>
            </view>
            <view v-else-if="approval.joinType === 'add_parent'" class="jpu-join-type">
              <text class="jpu-type-tag">追溯先祖</text>
            </view>
            <view v-else class="jpu-join-type">
              <text class="jpu-type-tag jpu-type-new">新成员加入</text>
            </view>
            <view class="jpu-relation-desc" v-if="approval.relationDesc">
              <text>{{ approval.relationDesc }}</text>
            </view>
            <view v-else>
              <text>呈请为家族添丁入谱</text>
            </view>
          </view>
          <view v-else-if="approval.type === 'edit'">
            <view>申请修改成员信息</view>
            <view class="edit-info" v-if="approval.memberName">
              <text>成员: {{ approval.memberName }}</text>
            </view>
            <view class="edit-reason" v-if="parseChanges(approval.changesJson).length > 0">
              <text class="jpu-tip-bold">修改内容：</text>
              <view class="change-item" v-for="(change, idx) in parseChanges(approval.changesJson)" :key="idx">
                <text class="change-field">{{ change.fieldName }}</text>
                <text class="change-arrow">{{ change.oldValue || '(无)' }} → {{ change.newValue || '(无)' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="jpu-approval-actions" v-if="isPending(approval.status)">
          <view class="jpu-btn-gray" @click="handleApproval(approval.id, 'reject')">
            <text>驳回</text>
          </view>
          <view class="jpu-btn-primary" @click="handleApproval(approval.id, 'approve')">
            <text>准奏入谱</text>
          </view>
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
      approvals: [],
      loading: true,
      activeTab: 'pending',
      leavingId: null
    }
  },

  computed: {
    ...mapState(['currentFamily', 'userInfo']),
    pendingCount() {
      return this.approvals.filter(a => a.status === 'PENDING').length
    },
    emptyText() {
      switch(this.activeTab) {
        case 'pending': return '暂无待审批事项'
        case 'approved': return '暂无已通过记录'
        case 'rejected': return '暂无已拒绝记录'
        default: return '暂无记录'
      }
    },
    fieldMap() {
      return {
        name: '姓名',
        birthDate: '出生日期',
        birthPlace: '出生地',
        bio: '生平简介'
      }
    }
  },

  onShow() {
    if (this.currentFamily) {
      this.loadApprovals()
    }
  },

  onPullDownRefresh() {
    this.loadApprovals().then(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    switchTab(tab) {
      this.activeTab = tab
      this.loadApprovals()
    },

    parseChanges(changesJson) {
      if (!changesJson) return []
      try {
        const changes = typeof changesJson === 'string' ? JSON.parse(changesJson) : changesJson
        return Object.entries(changes).map(([key, value]) => ({
          fieldName: this.fieldMap[key] || key,
          oldValue: value.oldValue,
          newValue: value.newValue
        }))
      } catch (e) {
        console.error('解析changesJson失败', e)
        return []
      }
    },

    loadApprovals() {
      if (!this.currentFamily || !this.currentFamily.id) {
        this.loading = false
        this.approvals = []
        return Promise.resolve()
      }
      
      this.loading = true
      const status = this.activeTab === 'pending' ? 'PENDING' : 
                     this.activeTab === 'approved' ? 'APPROVED' : 'REJECTED'
      
      return api.approval.getFamilyApprovals(this.currentFamily.id, { status })
        .then(res => {
          this.approvals = res.records || res || []
        })
        .catch(error => {
          console.error('加载审批列表失败', error)
          this.approvals = []
        })
        .finally(() => {
          this.loading = false
        })
    },

    getStatusClass(status) {
      const s = status?.toUpperCase()
      switch(s) {
        case 'PENDING': return 'jpu-tag-danger'
        case 'APPROVED': return 'jpu-tag-success'
        case 'REJECTED': return 'jpu-tag-gray'
        default: return ''
      }
    },

    getStatusText(status) {
      const s = status?.toUpperCase()
      switch(s) {
        case 'PENDING': return '待核准'
        case 'APPROVED': return '已通过'
        case 'REJECTED': return '已拒绝'
        default: return status || '未知'
      }
    },

    isPending(status) {
      return status?.toUpperCase() === 'PENDING'
    },

    handleApproval(id, action) {
      this.leavingId = id
      
      uni.showLoading({ title: '处理中...' })
      
      api.approval.handle(this.currentFamily.id, id, { 
        action: action === 'approve' ? 'approve' : 'reject' 
      }).then(() => {
        uni.hideLoading()
        uni.showToast({ 
          title: action === 'approve' ? '已准奏' : '已驳回', 
          icon: 'success' 
        })
        
        this.approvals = this.approvals.filter(a => a.id !== id)
        this.leavingId = null
      }).catch(error => {
        uni.hideLoading()
        this.leavingId = null
        console.error('处理审批失败', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      })
    }
  }
}
</script>

<style scoped>
.jpu-page-container {
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-text: #3E2A23;
  --theme-primary: #8E292C;
  --theme-border: #D4C9BD;
  
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 32rpx;
}

/* 空状态 */
.jpu-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.jpu-empty-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background-color: var(--theme-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(142, 41, 44, 0.2);
}

.jpu-empty-icon-text {
  font-size: 64rpx;
  color: #FFFFFF;
  font-weight: bold;
  letter-spacing: 8rpx;
}

.jpu-empty-text {
  font-size: 28rpx;
  color: #8D6E63;
  letter-spacing: 4rpx;
}

/* 审批列表 */
.jpu-approval-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 提示区 */
.jpu-tip-warning {
  background-color: #FFF8E1;
  border: 2rpx solid #FFE082;
  border-radius: 8rpx;
  padding: 20rpx 24rpx;
  font-size: 24rpx;
  color: #8D6E63;
  line-height: 1.6;
}

.jpu-tip-bold {
  color: #F57F17;
  font-weight: bold;
}

/* 审批卡片 */
.jpu-approval-card {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.jpu-card-leaving {
  opacity: 0;
  transform: translateX(-40rpx);
}

.jpu-approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.jpu-approval-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.jpu-tag-danger {
  display: inline-block;
  background-color: #FDF2F1;
  border: 2rpx solid #E6B0AA;
  color: var(--theme-primary);
  font-size: 24rpx;
  font-weight: bold;
  padding: 6rpx 16rpx;
  border-radius: 4rpx;
}

.jpu-approval-family {
  font-size: 24rpx;
  color: #8D6E63;
}

.jpu-approval-applicant {
  font-size: 24rpx;
  color: #8D6E63;
}

.jpu-approval-content {
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  font-size: 28rpx;
  color: var(--theme-text);
  line-height: 1.6;
}

.jpu-text-bold {
  font-weight: bold;
  color: var(--theme-text);
}

.jpu-text-highlight {
  color: var(--theme-primary);
  font-weight: bold;
}

.jpu-text-underline {
  border-bottom: 2rpx dashed var(--theme-primary);
  padding-bottom: 2rpx;
}

.jpu-approval-actions {
  display: flex;
  gap: 24rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid var(--theme-border);
}

/* 按钮 */
.jpu-btn-gray {
  flex: 1;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 24rpx;
  text-align: center;
}

.jpu-btn-gray text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
  color: var(--theme-text);
}

.jpu-btn-gray:active {
  background-color: var(--theme-bg);
}

.jpu-btn-primary {
  flex: 1;
  background-color: var(--theme-primary);
  border: 2rpx solid #722023;
  border-radius: 8rpx;
  padding: 24rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(142, 41, 44, 0.2);
}

.jpu-btn-primary text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
  color: #FFFFFF;
}

.jpu-btn-primary:active {
  opacity: 0.9;
}

/* Tab切换 */
.jpu-tabs {
  display: flex;
  background-color: var(--theme-card);
  border-radius: 12rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}

.jpu-tab {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 28rpx;
  color: #8D6E63;
  border-radius: 8rpx;
  position: relative;
}

.jpu-tab-active {
  background-color: var(--theme-primary);
  color: #FFFFFF;
  font-weight: bold;
}

.jpu-badge {
  position: absolute;
  top: 8rpx;
  right: 20rpx;
  background-color: #FF5722;
  color: #FFFFFF;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

/* 加载中 */
.loading-wrap {
  text-align: center;
  padding: 100rpx;
  color: #8D6E63;
}

/* 状态标签 */
.jpu-tag-success {
  display: inline-block;
  background-color: #E8F5E9;
  border: 2rpx solid #A5D6A7;
  color: #2E7D32;
  font-size: 24rpx;
  font-weight: bold;
  padding: 6rpx 16rpx;
  border-radius: 4rpx;
}

.jpu-tag-gray {
  display: inline-block;
  background-color: #F5F5F5;
  border: 2rpx solid #E0E0E0;
  color: #9E9E9E;
  font-size: 24rpx;
  font-weight: bold;
  padding: 6rpx 16rpx;
  border-radius: 4rpx;
}

/* 编辑信息 */
.edit-info {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #666;
}

.edit-reason {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #666;
}

.change-item {
  display: flex;
  align-items: center;
  padding: 8rpx 0;
  font-size: 24rpx;
}

.change-field {
  color: #8E292C;
  font-weight: bold;
  min-width: 120rpx;
}

.change-arrow {
  color: #666;
  margin-left: 16rpx;
}

/* 加入类型标签 */
.jpu-join-type {
  margin-bottom: 12rpx;
}

.jpu-type-tag {
  display: inline-block;
  background-color: #E3F2FD;
  border: 2rpx solid #1976D2;
  color: #1976D2;
  font-size: 24rpx;
  font-weight: bold;
  padding: 6rpx 16rpx;
  border-radius: 4rpx;
}

.jpu-type-new {
  background-color: #E8F5E9;
  border: 2rpx solid #388E3C;
  color: #388E3C;
}

.jpu-relation-desc {
  margin-top: 12rpx;
  font-size: 28rpx;
  color: var(--theme-text);
  font-weight: 500;
}
</style>
