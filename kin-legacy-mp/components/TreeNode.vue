<template>
  <view class="tree-node-wrapper">
    <view class="tree-node-item">
      <view class="node-container">
        <view v-if="node.children && node.children.length > 0" class="toggle-btn" @click="toggleNode">
          <text>{{ expanded ? '−' : '+' }}</text>
        </view>
        <view class="member-card" :class="{ 'is-me': node.currentUser }" @click="onClick">
          <view class="gender-badge" :class="node.gender">
            <text>{{ node.gender === 'male' ? '男' : '女' }}</text>
          </view>
          <view class="member-info">
            <text class="member-name">{{ node.name }}</text>
            <view class="generation-tag">
              <text>第 {{ node.generation || 1 }} 世</text>
            </view>
          </view>
          <view v-if="node.currentUser" class="current-user-badge">
            <text>{{ node.currentUserLabel || '我' }}</text>
          </view>
        </view>
      </view>
    </view>
    <view v-if="node.children && node.children.length > 0 && expanded" class="tree-children">
      <template v-for="child in node.children" :key="child.id">
        <TreeNode :node="child" @click="$emit('nodeClick', $event)" />
      </template>
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
    }
  },
  data() {
    return {
      expanded: true
    }
  },
  created() {
    const app = getApp()
    if (app && app.globalData && app.globalData.expandedIds) {
      this.expanded = app.globalData.expandedIds[this.node.id] !== false
    }
  },
  methods: {
    toggleNode() {
      this.expanded = !this.expanded
      const app = getApp()
      if (app && app.globalData) {
        if (!app.globalData.expandedIds) {
          app.globalData.expandedIds = {}
        }
        app.globalData.expandedIds[this.node.id] = this.expanded
      }
      this.$emit('toggle', this.node.id, this.expanded)
    },
    onClick() {
      this.$emit('click', this.node)
    }
  }
}
</script>

<style scoped>
.tree-node-wrapper {
  display: flex;
  flex-direction: column;
}

.tree-node-item {
  display: flex;
  flex-direction: column;
}

.node-container {
  display: flex;
  align-items: center;
}

.toggle-btn {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
  font-weight: bold;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
  background-color: var(--theme-card);
  border: 1rpx solid #D4C9BD;
}

.toggle-btn text {
  color: #8E292C;
}

.member-card {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  position: relative;
  margin-left: 16rpx;
  min-width: 280rpx;
  box-shadow: 0 4rpx 12rpx rgba(62, 42, 35, 0.08);
}

.member-card.is-me {
  background: linear-gradient(145deg, #FFF8F0 0%, #FEF3E2 100%);
  border: 2rpx solid #E6B0AA;
}

.member-card:not(.is-me) {
  background: linear-gradient(145deg, #FFFFFF 0%, #FBF9F6 100%);
  border: 2rpx solid #D4C9BD;
}

.gender-badge {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4rpx;
  margin-right: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  flex-shrink: 0;
}

.gender-badge.male {
  background-color: #E3F2FD;
  color: #1565C0;
  border: 2rpx solid #1565C0;
}

.gender-badge.female {
  background-color: #FDF2F1;
  color: var(--theme-primary);
  border: 2rpx solid var(--theme-primary);
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #3E2A23;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.generation-tag {
  display: inline-block;
  margin-top: 6rpx;
}

.generation-tag text {
  font-size: 20rpx;
  color: #8E292C;
  background: #FDF2F1;
  border: 1rpx solid #E6B0AA;
  border-radius: 4rpx;
  padding: 2rpx 10rpx;
}

.current-user-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border: 3rpx solid #FFFFFF;
  border-radius: 50%;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #fff;
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.4);
}

.tree-children {
  position: relative;
  padding-left: 56rpx;
  margin-left: 24rpx;
}

.tree-children::before {
  content: '';
  position: absolute;
  left: 20rpx;
  top: 0;
  bottom: 24rpx;
  width: 2rpx;
  background: #D4C9BD;
}
</style>
