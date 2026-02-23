<template>
  <view class="jpu-login-container">
    <view class="jpu-login-header">
      <view class="jpu-logo-wrap">
        <text class="jpu-logo-text">慎重</text>
        <text class="jpu-logo-text">追远</text>
      </view>
      <text class="jpu-slogan">慎终追远 · 民德归厚</text>
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

    <!-- 首次登录填写姓名弹窗 -->
    <view v-if="showNameModal" class="jpu-modal-overlay jpu-open">
      <view class="jpu-modal-center">
        <view class="jpu-modal-header">
          <text class="jpu-modal-title">请填写您的姓名</text>
        </view>
        
        <view class="jpu-modal-body">
          <view class="jpu-form-group">
            <text class="jpu-form-label">真实姓名</text>
            <input 
              class="jpu-form-input" 
              placeholder="请输入您的真实姓名" 
              v-model="nameForm.name"
              :focus="showNameModal"
              placeholder-class="jpu-placeholder"
            />
          </view>
          <view class="jpu-name-tip">
            <text>姓名用于在家谱中展示，请如实填写</text>
          </view>
        </view>
        
        <view class="jpu-modal-footer">
          <view class="jpu-btn-primary" @click="handleSaveName">
            <text>保存并进入</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'
import api from '../../utils/api'

export default {
  data() {
    return {
      showNameModal: false,
      nameForm: {
        name: ''
      }
    }
  },

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
            const res = await self.wxLogin(loginRes.code)
            uni.hideLoading()
            
            if (res.userInfo && res.userInfo.nameRequired) {
              self.showNameModal = true
            } else {
              uni.showToast({ title: '登录成功', icon: 'success' })
              setTimeout(function() {
                uni.switchTab({ url: '/pages/index/index' })
              }, 1000)
            }
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
    },

    async handleSaveName() {
      if (!this.nameForm.name || !this.nameForm.name.trim()) {
        uni.showToast({ title: '请输入您的姓名', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '保存中...' })
        await api.user.updateName(this.nameForm.name.trim())
        uni.hideLoading()
        
        this.showNameModal = false
        uni.showToast({ title: '保存成功', icon: 'success' })
        
        setTimeout(function() {
          uni.switchTab({ url: '/pages/index/index' })
        }, 1000)
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.jpu-login-container {
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-text: #3E2A23;
  --theme-primary: #8E292C;
  --theme-border: #D4C9BD;
  
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  box-shadow: 0 12rpx 40rpx rgba(142, 41, 44, 0.3);
}

.jpu-logo-text {
  font-size: 44rpx;
  font-weight: bold;
  color: #FFFFFF;
  font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
  text-align: center;
  line-height: 1;
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

/* 遮罩层 */
.jpu-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 居中模态框 */
.jpu-modal-center {
  width: 85%;
  max-width: 600rpx;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.2);
}

.jpu-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background-color: var(--theme-bg);
  border-bottom: 2rpx solid var(--theme-border);
}

.jpu-modal-title {
  font-size: 34rpx;
  font-weight: bold;
  color: var(--theme-text);
  letter-spacing: 4rpx;
}

.jpu-modal-body {
  padding: 32rpx;
}

.jpu-modal-footer {
  display: flex;
  padding: 24rpx 32rpx 32rpx;
  gap: 24rpx;
}

.jpu-form-group {
  margin-bottom: 24rpx;
}

.jpu-form-label {
  display: block;
  font-size: 28rpx;
  color: var(--theme-text);
  margin-bottom: 12rpx;
  font-weight: bold;
}

.jpu-form-input {
  width: 100%;
  height: 88rpx;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: var(--theme-text);
  box-sizing: border-box;
}

.jpu-placeholder {
  color: #9CA3AF;
}

.jpu-name-tip {
  font-size: 24rpx;
  color: #8D6E63;
  line-height: 1.5;
}

.jpu-btn-primary {
  flex: 1;
  background-color: var(--theme-primary);
  border: 2rpx solid #722023;
  border-radius: 12rpx;
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
</style>
