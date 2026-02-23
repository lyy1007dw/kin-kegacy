<template>
  <view class="jpu-page-container">
    <view v-if="pendingApprovals.length === 0" class="jpu-empty-state">
      <view class="jpu-empty-icon-wrap">
        <text class="jpu-empty-icon-text">审</text>
      </view>
      <text class="jpu-empty-text">族内安宁，暂无奏报</text>
    </view>

    <view v-else class="jpu-approval-list">
      <!-- 提示栏 -->
      <view class="jpu-tip-warning">
        <text class="jpu-tip-bold">编修提示：</text>
        <text>族人提交的"信息修正"或"添丁"申请将在此处汇总，须由修谱人核准方可入谱。</text>
      </view>

      <view 
        class="jpu-approval-card" 
        v-for="approval in pendingApprovals" 
        :key="approval.id"
        :class="{ 'jpu-card-leaving': leavingId === approval.id }"
      >
        <view class="jpu-approval-header">
          <view class="jpu-approval-left">
            <text class="jpu-tag-danger">待核准</text>
            <text class="jpu-approval-family">{{ approval.familyName }}</text>
          </view>
          <text class="jpu-approval-applicant">{{ approval.applicantName }} 具禀</text>
        </view>

        <view class="jpu-approval-content">
          <view v-if="approval.type === 'join'">
            呈请为家族添丁入谱
          </view>
          <view v-else>
            呈请修正 <text class="jpu-text-bold">{{ approval.targetName }}</text> 之记录为：
            <text class="jpu-text-highlight jpu-text-underline">{{ approval.newValue }}</text>
          </view>
        </view>

        <view class="jpu-approval-actions">
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

    handleApproval(id, action) {
      var self = this
      this.leavingId = id
      
      setTimeout(function() {
        var familyId = self.currentFamily && self.currentFamily.id
        api.approval.handle(familyId, id, { 
          action: action === 'approve' ? 'approve' : 'reject' 
        }).then(function() {
          uni.showToast({ 
            title: action === 'approve' ? '已准奏' : '已驳回', 
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
</style>
