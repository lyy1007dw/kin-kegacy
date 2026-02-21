<template>
  <view class="page-container">
    <view class="user-card">
      <view class="user-avatar">
        <text class="avatar-text">{{ userInfo && userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userInfo && userInfo.nickname || '未登录' }}</text>
        <text class="user-id">ID: {{ userInfo && userInfo.id || '---' }}</text>
      </view>
    </view>

    <view class="menu-card">
      <view class="menu-item" @click="navigateTo('/pages/settings/settings')">
        <text class="menu-text">账号设置</text>
        <text class="menu-arrow">→</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/about/about')">
        <text class="menu-text">关于我们</text>
        <text class="menu-arrow">→</text>
      </view>
      <view class="menu-item logout" @click="handleLogout">
        <text class="menu-text logout-text">退出登录</text>
      </view>
    </view>

    <view class="version-info">
      <text class="version-text">家谱 MVP v1.0.0</text>
    </view>
  </view>
</template>

<script>
import { mapState, mapActions } from 'vuex'

export default {
  computed: {
    ...mapState(['userInfo'])
  },

  onShow() {
    if (!this.userInfo) {
      this.getUserInfo()
    }
  },

  methods: {
    ...mapActions(['getUserInfo', 'logout']),

    navigateTo(url) {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },

    handleLogout() {
      var self = this
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: function(res) {
          if (res.confirm) {
            self.logout()
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }
        }
      })
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

.user-card {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  padding: 32rpx;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 32rpx;
}

.user-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(139, 69, 19, 0.2);
}

.avatar-text {
  font-size: 48rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 4rpx;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
  flex: 1;
}

.user-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-id {
  font-size: 26rpx;
  color: #6B7280;
}

.menu-card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #F3F4F6;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background-color: #F9FAFB;
}

.menu-text {
  font-size: 30rpx;
  color: #1F2937;
}

.menu-arrow {
  font-size: 28rpx;
  color: #D1D5DB;
}

.logout-text {
  color: #DC2626;
}

.version-info {
  text-align: center;
  margin-top: 120rpx;
}

.version-text {
  font-size: 24rpx;
  color: #D1D5DB;
}
</style>
