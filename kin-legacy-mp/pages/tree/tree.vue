<template>
  <view class="page-container">
    <!-- 未选择家谱提示 -->
    <view v-if="!currentFamily" class="empty-state">
      <view class="empty-icon-wrap">
        <text class="empty-icon-text">谱</text>
      </view>
      <text class="empty-text">请先选择一个家谱</text>
      <view class="empty-btn" @click="goToIndex">返回首页选择</view>
    </view>

    <block v-else>
      <view class="stats-bar">
        <text class="stats-text">共 {{ members.length }} 人</text>
        <text class="stats-hint">点击成员查看详情</text>
      </view>

      <scroll-view scroll-x class="tree-scroll" v-if="treeData.length > 0">
        <view class="tree-container">
          <view class="tree-node" v-for="(node, index) in treeData" :key="node.id">
            <TreeNode :node="node" :currentUserId="currentUserId" @click="showMemberDetail" />
          </view>
        </view>
      </scroll-view>

      <view v-else class="empty-state">
        <view class="empty-icon-wrap">
          <text class="empty-icon-text">谱</text>
        </view>
        <text class="empty-text">暂无成员数据</text>
        <view class="empty-btn">添加第一位成员</view>
      </view>

      <!-- 可拖动的邀请按钮 -->
      <view 
        class="drag-btn" 
        :style="{ left: dragLeft + 'px', top: dragTop + 'px' }"
        @touchstart="onDragStart"
        @touchmove="onDragMove"
        @touchend="onDragEnd"
        @click="onDragClick"
      >
        <text class="drag-btn-text">邀请</text>
      </view>
    </block>

    <!-- 邀请弹窗 -->
    <view v-if="showInviteModal" class="modal-overlay" @click="showInviteModal = false">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">邀请家人加入</text>
          <view class="modal-close" @click="showInviteModal = false">
            <text class="close-text">×</text>
          </view>
        </view>
        
        <view class="qr-container">
          <image class="qr-image" :src="qrCodeUrl" mode="aspectFit" />
        </view>

        <view class="code-display">
          <text class="code-label">家谱码</text>
          <text class="code-value">{{ currentFamily.code }}</text>
          <view class="copy-btn" @click="copyCode">
            <text class="copy-text">复制</text>
          </view>
        </view>

        <button class="share-btn" open-type="share">分享邀请</button>
      </view>
    </view>

    <!-- 成员详情弹窗 -->
    <view v-if="showMemberModal" class="modal-overlay" @click="showMemberModal = false">
      <view class="modal-box member-modal" @click.stop>
        <view class="member-header">
          <view class="member-avatar" :class="{ 'is-me': isCurrentUser }">
            <text class="member-avatar-text">{{ selectedMember.name ? selectedMember.name.charAt(0) : '?' }}</text>
          </view>
          <text class="member-name">{{ selectedMember.name }}</text>
          <view class="member-tags">
            <view class="member-tag">
              <text class="tag-text">{{ genderText }}</text>
            </view>
            <view class="member-tag">
              <text class="tag-text">{{ selectedMember.isCreator ? '创建者' : '成员' }}</text>
            </view>
            <view v-if="isCurrentUser" class="member-tag me-tag">
              <text class="tag-text">我</text>
            </view>
          </view>
        </view>

        <view class="member-info-list">
          <view class="info-item">
            <text class="info-label">出生日期</text>
            <text class="info-value">{{ selectedMember.birthDate || '未设置' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">简介</text>
            <text class="info-value bio-text">{{ selectedMember.bio || '暂无简介' }}</text>
          </view>
        </view>

        <view class="member-actions">
          <view v-if="!isSelf" class="btn-outline" @click="showEditRequest">申请修改信息</view>
          <view class="btn-gray" @click="showMemberModal = false">关闭</view>
        </view>
      </view>
    </view>

    <!-- 修改申请弹窗 -->
    <view v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">申请修改信息</text>
          <view class="modal-close" @click="showEditModal = false">
            <text class="close-text">×</text>
          </view>
        </view>
        <text class="modal-subtitle">修改提交后需由创建者审批</text>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">修改字段 <text class="required">*</text></text>
            <picker :value="editFieldIndex" :range="editFields" range-key="label" @change="onEditFieldChange">
              <view class="picker-field">
                <text class="picker-value">{{ editFields[editFieldIndex].label }}</text>
                <text class="picker-arrow">∨</text>
              </view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="form-label">新内容 <text class="required">*</text></text>
            <input 
              v-if="editFields[editFieldIndex].value !== 'bio'"
              class="form-input" 
              :placeholder="'请输入新的' + editFields[editFieldIndex].label" 
              v-model="editForm.newValue"
              placeholder-class="placeholder"
            />
            <textarea 
              v-else
              class="form-textarea" 
              placeholder="请输入新的简介"
              v-model="editForm.newValue"
              placeholder-class="placeholder"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="btn-cancel" @click="showEditModal = false">取消</view>
          <view class="btn-confirm" @click="submitEditRequest">提交申请</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'
import api from '../../utils/api'
import TreeNode from '../../components/TreeNode.vue'

export default {
  components: {
    TreeNode
  },

  data() {
    return {
      showInviteModal: false,
      showMemberModal: false,
      showEditModal: false,
      members: [],
      treeData: [],
      selectedMember: {},
      editFields: [
        { label: '姓名', value: 'name' },
        { label: '出生日期', value: 'birthDate' },
        { label: '简介', value: 'bio' }
      ],
      editFieldIndex: 0,
      editForm: {
        fieldName: 'name',
        newValue: ''
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
    ...mapState(['currentFamily', 'userInfo']),
    currentUserId() {
      return this.userInfo && this.userInfo.id
    },
    qrCodeUrl() {
      var code = this.currentFamily && this.currentFamily.code || '000000'
      return 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=FamilyCode' + code
    },
    genderText() {
      return this.selectedMember.gender === 'male' ? '男' : '女'
    },
    isSelf() {
      return this.selectedMember.userId === this.currentUserId
    },
    isCurrentUser() {
      return this.selectedMember.userId === this.currentUserId
    }
  },

  onLoad() {
    var systemInfo = uni.getSystemInfoSync()
    this.windowWidth = systemInfo.windowWidth
    this.windowHeight = systemInfo.windowHeight
    this.dragLeft = this.windowWidth - 80
    this.dragTop = this.windowHeight - 220
  },

  onShow() {
    if (this.currentFamily) {
      this.loadTreeData()
    }
  },

  onShareAppMessage() {
    return {
      title: '邀请加入' + (this.currentFamily && this.currentFamily.name || '家谱'),
      path: '/pages/index/index?code=' + (this.currentFamily && this.currentFamily.code || '')
    }
  },

  methods: {
    goToIndex() {
      uni.switchTab({ url: '/pages/index/index' })
    },

    async loadTreeData() {
      try {
        var res = await api.member.getTree(this.currentFamily.id)
        this.treeData = res || []
        
        var memberRes = await api.member.getList(this.currentFamily.id)
        this.members = memberRes || []
      } catch (error) {
        console.error('加载树形数据失败', error)
      }
    },

    showMemberDetail(member) {
      this.selectedMember = member
      this.showMemberModal = true
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
      
      var btnSize = 70
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
        this.showInviteModal = true
      }
    },

    copyCode() {
      uni.setClipboardData({
        data: this.currentFamily.code,
        success: function() {
          uni.showToast({ title: '已复制家谱码', icon: 'success' })
        }
      })
    },

    onEditFieldChange(e) {
      this.editFieldIndex = e.detail.value
      this.editForm.fieldName = this.editFields[this.editFieldIndex].value
      this.editForm.newValue = ''
    },

    showEditRequest() {
      this.showMemberModal = false
      this.editFieldIndex = 0
      this.editForm = {
        fieldName: 'name',
        newValue: ''
      }
      this.showEditModal = true
    },

    async submitEditRequest() {
      if (!this.editForm.newValue.trim()) {
        uni.showToast({ title: '请输入新内容', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        await api.member.applyEdit(this.currentFamily.id, this.selectedMember.id, {
          fieldName: this.editForm.fieldName,
          oldValue: this.selectedMember[this.editForm.fieldName] || '',
          newValue: this.editForm.newValue
        })
        uni.hideLoading()
        uni.showToast({ title: '申请已提交', icon: 'success' })
        this.showEditModal = false
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
  display: flex;
  flex-direction: column;
}

.stats-bar {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  background-color: #FFFFFF;
  border-bottom: 2rpx solid #F3F4F6;
}

.stats-text, .stats-hint {
  font-size: 26rpx;
  color: #6B7280;
}

.tree-scroll {
  flex: 1;
  white-space: nowrap;
}

.tree-container {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 60rpx;
  min-width: 100%;
}

.tree-node {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
}

.empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(139, 69, 19, 0.2);
}

.empty-icon-text {
  font-size: 72rpx;
  color: #FFFFFF;
  font-weight: 600;
  letter-spacing: 8rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: 500;
  padding: 24rpx 48rpx;
  border-radius: 44rpx;
}

.drag-btn {
  position: fixed;
  width: 140rpx;
  height: 140rpx;
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
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
  letter-spacing: 4rpx;
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
  max-height: 80vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
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

.modal-subtitle {
  display: block;
  font-size: 26rpx;
  color: #9CA3AF;
  text-align: center;
  padding: 16rpx 32rpx 0;
}

.modal-body {
  padding: 24rpx 32rpx;
}

.qr-container {
  padding: 32rpx;
  display: flex;
  justify-content: center;
}

.qr-image {
  width: 320rpx;
  height: 320rpx;
  background-color: #F9FAFB;
  border-radius: 16rpx;
}

.code-display {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 32rpx 24rpx;
  padding: 24rpx;
  background-color: #F9FAFB;
  border-radius: 16rpx;
}

.code-label {
  font-size: 28rpx;
  color: #6B7280;
  margin-right: 16rpx;
}

.code-value {
  font-size: 44rpx;
  font-weight: 600;
  color: #8B4513;
  letter-spacing: 8rpx;
  margin-right: 20rpx;
}

.copy-btn {
  padding: 12rpx 24rpx;
  background-color: #FFFFFF;
  border-radius: 20rpx;
  border: 2rpx solid #E5E7EB;
}

.copy-text {
  font-size: 26rpx;
  color: #6B7280;
}

.share-btn {
  margin: 0 32rpx 32rpx;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  color: #FFFFFF;
  font-weight: 500;
  border-radius: 44rpx;
  font-size: 30rpx;
  height: 88rpx;
  line-height: 88rpx;
}

/* 成员详情 */
.member-modal {
  padding: 0;
}

.member-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 32rpx;
  background: linear-gradient(180deg, #FFFBF5 0%, #FFFFFF 100%);
}

.member-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.member-avatar.is-me {
  border: 6rpx solid #3B82F6;
  box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.2);
}

.member-avatar-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 4rpx;
}

.member-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 16rpx;
}

.member-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  justify-content: center;
}

.member-tag {
  background-color: #F3F4F6;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.me-tag {
  background-color: #3B82F6;
}

.me-tag .tag-text {
  color: #FFFFFF;
}

.tag-text {
  font-size: 24rpx;
  color: #6B7280;
}

.member-info-list {
  padding: 0 32rpx;
}

.info-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F3F4F6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #9CA3AF;
  width: 160rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2937;
  flex: 1;
  text-align: right;
}

.bio-text {
  word-break: break-all;
}

.member-actions {
  padding: 24rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.btn-outline {
  border: 3rpx solid #8B4513;
  color: #8B4513;
  font-weight: 500;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  text-align: center;
  font-size: 30rpx;
}

.btn-outline:active {
  background-color: #FFFBF5;
}

.btn-gray {
  background-color: #F3F4F6;
  color: #4B5563;
  font-weight: 500;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  text-align: center;
  font-size: 30rpx;
}

.btn-gray:active {
  background-color: #E5E7EB;
}

/* 表单 */
.form-item {
  margin-bottom: 24rpx;
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
  height: 160rpx;
  background-color: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 30rpx;
  color: #1F2937;
  box-sizing: border-box;
}

.placeholder {
  color: #9CA3AF;
}

.picker-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 12rpx;
  padding: 0 24rpx;
  height: 88rpx;
}

.picker-value {
  font-size: 30rpx;
  color: #1F2937;
}

.picker-arrow {
  font-size: 24rpx;
  color: #9CA3AF;
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
