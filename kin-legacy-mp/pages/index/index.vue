<template>
  <view class="page-container">
    <view class="content-wrapper">
      <view class="section">
        <view class="section-header">
          <view class="section-line"></view>
          <text class="section-title">我创建的</text>
          <view class="section-line"></view>
        </view>
        
        <view v-if="createdFamilies.length > 0">
          <view 
            class="family-card" 
            v-for="family in createdFamilies" 
            :key="family.id"
            @click="goToTree(family)"
          >
            <view class="family-avatar">
              <text class="family-avatar-text">{{ family.name.charAt(0) }}</text>
            </view>
            <view class="family-info">
              <text class="family-name">{{ family.name }}</text>
              <text class="family-meta">创建者 · {{ family.memberCount || 0 }} 位成员</text>
            </view>
            <view class="family-arrow">
              <text class="arrow-text">→</text>
            </view>
          </view>
        </view>
        
        <view v-else class="empty-card">
          <text class="empty-text">暂无创建的家谱</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <view class="section-line"></view>
          <text class="section-title">我加入的</text>
          <view class="section-line"></view>
        </view>
        
        <view v-if="joinedFamilies.length > 0">
          <view 
            class="family-card" 
            v-for="family in joinedFamilies" 
            :key="family.id"
            @click="goToTree(family)"
          >
            <view class="family-avatar joined">
              <text class="family-avatar-text">{{ family.name.charAt(0) }}</text>
            </view>
            <view class="family-info">
              <text class="family-name">{{ family.name }}</text>
              <text class="family-meta">成员 · {{ family.memberCount || 0 }} 位成员</text>
            </view>
            <view class="family-arrow">
              <text class="arrow-text">→</text>
            </view>
          </view>
        </view>
        
        <view v-else class="empty-card">
          <text class="empty-text">暂无加入的家谱</text>
        </view>
        
        <view class="join-btn" @click="showJoinModal = true">
          <text class="join-btn-text">输入家谱码加入</text>
        </view>
      </view>
      
      <!-- 可拖动的创建按钮 -->
      <view 
        class="drag-btn" 
        :style="{ left: dragLeft + 'px', top: dragTop + 'px' }"
        @touchstart="onDragStart"
        @touchmove="onDragMove"
        @touchend="onDragEnd"
        @click="onDragClick"
      >
        <text class="drag-btn-text">+</text>
      </view>
    </view>

    <!-- 创建家谱弹窗 -->
    <view v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">创建新家谱</text>
          <view class="modal-close" @click="closeCreateModal">
            <text class="close-text">×</text>
          </view>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">家谱名称 <text class="required">*</text></text>
            <input 
              class="form-input" 
              placeholder="例如：李氏家族" 
              v-model="createForm.name"
              placeholder-class="placeholder"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">简介</text>
            <textarea 
              class="form-textarea" 
              placeholder="一句话介绍这个家族..."
              v-model="createForm.description"
              placeholder-class="placeholder"
              :maxlength="200"
            />
            <text class="char-count">{{ createForm.description.length }}/200</text>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeCreateModal">取消</view>
          <view class="btn-confirm" @click="handleCreate">立即创建</view>
        </view>
      </view>
    </view>

    <!-- 加入家谱弹窗 -->
    <view v-if="showJoinModal" class="modal-overlay" @click="closeJoinModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">加入家谱</text>
          <view class="modal-close" @click="closeJoinModal">
            <text class="close-text">×</text>
          </view>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">家谱码 <text class="required">*</text></text>
            <input 
              class="form-input code-input" 
              placeholder="请输入6位数字" 
              v-model="joinForm.code"
              maxlength="6"
              type="number"
              placeholder-class="placeholder"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">您的姓名 <text class="required">*</text></text>
            <input 
              class="form-input" 
              placeholder="真实姓名" 
              v-model="joinForm.name"
              placeholder-class="placeholder"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">与谁的关系 <text class="required">*</text></text>
            <input 
              class="form-input" 
              placeholder="例如：张三的儿子" 
              v-model="joinForm.relationDesc"
              placeholder-class="placeholder"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeJoinModal">取消</view>
          <view class="btn-confirm" @click="handleJoin">提交申请</view>
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
      },
      dragLeft: 0,
      dragTop: 0,
      startLeft: 0,
      startTop: 0,
      startPageX: 0,
      startPageY: 0,
      isDragging: false,
      hasMoved: false,
      windowWidth: 375,
      windowHeight: 667
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

  onLoad() {
    var systemInfo = uni.getSystemInfoSync()
    this.windowWidth = systemInfo.windowWidth
    this.windowHeight = systemInfo.windowHeight
    this.dragLeft = this.windowWidth - 70
    this.dragTop = this.windowHeight - 220
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

    onDragStart(e) {
      this.isDragging = true
      this.hasMoved = false
      this.startLeft = this.dragLeft
      this.startTop = this.dragTop
      this.startPageX = e.touches[0].pageX
      this.startPageY = e.touches[0].pageY
    },

    onDragMove(e) {
      if (!this.isDragging) return
      
      var moveX = e.touches[0].pageX - this.startPageX
      var moveY = e.touches[0].pageY - this.startPageY
      
      if (Math.abs(moveX) > 5 || Math.abs(moveY) > 5) {
        this.hasMoved = true
      }
      
      var newLeft = this.startLeft + moveX
      var newTop = this.startTop + moveY
      
      var btnSize = 60
      newLeft = Math.max(10, Math.min(newLeft, this.windowWidth - btnSize - 10))
      newTop = Math.max(100, Math.min(newTop, this.windowHeight - btnSize - 120))
      
      this.dragLeft = newLeft
      this.dragTop = newTop
    },

    onDragEnd(e) {
      this.isDragging = false
    },

    onDragClick() {
      if (!this.hasMoved) {
        this.showCreateModal = true
      }
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
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFFBF5 0%, #F9FAFB 100%);
}

.content-wrapper {
  padding: 32rpx;
  padding-bottom: 200rpx;
}

.section {
  margin-bottom: 48rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-line {
  flex: 1;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #E5E7EB);
}

.section-title {
  padding: 0 24rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #6B7280;
  letter-spacing: 4rpx;
}

.family-card {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.family-card:active {
  background-color: #F9FAFB;
}

.family-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.family-avatar.joined {
  background: linear-gradient(135deg, #6B7280 0%, #9CA3AF 100%);
}

.family-avatar-text {
  font-size: 44rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 4rpx;
}

.family-info {
  flex: 1;
  min-width: 0;
}

.family-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.family-meta {
  font-size: 26rpx;
  color: #9CA3AF;
  margin-top: 8rpx;
  display: block;
}

.family-arrow {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.arrow-text {
  font-size: 32rpx;
  color: #D1D5DB;
}

.empty-card {
  background-color: #FFFFFF;
  border: 2rpx dashed #D1D5DB;
  border-radius: 20rpx;
  padding: 48rpx;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.join-btn {
  margin-top: 24rpx;
  background-color: #FFFFFF;
  border: 2rpx solid #8B4513;
  border-radius: 44rpx;
  padding: 24rpx;
  text-align: center;
}

.join-btn:active {
  background-color: #FFFBF5;
}

.join-btn-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #8B4513;
}

/* 可拖动按钮 */
.drag-btn {
  position: fixed;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(139, 69, 19, 0.35);
  z-index: 100;
}

.drag-btn:active {
  transform: scale(1.05);
}

.drag-btn-text {
  font-size: 64rpx;
  color: #FFFFFF;
  font-weight: 300;
  line-height: 1;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-box {
  width: 85%;
  max-width: 600rpx;
  background-color: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #F3F4F6;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F2937;
}

.modal-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-text {
  font-size: 48rpx;
  color: #9CA3AF;
  line-height: 1;
}

.modal-body {
  padding: 32rpx;
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

.required {
  color: #DC2626;
}

.form-input {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #1F2937;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  height: 180rpx;
  background-color: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 30rpx;
  color: #1F2937;
  box-sizing: border-box;
}

.code-input {
  text-align: center;
  letter-spacing: 12rpx;
  font-size: 36rpx;
  font-weight: 600;
}

.placeholder {
  color: #9CA3AF;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-top: 8rpx;
}

.modal-footer {
  display: flex;
  padding: 20rpx 32rpx 32rpx;
  gap: 24rpx;
}

.btn-cancel {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 44rpx;
  background-color: #F3F4F6;
  color: #6B7280;
}

.btn-confirm {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  color: #FFFFFF;
  box-shadow: 0 4rpx 16rpx rgba(139, 69, 19, 0.25);
}

.btn-cancel:active, .btn-confirm:active {
  opacity: 0.9;
}
</style>
