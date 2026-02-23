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

/* 全局CSS变量 - 传统风格 */
page, view, text {
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-text: #3E2A23;
  --theme-primary: #8E292C;
  --theme-border: #D4C9BD;
}

page {  
  background-color: var(--theme-bg);
  font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
  font-size: 28rpx;
  color: var(--theme-text);
  -webkit-tap-highlight-color: transparent;
}

/* 衬线字体 */
.font-serif {
  font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
}

/* 主色调 */
.text-primary {
  color: var(--theme-primary);
}

.bg-primary {
  background-color: var(--theme-primary);
}

.border-primary {
  border-color: var(--theme-primary);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8rpx;
  height: 8rpx;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--theme-border);
  border-radius: 8rpx;
}

/* 安全区适配 */
.jpu-safe-top {
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

.jpu-safe-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* Section 标题样式 */
.jpu-section-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: bold;
  color: #6D4C41;
  margin-bottom: 24rpx;
  margin-left: 8rpx;
}

.jpu-section-line {
  width: 8rpx;
  height: 28rpx;
  background-color: var(--theme-primary);
  margin-right: 16rpx;
  border-radius: 4rpx;
}
</style>
