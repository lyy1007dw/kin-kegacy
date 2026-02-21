<template>
  <view class="custom-tabbar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
    <view 
      class="tab-item" 
      v-for="(item, index) in tabs" 
      :key="index"
      :class="{ active: currentIndex === index }"
      @click="switchTab(index)"
    >
      <view class="tab-icon">
        <text class="icon-text">{{ item.iconText }}</text>
      </view>
      <text class="tab-text">{{ item.text }}</text>
      <view v-if="item.badge && item.badge > 0" class="tab-badge">
        <text class="badge-text">{{ item.badge > 99 ? '99+' : item.badge }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomTabBar',
  props: {
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      currentIndex: 0,
      safeAreaBottom: 0,
      tabs: [
        { iconText: '首页', text: '首页', path: '/pages/index/index' },
        { iconText: '谱', text: '家谱树', path: '/pages/tree/tree' },
        { iconText: '审', text: '审批', path: '/pages/approval/approval', badge: 0 },
        { iconText: '我', text: '我的', path: '/pages/profile/profile' }
      ]
    }
  },
  watch: {
    current: {
      immediate: true,
      handler: function(val) {
        this.currentIndex = val
      }
    }
  },
  created: function() {
    var systemInfo = uni.getSystemInfoSync()
    this.safeAreaBottom = systemInfo.safeAreaInsets && systemInfo.safeAreaInsets.bottom || 0
  },
  methods: {
    switchTab: function(index) {
      if (this.currentIndex === index) return
      
      this.currentIndex = index
      uni.switchTab({
        url: this.tabs[index].path
      })
    },
    setBadge: function(index, count) {
      if (this.tabs[index]) {
        this.tabs[index].badge = count
      }
    }
  }
}
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #FFFFFF;
  border-top: 2rpx solid #F3F4F6;
  padding-top: 8rpx;
  z-index: 999;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;
  position: relative;
  flex: 1;
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-text {
  font-size: 24rpx;
  font-weight: 500;
  color: #9CA3AF;
  transition: all 0.2s;
}

.tab-item.active .icon-text {
  color: #8B4513;
  font-weight: 600;
}

.tab-text {
  font-size: 20rpx;
  color: #9CA3AF;
  margin-top: 4rpx;
  transition: color 0.2s;
}

.tab-item.active .tab-text {
  color: #8B4513;
  font-weight: 500;
}

.tab-badge {
  position: absolute;
  top: 0;
  right: 20%;
  min-width: 32rpx;
  height: 32rpx;
  background-color: #DC2626;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge-text {
  font-size: 18rpx;
  color: #FFFFFF;
  font-weight: 500;
}
</style>
