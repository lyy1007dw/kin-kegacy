<template>
  <view class="jpu-page-container">
    <view v-if="!currentFamily" class="jpu-empty-state">
      <view class="jpu-empty-icon-wrap">
        <text class="jpu-empty-icon-text">谱</text>
      </view>
      <text class="jpu-empty-text">请先选择一个家谱</text>
      <view class="jpu-empty-btn" @click="goToIndex">返回首页选择</view>
    </view>

    <block v-else>
      <!-- 邀请码信息区 -->
      <view class="jpu-invite-card">
        <view>
          <text class="jpu-invite-label">家族邀请码</text>
          <text class="jpu-invite-code">{{ currentFamily.code }}</text>
        </view>
        <view class="jpu-copy-btn" @click="copyCode">
          <text class="jpu-copy-btn-text">复制分享</text>
        </view>
      </view>

      <!-- 家族世系图标题 -->
      <view class="jpu-section-title">
        <view class="jpu-section-line"></view>
        <text>家族世系图</text>
      </view>

      <!-- 家谱树容器 -->
      <scroll-view scroll-x class="jpu-tree-scroll" v-if="treeData.length > 0">
        <view class="jpu-tree-card">
          <view class="jpu-tree-container">
            <view class="jpu-tree-node-item" v-for="(node, index) in treeData" :key="node.id" :style="{ marginTop: index === 0 ? '0' : '32rpx' }">
              <TreeNode :node="node" :currentUserId="currentUserId" @click="showMemberDetail" />
            </view>
          </view>
        </view>
      </scroll-view>

      <view v-else class="jpu-empty-state">
        <view class="jpu-empty-icon-wrap">
          <text class="jpu-empty-icon-text">谱</text>
        </view>
        <text class="jpu-empty-text">暂无成员数据</text>
      </view>
    </block>

    <!-- 遮罩层 -->
    <view 
      class="jpu-modal-overlay" 
      :class="{ 'jpu-open': showDrawer || showEditModal || showInviteModal }"
      @click="closeAllModals"
    ></view>

    <!-- 成员操作抽屉 -->
    <view class="jpu-drawer" :class="{ 'jpu-open': showDrawer }">
      <view class="jpu-drawer-handle"></view>
      <view class="jpu-drawer-header">
        <text class="jpu-drawer-title">{{ selectedMember.name }}</text>
        <text class="jpu-drawer-subtitle">第 {{ selectedMember.generation || 1 }} 世成员</text>
      </view>
      <view class="jpu-drawer-actions">
        <view class="jpu-drawer-btn jpu-btn-outline" @click="openForm('add-child')">
          <text class="jpu-drawer-btn-text">录入子嗣 (下一世)</text>
          <text class="jpu-drawer-btn-arrow">↳</text>
        </view>
        <view class="jpu-drawer-btn jpu-btn-outline" @click="openForm('add-parent')">
          <text class="jpu-drawer-btn-text">追溯先祖 (上一世)</text>
          <text class="jpu-drawer-btn-arrow">↰</text>
        </view>
        <view class="jpu-drawer-btn jpu-btn-danger" @click="openForm('edit-member')">
          <text class="jpu-drawer-btn-text">修正小传</text>
          <text class="jpu-drawer-btn-arrow">✎</text>
        </view>
      </view>
    </view>

    <!-- 表单弹窗 -->
    <view v-if="showEditModal" class="jpu-modal-center">
      <view class="jpu-modal-header">
        <text class="jpu-modal-title">{{ formTitle }}</text>
        <text class="jpu-modal-close" @click="closeAllModals">×</text>
      </view>
      <view class="jpu-modal-body">
        <view class="jpu-form-group">
          <text class="jpu-form-label">尊讳/姓名</text>
          <input 
            class="jpu-form-input" 
            :value="editForm.name"
            @input="editForm.name = $event.detail.value"
            placeholder="输入真实姓名"
            placeholder-class="jpu-placeholder"
          />
        </view>
        <view class="jpu-form-group">
          <text class="jpu-form-label">性别</text>
          <view class="jpu-radio-group">
            <label class="jpu-radio-item">
              <radio class="jpu-radio" value="male" :checked="editForm.gender === 'male'" @click="editForm.gender = 'male'" />
              <text class="jpu-radio-text">男</text>
            </label>
            <label class="jpu-radio-item">
              <radio class="jpu-radio" value="female" :checked="editForm.gender === 'female'" @click="editForm.gender = 'female'" />
              <text class="jpu-radio-text">女</text>
            </label>
          </view>
        </view>
        <view v-if="formType === 'add-parent'" class="jpu-tip-warning">
          <text class="jpu-tip-bold">注意：</text>
          <text>追溯先祖将自动重新推算当前分支所有后裔的世代辈分。</text>
        </view>
      </view>
      <view class="jpu-modal-footer">
        <view class="jpu-btn-gray" @click="closeAllModals">
          <text>作罢</text>
        </view>
        <view class="jpu-btn-primary" @click="submitForm">
          <text>落笔确认</text>
        </view>
      </view>
    </view>

    <!-- 邀请弹窗 -->
    <view v-if="showInviteModal" class="jpu-modal-center">
      <view class="jpu-modal-header">
        <text class="jpu-modal-title">邀请家人加入</text>
        <text class="jpu-modal-close" @click="showInviteModal = false">×</text>
      </view>
      <view class="jpu-modal-body">
        <view class="jpu-qr-container">
          <image class="jpu-qr-image" :src="qrCodeUrl" mode="aspectFit" />
        </view>
        <view class="jpu-code-display">
          <text class="jpu-code-label">家谱码</text>
          <text class="jpu-code-value">{{ currentFamily.code }}</text>
        </view>
      </view>
      <view class="jpu-modal-footer">
        <view class="jpu-btn-outline" @click="copyCode">
          <text>复制邀请码</text>
        </view>
        <button class="jpu-btn-primary" open-type="share">
          <text>分享邀请</text>
        </button>
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
      showDrawer: false,
      showEditModal: false,
      members: [],
      treeData: [],
      selectedMember: {},
      formType: '',
      editForm: {
        name: '',
        gender: 'male'
      }
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
    formTitle() {
      if (this.formType === 'add-child') return '为 [' + this.selectedMember.name + '] 录入子嗣'
      if (this.formType === 'add-parent') return '为 [' + this.selectedMember.name + '] 追溯先祖'
      if (this.formType === 'edit-member') return '修正小传'
      return ''
    }
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
      this.showDrawer = true
    },

    closeAllModals() {
      this.showDrawer = false
      this.showEditModal = false
    },

    openForm(type) {
      this.formType = type
      this.editForm = {
        name: type === 'edit-member' ? this.selectedMember.name : '',
        gender: this.selectedMember.gender || 'male'
      }
      this.showDrawer = false
      this.showEditModal = true
    },

    copyCode() {
      uni.setClipboardData({
        data: this.currentFamily.code,
        success: function() {
          uni.showToast({ title: '已复制邀请码', icon: 'success' })
        }
      })
    },

    async submitForm() {
      if (!this.editForm.name.trim()) {
        uni.showToast({ title: '姓名不能为空', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        
        if (this.formType === 'edit-member') {
          await api.member.applyEdit(this.currentFamily.id, this.selectedMember.id, {
            fieldName: 'name',
            oldValue: this.selectedMember.name,
            newValue: this.editForm.name
          })
        }
        
        uni.hideLoading()
        uni.showToast({ title: '已提交', icon: 'success' })
        this.closeAllModals()
        this.loadTreeData()
      } catch (error) {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
.jpu-page-container {
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-text: #3E2A23;
  --theme-primary: #8E292C;
  --theme-border: #D4C9BD;
  
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 32rpx;
}

/* 空状态 */
.jpu-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 64rpx;
}

.jpu-empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background-color: var(--theme-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(142, 41, 44, 0.2);
}

.jpu-empty-icon-text {
  font-size: 72rpx;
  color: #FFFFFF;
  font-weight: bold;
  letter-spacing: 8rpx;
}

.jpu-empty-text {
  font-size: 28rpx;
  color: #8D6E63;
  margin-bottom: 48rpx;
}

.jpu-empty-btn {
  background-color: var(--theme-primary);
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
  padding: 24rpx 48rpx;
  border-radius: 12rpx;
}

/* Section标题 */
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

/* 邀请码卡片 */
.jpu-invite-card {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.jpu-invite-label {
  font-size: 24rpx;
  color: #8D6E63;
  margin-bottom: 8rpx;
  display: block;
}

.jpu-invite-code {
  font-family: monospace;
  font-size: 40rpx;
  font-weight: bold;
  letter-spacing: 12rpx;
  color: var(--theme-primary);
}

.jpu-copy-btn {
  background-color: #F9EBEA;
  border: 2rpx solid #E6B0AA;
  border-radius: 8rpx;
  padding: 16rpx 24rpx;
}

.jpu-copy-btn:active {
  background-color: #F2D7D5;
}

.jpu-copy-btn-text {
  font-size: 24rpx;
  font-weight: bold;
  color: var(--theme-primary);
}

/* 家谱树滚动区 */
.jpu-tree-scroll {
  white-space: nowrap;
}

.jpu-tree-card {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 32rpx;
  display: inline-block;
  min-width: 100%;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.jpu-tree-container {
  min-width: max-content;
}

.jpu-tree-node-item {
  position: relative;
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
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.jpu-modal-overlay.jpu-open {
  opacity: 1;
  pointer-events: auto;
}

/* 底部抽屉 */
.jpu-drawer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--theme-bg);
  border-radius: 32rpx 32rpx 0 0;
  padding: 0 32rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 60;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.15);
}

.jpu-drawer.jpu-open {
  transform: translateY(0);
}

.jpu-drawer-handle {
  width: 96rpx;
  height: 8rpx;
  background-color: var(--theme-border);
  border-radius: 4rpx;
  margin: 24rpx auto;
}

.jpu-drawer-header {
  text-align: center;
  margin-bottom: 32rpx;
}

.jpu-drawer-title {
  font-size: 40rpx;
  font-weight: bold;
  color: var(--theme-text);
  letter-spacing: 6rpx;
  display: block;
}

.jpu-drawer-subtitle {
  font-size: 24rpx;
  color: #8D6E63;
  margin-top: 8rpx;
  display: block;
}

.jpu-drawer-actions {
  padding-bottom: 32rpx;
}

.jpu-drawer-btn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 48rpx;
  border-radius: 8rpx;
  margin-bottom: 20rpx;
}

.jpu-drawer-btn:last-child {
  margin-bottom: 0;
}

.jpu-drawer-btn-text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
}

.jpu-drawer-btn-arrow {
  font-size: 28rpx;
}

.jpu-btn-outline {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
}

.jpu-btn-outline .jpu-drawer-btn-text,
.jpu-btn-outline .jpu-drawer-btn-arrow {
  color: var(--theme-text);
}

.jpu-btn-outline .jpu-drawer-btn-arrow {
  color: var(--theme-border);
}

.jpu-btn-danger {
  background-color: #F5EBE9;
  border: 2rpx solid #E6B0AA;
}

.jpu-btn-danger .jpu-drawer-btn-text,
.jpu-btn-danger .jpu-drawer-btn-arrow {
  color: var(--theme-primary);
}

.jpu-btn-danger .jpu-drawer-btn-arrow {
  color: #E6B0AA;
}

/* 居中模态框 */
.jpu-modal-center {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 600rpx;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  overflow: hidden;
  z-index: 60;
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

/* 提示区 */
.jpu-tip-warning {
  background-color: #FFF8E1;
  border: 2rpx solid #FFE082;
  border-radius: 8rpx;
  padding: 20rpx 24rpx;
  font-size: 24rpx;
  color: #8D6E63;
  line-height: 1.6;
}

.jpu-tip-bold {
  color: #F57F17;
  font-weight: bold;
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

/* 二维码 */
.jpu-qr-container {
  display: flex;
  justify-content: center;
  padding: 32rpx 0;
}

.jpu-qr-image {
  width: 320rpx;
  height: 320rpx;
  background-color: var(--theme-bg);
  border-radius: 16rpx;
}

.jpu-code-display {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
}

.jpu-code-label {
  font-size: 28rpx;
  color: #8D6E63;
  margin-right: 16rpx;
}

.jpu-code-value {
  font-family: monospace;
  font-size: 40rpx;
  font-weight: bold;
  letter-spacing: 8rpx;
  color: var(--theme-primary);
}
</style>
