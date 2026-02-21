<template>
  <view class="tree-node-wrapper">
    <view 
      class="member-card" 
      :class="cardClass" 
      @click="handleClick"
    >
      <view class="avatar-circle" :class="{ 'avatar-me': isCurrentUser }">
        <text class="avatar-text">{{ avatarText }}</text>
      </view>
      <text class="member-name">{{ node.name }}</text>
      <text class="member-info">{{ memberInfo }}</text>
      <view v-if="isCurrentUser" class="me-badge">
        <text class="me-badge-text">我</text>
      </view>
    </view>
    
    <view v-if="hasChildren" class="tree-children">
      <view class="tree-line-top"></view>
      <view 
        class="tree-child" 
        v-for="child in node.children" 
        :key="child.id"
      >
        <TreeNode :node="child" :currentUserId="currentUserId" @click="handleChildClick" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TreeNode',
  props: {
    node: {
      type: Object,
      required: true
    },
    currentUserId: {
      type: [Number, String],
      default: null
    }
  },
  computed: {
    isCurrentUser() {
      return this.node.userId === this.currentUserId
    },
    avatarText() {
      return this.node.name ? this.node.name.charAt(0) : '?'
    },
    memberInfo() {
      if (this.node.isCreator) return '创建者'
      return this.node.gender === 'male' ? '男' : '女'
    },
    hasChildren() {
      return this.node.children && this.node.children.length > 0
    },
    cardClass() {
      var classes = []
      if (this.node.isCreator) classes.push('member-card-creator')
      if (this.isCurrentUser) classes.push('member-card-me')
      return classes.join(' ')
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.node)
    },
    handleChildClick(child) {
      this.$emit('click', child)
    }
  }
}
</script>

<style scoped>
.tree-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.member-card {
  background: #FFFFFF;
  border: 3rpx solid #E5E7EB;
  padding: 20rpx 16rpx;
  border-radius: 20rpx;
  width: 156rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.member-card:active {
  transform: scale(0.98);
  border-color: #A0522D;
}

.member-card-creator {
  border: 3rpx solid #8B4513;
  background: linear-gradient(180deg, #FFFBF5 0%, #FFFFFF 100%);
}

.member-card-me {
  border: 4rpx solid #3B82F6;
  background: linear-gradient(180deg, #EFF6FF 0%, #FFFFFF 100%);
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.2);
}

.avatar-circle {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  margin: 0 auto 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-me {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border: 3rpx solid #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.avatar-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 2rpx;
}

.member-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F2937;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-info {
  font-size: 20rpx;
  color: #9CA3AF;
  display: block;
  margin-top: 4rpx;
}

.me-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background-color: #3B82F6;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #FFFFFF;
}

.me-badge-text {
  font-size: 18rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.tree-children {
  display: flex;
  flex-direction: row;
  justify-content: center;
  padding-top: 32rpx;
  position: relative;
}

.tree-line-top {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2rpx;
  height: 32rpx;
  background-color: #D1D5DB;
}

.tree-child {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 6rpx;
  position: relative;
}

.tree-child::before {
  content: '';
  position: absolute;
  top: 0;
  right: 50%;
  border-top: 2rpx solid #D1D5DB;
  width: 50%;
  height: 32rpx;
}

.tree-child::after {
  content: '';
  position: absolute;
  top: 0;
  right: auto;
  left: 50%;
  border-top: 2rpx solid #D1D5DB;
  border-left: 2rpx solid #D1D5DB;
  width: 50%;
  height: 32rpx;
}

.tree-child:first-child::after {
  border-radius: 6rpx 0 0 0;
}

.tree-child:last-child::before {
  border-right: 2rpx solid #D1D5DB;
  border-radius: 0 6rpx 0 0;
}

.tree-child:first-child:last-child::before,
.tree-child:first-child:last-child::after {
  display: none;
}

.tree-child:first-child:last-child {
  padding-top: 0;
}
</style>
