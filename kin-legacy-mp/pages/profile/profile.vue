<template>
  <view class="jpu-page-container">
    <view class="jpu-user-card">
      <view class="jpu-user-avatar">
        <text class="jpu-avatar-text">{{ userInfo && userInfo.nickname ? userInfo.nickname.charAt(0) : '?' }}</text>
      </view>
      <view class="jpu-user-info">
        <text class="jpu-user-name">{{ userInfo && userInfo.nickname || '未登录' }}</text>
        <text class="jpu-user-id">ID: {{ userInfo && userInfo.id || '---' }}</text>
      </view>
    </view>

    <view class="jpu-menu-card">
      <view class="jpu-menu-item" @click="navigateTo('/pages/settings/settings')">
        <text class="jpu-menu-text">账号设置</text>
        <text class="jpu-menu-arrow">❯</text>
      </view>
      <view class="jpu-menu-item" @click="navigateTo('/pages/about/about')">
        <text class="jpu-menu-text">关于我们</text>
        <text class="jpu-menu-arrow">❯</text>
      </view>
      <view class="jpu-menu-item jpu-logout" @click="handleLogout">
        <text class="jpu-menu-text jpu-logout-text">退出登录</text>
      </view>
    </view>

    <view class="jpu-version-info">
      <text class="jpu-version-text">家谱 v1.0.0</text>
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
.jpu-page-container {
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 32rpx;
}

.jpu-user-card {
  display: flex;
  align-items: center;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  padding: 32rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 32rpx;
}

.jpu-user-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background-color: var(--theme-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(142, 41, 44, 0.2);
}

.jpu-avatar-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #FFFFFF;
  letter-spacing: 4rpx;
}

.jpu-user-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
  flex: 1;
}

.jpu-user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--theme-text);
  letter-spacing: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.jpu-user-id {
  font-size: 26rpx;
  color: #8D6E63;
}

.jpu-menu-card {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.jpu-menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid var(--theme-border);
}

.jpu-menu-item:last-child {
  border-bottom: none;
}

.jpu-menu-item:active {
  background-color: var(--theme-bg);
}

.jpu-menu-text {
  font-size: 30rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
  color: var(--theme-text);
}

.jpu-menu-arrow {
  font-size: 28rpx;
  color: var(--theme-border);
}

.jpu-logout-text {
  color: var(--theme-primary);
}

.jpu-version-info {
  text-align: center;
  margin-top: 120rpx;
}

.jpu-version-text {
  font-size: 24rpx;
  color: var(--theme-border);
}
</style>
