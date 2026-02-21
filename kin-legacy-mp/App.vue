<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    const token = uni.getStorageSync('token')
    if (token) {
      this.$store.dispatch('getUserInfo').catch(function() {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('userInfo')
      })
    }
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
@import './common/styles/common.css';

page {
  background: linear-gradient(180deg, #FFFBF5 0%, #F9FAFB 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 28rpx;
  color: #1F2937;
  -webkit-tap-highlight-color: transparent;
}

.font-serif {
  font-family: 'Times New Roman', Times, serif;
}

.text-primary {
  color: #8B4513;
}

.bg-primary {
  background-color: #8B4513;
}

.border-primary {
  border-color: #8B4513;
}
</style>
