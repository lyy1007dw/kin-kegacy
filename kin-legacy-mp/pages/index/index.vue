<template>
  <view class="jpu-page-container">
    <view class="jpu-content-wrapper">
      <!-- 快捷操作区 -->
      <view class="jpu-quick-actions">
        <view class="jpu-quick-btn jpu-btn-primary" @click="showCreateModal = true">
          <text class="jpu-quick-btn-icon">⛩️</text>
          <text class="jpu-quick-btn-text">敬修家谱</text>
        </view>
        <view class="jpu-quick-btn jpu-btn-outline-alt" @click="showJoinModal = true">
          <text class="jpu-quick-btn-icon">📜</text>
          <text class="jpu-quick-btn-text">寻根加入</text>
        </view>
      </view>

      <!-- 我管理的家谱 -->
      <view class="jpu-section">
        <view class="jpu-section-title">
          <view class="jpu-section-line"></view>
          <text>我管理的家谱</text>
        </view>
        
        <view v-if="createdFamilies.length > 0">
          <view 
            class="jpu-family-card" 
            v-for="family in createdFamilies" 
            :key="family.id"
            @click="goToTree(family)"
          >
            <view class="jpu-family-avatar">
              <text class="jpu-family-avatar-text">{{ family.name.charAt(0) }}</text>
            </view>
            <view class="jpu-family-info">
              <text class="jpu-family-name">{{ family.name }}</text>
              <text class="jpu-family-meta">已录 {{ family.memberCount || 0 }} 人 · 由我修撰</text>
            </view>
            <text class="jpu-family-arrow">❯</text>
          </view>
        </view>
        
        <view v-else class="jpu-empty-card">
          <text class="jpu-empty-text">暂无管理的家谱</text>
        </view>
      </view>

      <!-- 我加入的家谱 -->
      <view class="jpu-section">
        <view class="jpu-section-title">
          <view class="jpu-section-line"></view>
          <text>我加入的家谱</text>
        </view>
        
        <view v-if="joinedFamilies.length > 0">
          <view 
            class="jpu-family-card" 
            v-for="family in joinedFamilies" 
            :key="family.id"
            @click="goToTree(family)"
          >
            <view class="jpu-family-avatar jpu-joined">
              <text class="jpu-family-avatar-text">{{ family.name.charAt(0) }}</text>
            </view>
            <view class="jpu-family-info">
              <text class="jpu-family-name">{{ family.name }}</text>
              <text class="jpu-family-meta">已录 {{ family.memberCount || 0 }} 人</text>
            </view>
            <text class="jpu-family-arrow">❯</text>
          </view>
        </view>
        
        <view v-else class="jpu-empty-card">
          <text class="jpu-empty-text">暂无加入的家谱</text>
        </view>
      </view>
    </view>

    <!-- 创建家谱弹窗 -->
    <view v-if="showCreateModal" class="jpu-modal-overlay jpu-open" @click="closeCreateModal">
      <view class="jpu-modal-center" @click.stop>
        <view class="jpu-modal-header">
          <text class="jpu-modal-title">敬修新谱</text>
          <text class="jpu-modal-close" @click="closeCreateModal">×</text>
        </view>
        
        <view class="jpu-modal-body">
          <view class="jpu-form-group">
            <text class="jpu-form-label">家族堂号/谱名</text>
            <input 
              class="jpu-form-input" 
              placeholder="例如：赵氏宗谱" 
              v-model="createForm.name"
              placeholder-class="jpu-placeholder"
            />
          </view>
          
          <view class="jpu-form-group">
            <text class="jpu-form-label">始祖尊讳</text>
            <input 
              class="jpu-form-input" 
              placeholder="输入第一世祖先姓名" 
              v-model="createForm.description"
              placeholder-class="jpu-placeholder"
            />
          </view>

          <view class="jpu-form-group">
            <text class="jpu-form-label">始祖性别</text>
            <view class="jpu-radio-group">
              <label class="jpu-radio-item">
                <radio class="jpu-radio" value="male" checked />
                <text class="jpu-radio-text">男</text>
              </label>
              <label class="jpu-radio-item">
                <radio class="jpu-radio" value="female" />
                <text class="jpu-radio-text">女</text>
              </label>
            </view>
          </view>
        </view>
        
        <view class="jpu-modal-footer">
          <view class="jpu-btn-gray" @click="closeCreateModal">
            <text>作罢</text>
          </view>
          <view class="jpu-btn-primary" @click="handleCreate">
            <text>落笔确认</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加入家谱弹窗 -->
    <view v-if="showJoinModal" class="jpu-modal-overlay jpu-open" @click="closeJoinModal">
      <view class="jpu-modal-center" @click.stop>
        <view class="jpu-modal-header">
          <text class="jpu-modal-title">输入字号加入</text>
          <text class="jpu-modal-close" @click="closeJoinModal">×</text>
        </view>
        
        <view class="jpu-modal-body">
          <view class="jpu-form-group">
            <text class="jpu-form-label">请输入6位宗亲邀请码</text>
            <input 
              class="jpu-form-input jpu-code-input" 
              placeholder="输入邀请码" 
              v-model="joinForm.code"
              maxlength="6"
              placeholder-class="jpu-placeholder"
            />
          </view>
          
          <view class="jpu-form-group">
            <text class="jpu-form-label">您的姓名</text>
            <input 
              class="jpu-form-input" 
              placeholder="真实姓名" 
              v-model="joinForm.name"
              placeholder-class="jpu-placeholder"
            />
          </view>
          
          <view class="jpu-form-group">
            <text class="jpu-form-label">与谁的关系</text>
            <input 
              class="jpu-form-input" 
              placeholder="例如：张三的儿子" 
              v-model="joinForm.relationDesc"
              placeholder-class="jpu-placeholder"
            />
          </view>
        </view>
        
        <view class="jpu-modal-footer">
          <view class="jpu-btn-gray" @click="closeJoinModal">
            <text>作罢</text>
          </view>
          <view class="jpu-btn-primary" @click="handleJoin">
            <text>落笔确认</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import api from '../../utils/api'

