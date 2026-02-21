<template>
  <view class="jpu-login-container">
    <view class="jpu-login-header">
      <view class="jpu-logo-wrap">
        <text class="jpu-logo-text">家谱</text>
      </view>
      <text class="jpu-slogan">记录家族传承 延续血脉亲情</text>
    </view>

    <view class="jpu-login-form">
      <view class="jpu-btn-wechat" @click="handleWxLogin">
        <text class="jpu-btn-wechat-text">微信一键登录</text>
      </view>
    </view>

    <view class="jpu-login-footer">
      <text class="jpu-footer-text">登录即表示同意</text>
      <text class="jpu-footer-link">《用户协议》</text>
      <text class="jpu-footer-text">和</text>
      <text class="jpu-footer-link">《隐私政策》</text>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  methods: {
    ...mapActions(['wxLogin']),

    async handleWxLogin() {
      var self = this
      uni.showLoading({ title: '登录中...' })
      
      uni.login({
        provider: 'weixin',
        success: async function(loginRes) {
          if (!loginRes.code) {
            uni.hideLoading()
            uni.showToast({ title: '获取登录凭证失败', icon: 'none' })
            return
          }
          
          try {
            await self.wxLogin(loginRes.code)
            uni.hideLoading()
            uni.showToast({ title: '登录成功', icon: 'success' })
            setTimeout(function() {
              uni.switchTab({ url: '/pages/index/index' })
            }, 1000)
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: error.message || '微信登录失败', icon: 'none' })
          }
        },
        fail: function(err) {
          uni.hideLoading()
          uni.showToast({ title: '微信登录失败', icon: 'none' })
        }
      })
    }
  }
}
</script>

<style scoped>
.jpu-login-container {
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 160rpx 48rpx 48rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.jpu-login-header {
  text-align: center;
  margin-bottom: 120rpx;
}

.jpu-logo-wrap {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 32rpx;
  background-color: var(--theme-primary);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(142, 41, 44, 0.3);
}

.jpu-logo-text {
  font-size: 64rpx;
  font-weight: bold;
  color: #FFFFFF;
  font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
  text-align: center;
}

.jpu-slogan {
  font-size: 28rpx;
  color: #8D6E63;
  letter-spacing: 4rpx;
}

.jpu-login-form {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05);
}

.jpu-btn-wechat {
  height: 100rpx;
  border-radius: 12rpx;
  background-color: #07C160;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.3);
}

.jpu-btn-wechat:active {
  opacity: 0.9;
  transform: scale(0.98);
}

.jpu-btn-wechat-icon {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: bold;
  margin-right: 16rpx;
}

.jpu-btn-wechat-text {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
}

.jpu-login-footer {
  margin-top: auto;
  padding-top: 80rpx;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.jpu-footer-text {
  font-size: 24rpx;
  color: #8D6E63;
}

.jpu-footer-link {
  font-size: 24rpx;
  color: var(--theme-primary);
}
</style>
