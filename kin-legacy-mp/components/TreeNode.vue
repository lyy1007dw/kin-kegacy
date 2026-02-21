<template>
  <view class="jpu-tree-node-wrapper">
    <view class="jpu-node-row">
      <view 
        v-if="hasChildren" 
        class="jpu-tree-toggle"
        @click.stop="toggleExpand"
      >
        <text class="jpu-toggle-text">{{ expanded ? '−' : '+' }}</text>
      </view>
      
      <view 
        class="jpu-member-card"
        :class="cardClass"
        @click="handleClick"
      >
        <view class="jpu-gender-tag" :class="genderClass">
          <text class="jpu-gender-text">{{ genderText }}</text>
        </view>
        
        <view class="jpu-member-info">
          <text class="jpu-member-name">{{ node.name }}</text>
          <text class="jpu-member-generation">第 {{ node.generation || 1 }} 世</text>
        </view>
        
        <view v-if="isCurrentUser" class="jpu-me-tag">
          <text class="jpu-me-text">我</text>
        </view>
      </view>
    </view>
    
    <view 
      v-if="hasChildren" 
      class="jpu-tree-children"
      :class="{ 'jpu-hidden': !expanded }"
    >
      <view 
        class="jpu-tree-node-item"
        v-for="child in node.children"
        :key="child.id"
      >
        <TreeNode 
          :node="child" 
          :currentUserId="currentUserId"
          :defaultExpanded="defaultExpanded"
          @click="handleChildClick"
        />
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
    },
    defaultExpanded: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      expanded: true
    }
  },
  created() {
    this.expanded = this.defaultExpanded
  },
  computed: {
    isCurrentUser() {
      return this.node.userId === this.currentUserId
    },
    genderText() {
      return this.node.gender === 'male' ? '男' : '女'
    },
    genderClass() {
      return this.node.gender === 'male' ? 'jpu-gender-male' : 'jpu-gender-female'
    },
    hasChildren() {
      return this.node.children && this.node.children.length > 0
    },
    cardClass() {
      var classes = []
      if (this.node.isCreator) classes.push('jpu-card-creator')
      if (this.isCurrentUser) classes.push('jpu-card-me')
      return classes.join(' ')
    }
  },
  methods: {
    toggleExpand() {
      this.expanded = !this.expanded
    },
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
.jpu-tree-node-wrapper {
  position: relative;
}

.jpu-node-row {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.jpu-tree-toggle {
  position: absolute;
  left: -20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 40rpx;
  height: 40rpx;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
}

.jpu-tree-toggle:active {
  background-color: var(--theme-bg);
}

.jpu-toggle-text {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--theme-primary);
  line-height: 1;
}

.jpu-member-card {
  display: inline-flex;
  align-items: center;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 20rpx 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  margin-left: 32rpx;
}

.jpu-member-card:active {
  transform: scale(0.98);
}

.jpu-card-creator {
  background: linear-gradient(180deg, #FFFBF5 0%, var(--theme-card) 100%);
  border-color: var(--theme-primary);
}

.jpu-card-me {
  border-width: 3rpx;
  border-color: #3B82F6;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2);
}

.jpu-gender-tag {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.jpu-gender-male {
  background-color: #EAF2FF;
  border: 2rpx solid #1565C0;
}

.jpu-gender-female {
  background-color: #FDF2F1;
  border: 2rpx solid var(--theme-primary);
}

.jpu-gender-text {
  font-size: 24rpx;
  font-weight: bold;
}

.jpu-gender-male .jpu-gender-text {
  color: #1565C0;
}

.jpu-gender-female .jpu-gender-text {
  color: var(--theme-primary);
}

.jpu-member-info {
  display: flex;
  flex-direction: column;
}

.jpu-member-name {
  font-size: 30rpx;
  font-weight: bold;
  color: var(--theme-text);
  letter-spacing: 6rpx;
}

.jpu-member-generation {
  font-size: 20rpx;
  color: var(--theme-primary);
  background-color: #FDF2F1;
  border: 1rpx solid #E6B0AA;
  border-radius: 4rpx;
  padding: 2rpx 8rpx;
  margin-top: 6rpx;
  display: inline-block;
  width: fit-content;
}

.jpu-me-tag {
  position: absolute;
  top: -12rpx;
  right: -12rpx;
  background-color: #3B82F6;
  border: 3rpx solid var(--theme-card);
  border-radius: 50%;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.jpu-me-text {
  font-size: 20rpx;
  font-weight: bold;
  color: #FFFFFF;
}

.jpu-tree-children {
  position: relative;
  padding-left: 56rpx;
  margin-left: 24rpx;
  margin-top: 32rpx;
}

.jpu-tree-children::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  border-left: 2rpx solid var(--theme-border);
}

.jpu-tree-node-item {
  position: relative;
  margin-top: 32rpx;
}

.jpu-tree-node-item:first-child {
  margin-top: 0;
}

.jpu-tree-node-item::before {
  content: '';
  position: absolute;
  top: 44rpx;
  left: -56rpx;
  width: 56rpx;
  border-top: 2rpx solid var(--theme-border);
}

.jpu-tree-node-item:last-child > .jpu-tree-children::before {
  display: none;
}

.jpu-hidden {
  display: none;
}
</style>