export default {
  data() {
    return {
      showCreateModal: false,
      showJoinModal: false,
      createForm: {
        name: '',
        description: ''
      },
      joinForm: {
        code: '',
        name: '',
        relationDesc: ''
      }
    }
  },

  computed: {
    ...mapState(['myFamilies', 'userInfo']),
    createdFamilies() {
      if (!this.userInfo || !this.myFamilies) return []
      var self = this
      return this.myFamilies.filter(function(f) { return f.creatorId === self.userInfo.id })
    },
    joinedFamilies() {
      if (!this.userInfo || !this.myFamilies) return []
      var self = this
      return this.myFamilies.filter(function(f) { return f.creatorId !== self.userInfo.id })
    }
  },

  onShow() {
    this.fetchMyFamilies()
  },

  methods: {
    ...mapActions(['fetchMyFamilies', 'setCurrentFamily']),

    closeCreateModal() {
      this.showCreateModal = false
    },

    closeJoinModal() {
      this.showJoinModal = false
    },

    goToTree(family) {
      this.setCurrentFamily(family)
      uni.switchTab({
        url: '/pages/tree/tree'
      })
    },

    async handleCreate() {
      if (!this.createForm.name.trim()) {
        uni.showToast({ title: '请输入家谱名称', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '创建中...' })
        await api.family.create(this.createForm)
        uni.hideLoading()
        uni.showToast({ title: '家谱创建成功', icon: 'success' })
        this.showCreateModal = false
        this.createForm = { name: '', description: '' }
        this.fetchMyFamilies()
      } catch (error) {
        uni.hideLoading()
      }
    },

    async handleJoin() {
      if (!this.joinForm.code.trim() || this.joinForm.code.length !== 6) {
        uni.showToast({ title: '请输入6位家谱码', icon: 'none' })
        return
      }
      if (!this.joinForm.name.trim()) {
        uni.showToast({ title: '请输入您的姓名', icon: 'none' })
        return
      }
      if (!this.joinForm.relationDesc.trim()) {
        uni.showToast({ title: '请输入关系描述', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        await api.family.join(this.joinForm)
        uni.hideLoading()
        uni.showToast({ title: '申请已提交', icon: 'success' })
        this.showJoinModal = false
        this.joinForm = { code: '', name: '', relationDesc: '' }
      } catch (error) {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
.jpu-page-container {
  min-height: 100vh;
  background-color: var(--theme-bg);
}

.jpu-content-wrapper {
  padding: 32rpx;
  padding-bottom: 200rpx;
}

/* 快捷操作区 */
.jpu-quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32rpx;
  margin-bottom: 48rpx;
}

.jpu-quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  border-radius: 12rpx;
  min-height: 160rpx;
}

.jpu-quick-btn-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.jpu-quick-btn-text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
}

.jpu-btn-primary {
  background-color: var(--theme-primary);
  border: 2rpx solid #722023;
}

.jpu-btn-primary .jpu-quick-btn-text {
  color: #FFFFFF;
}

.jpu-btn-outline-alt {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-primary);
}

.jpu-btn-outline-alt .jpu-quick-btn-text {
  color: var(--theme-primary);
}

.jpu-btn-outline-alt:active {
  background-color: var(--theme-bg);
}

/* Section */
.jpu-section {
  margin-bottom: 48rpx;
}

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

/* 家谱卡片 */
.jpu-family-card {
  display: flex;
  align-items: center;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.jpu-family-card:active {
  background-color: var(--theme-bg);
}

.jpu-family-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 8rpx;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.jpu-family-avatar.jpu-joined {
  background-color: #F5EBE9;
  border-color: #E6B0AA;
}

.jpu-family-avatar-text {
  font-size: 44rpx;
  font-weight: bold;
  color: var(--theme-primary);
  letter-spacing: 4rpx;
}

.jpu-family-info {
  flex: 1;
  min-width: 0;
}

.jpu-family-name {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--theme-text);
  letter-spacing: 6rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.jpu-family-meta {
  font-size: 24rpx;
  color: #8D6E63;
  margin-top: 8rpx;
  display: block;
}

.jpu-family-arrow {
  color: var(--theme-border);
  font-size: 32rpx;
  flex-shrink: 0;
}

/* 空状态 */
.jpu-empty-card {
  background-color: var(--theme-card);
  border: 2rpx dashed var(--theme-border);
  border-radius: 12rpx;
  padding: 48rpx;
  text-align: center;
}

.jpu-empty-text {
  font-size: 28rpx;
  color: #8D6E63;
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
  letter-spacing: 6rpx;
}

.jpu-modal-close {
  font-size: 48rpx;
  color: #8D6E63;
  line-height: 1;
}

.jpu-modal-body {
  padding: 32rpx;
}

.jpu-modal-footer {
  display: flex;
  padding: 24rpx 32rpx 32rpx;
  gap: 24rpx;
}

/* 表单 */
.jpu-form-group {
  margin-bottom: 32rpx;
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
  font-family: 'Noto Serif SC', 'Songti SC', 'SimSun', STSong, serif;
}

.jpu-code-input {
  text-align: center;
  font-family: monospace;
  font-size: 36rpx;
  font-weight: bold;
  letter-spacing: 12rpx;
}

.jpu-placeholder {
  color: #9CA3AF;
}

.jpu-radio-group {
  display: flex;
  gap: 48rpx;
}

.jpu-radio-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.jpu-radio {
  accent-color: var(--theme-primary);
}

.jpu-radio-text {
  font-size: 28rpx;
  color: var(--theme-text);
}

/* 按钮 */
.jpu-btn-gray {
  flex: 1;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 24rpx;
  text-align: center;
}

.jpu-btn-gray text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
  color: var(--theme-text);
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
