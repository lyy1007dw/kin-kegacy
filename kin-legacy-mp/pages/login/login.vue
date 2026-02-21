<template>
  <view class="login-container">
    <view class="login-header">
      <view class="logo-wrap">
        <text class="logo-text">家谱</text>
      </view>
      <text class="slogan">记录家族传承 延续血脉亲情</text>
    </view>

    <view class="login-form">
      <view class="form-item">
        <text class="form-label">用户名</text>
        <input 
          class="form-input" 
          placeholder="请输入用户名" 
          v-model="form.username"
          placeholder-class="placeholder"
        />
      </view>
      
      <view class="form-item">
        <text class="form-label">密码</text>
        <input 
          class="form-input" 
          placeholder="请输入密码" 
          v-model="form.password"
          type="password"
          placeholder-class="placeholder"
        />
      </view>

      <view class="btn-login" @click="handleLogin">
        <text class="btn-text">登录</text>
      </view>
      
      <view class="divider">
        <view class="divider-line"></view>
        <text class="divider-text">或</text>
        <view class="divider-line"></view>
      </view>

      <view class="btn-wechat" @click="handleWxLogin">
        <text class="btn-text">微信快捷登录</text>
      </view>
      
      <view class="tips">
        <text class="tips-text">开发工具中微信登录将模拟创建新用户</text>
      </view>
    </view>

    <view class="login-footer">
      <text class="footer-text">登录即表示同意</text>
      <text class="footer-link">《用户协议》</text>
      <text class="footer-text">和</text>
      <text class="footer-link">《隐私政策》</text>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      }
    }
  },

  methods: {
    ...mapActions(['login', 'wxLogin']),

    async handleLogin() {
      if (!this.form.username.trim()) {
        uni.showToast({ title: '请输入用户名', icon: 'none' })
        return
      }
      if (!this.form.password.trim()) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '登录中...' })
        await this.login(this.form)
        uni.hideLoading()
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(function() {
          uni.switchTab({ url: '/pages/index/index' })
        }, 1000)
      } catch (error) {
        uni.hideLoading()
      }
    },

    async handleWxLogin() {
      var self = this
      uni.showLoading({ title: '登录中...' })
      
      uni.login({
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
        fail: function() {
          uni.hideLoading()
          uni.showToast({ title: '微信登录失败', icon: 'none' })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFFBF5 0%, #F9FAFB 100%);
  padding: 80rpx 40rpx 40rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 56rpx;
}

.logo-wrap {
  width: 140rpx;
  height: 140rpx;
  margin: 0 auto 24rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(139, 69, 19, 0.25);
}

.logo-text {
  font-size: 56rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 8rpx;
}

.slogan {
  font-size: 26rpx;
  color: #6B7280;
  letter-spacing: 4rpx;
}

.login-form {
  background-color: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05);
}

.form-item {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background-color: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #1F2937;
  box-sizing: border-box;
}

.placeholder {
  color: #9CA3AF;
}

.btn-login {
  margin-top: 36rpx;
  height: 96rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(139, 69, 19, 0.25);
}

.btn-login:active {
  opacity: 0.9;
}

.btn-text {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 4rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin: 36rpx 0;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: #E5E7EB;
}

.divider-text {
  padding: 0 28rpx;
  font-size: 24rpx;
  color: #9CA3AF;
}

.btn-wechat {
  height: 96rpx;
  border-radius: 48rpx;
  background-color: #07C160;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.25);
}

.btn-wechat:active {
  opacity: 0.9;
}

.tips {
  margin-top: 24rpx;
  text-align: center;
}

.tips-text {
  font-size: 22rpx;
  color: #9CA3AF;
}

.login-footer {
  margin-top: auto;
  padding-top: 48rpx;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.footer-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.footer-link {
  font-size: 24rpx;
  color: #8B4513;
}
</style>
