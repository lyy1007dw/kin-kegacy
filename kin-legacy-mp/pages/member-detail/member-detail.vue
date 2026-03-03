<template>
  <view class="detail-page">
    <view v-if="!currentFamily" class="jpu-empty-state">
      <view class="jpu-empty-icon-wrap">
        <text class="jpu-empty-icon-text">谱</text>
      </view>
      <text class="jpu-empty-text">请先选择一个家谱</text>
      <view class="jpu-empty-btn" @click="goToIndex">返回首页选择</view>
    </view>

    <block v-else>
      <view v-if="loading" class="loading-wrap">
        <text>加载中...</text>
      </view>

      <block v-else-if="member">
        <!-- 成员基本信息卡片 -->
        <view class="detail-card">
          <view class="member-header">
            <image 
              v-if="member.avatar" 
              :src="member.avatar" 
              class="member-avatar" 
              mode="aspectFill"
            />
            <view v-else class="member-avatar-placeholder">
              <text>{{ member.name?.charAt(0) || '?' }}</text>
            </view>
            <view class="member-basic">
              <text class="member-name">{{ member.name }}</text>
              <view class="member-tags">
                <text class="gender-tag" :class="member.gender === 'male' ? 'tag-male' : 'tag-female'">
                  {{ member.gender === 'male' ? '男' : '女' }}
                </text>
                <text v-if="member.age" class="age-tag">{{ member.age }}岁</text>
                <text v-if="member.currentUser" class="me-tag">{{ member.currentUserLabel || '我' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 详细信息 -->
        <view class="detail-section">
          <view class="section-title">
            <text class="section-line"></text>
            <text>基本信息</text>
          </view>
          <view class="info-list">
            <view class="info-item" v-if="member.birthDate">
              <text class="info-label">出生日期</text>
              <text class="info-value">{{ member.birthDate }}</text>
            </view>
            <view class="info-item" v-if="member.birthPlace">
              <text class="info-label">出生地</text>
              <text class="info-value">{{ member.birthPlace }}</text>
            </view>
            <view class="info-item" v-if="member.deathDate">
              <text class="info-label">去世日期</text>
              <text class="info-value">{{ member.deathDate }}</text>
            </view>
            <view class="info-item" v-if="!member.birthDate && !member.birthPlace && !member.deathDate">
              <text class="info-empty">暂无详细信息</text>
            </view>
          </view>
        </view>

        <!-- 生平简介 -->
        <view class="detail-section">
          <view class="section-title">
            <text class="section-line"></text>
            <text>生平简介</text>
          </view>
          <view class="bio-content">
            <text>{{ member.bio || '暂无简介' }}</text>
          </view>
        </view>

        <!-- 照片相册 -->
        <view class="detail-section">
          <view class="section-title">
            <text class="section-line"></text>
            <text>照片相册</text>
          </view>
          <view class="photos-placeholder">
            <text>{{ member.photos || '该功能待开发' }}</text>
          </view>
        </view>

        <!-- 家庭关系 -->
        <view class="detail-section" v-if="member.relations && member.relations.length > 0">
          <view class="section-title">
            <text class="section-line"></text>
            <text>家庭关系</text>
          </view>
          <view class="relations-list">
            <view 
              class="relation-item" 
              v-for="rel in member.relations" 
              :key="rel.memberId"
              @click="goToMember(rel.memberId)"
            >
              <text class="relation-label">{{ rel.relationLabel }}</text>
              <text class="relation-name">{{ rel.memberName }}</text>
              <text class="relation-arrow">→</text>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <view class="action-btn primary" @click="openApplyEdit">
            <text>申请修改</text>
          </view>
        </view>
      </block>
    </block>

    <!-- 申请修改弹窗 -->
    <view v-if="showEditModal" class="jpu-modal-center">
      <view class="jpu-modal-header">
        <text class="jpu-modal-title">申请修改成员信息</text>
        <text class="jpu-modal-close" @click="showEditModal = false">×</text>
      </view>
      <view class="jpu-modal-body">
        <view class="jpu-form-group">
          <text class="jpu-form-label">姓名</text>
          <input class="jpu-form-input" v-model="editForm.name" placeholder="请输入姓名" />
        </view>
        <view class="jpu-form-group">
          <text class="jpu-form-label">出生日期</text>
          <input class="jpu-form-input" v-model="editForm.birthDate" placeholder="格式: 1990-01-01" />
        </view>
        <view class="jpu-form-group">
          <text class="jpu-form-label">出生地</text>
          <input class="jpu-form-input" v-model="editForm.birthPlace" placeholder="请输入出生地" />
        </view>
        <view class="jpu-form-group">
          <text class="jpu-form-label">生平简介</text>
          <textarea class="jpu-form-textarea" v-model="editForm.bio" placeholder="请输入生平简介" />
        </view>
      </view>
      <view class="jpu-modal-footer">
        <view class="jpu-btn-gray" @click="showEditModal = false">
          <text>取消</text>
        </view>
        <view class="jpu-btn-primary" @click="submitEditRequest">
          <text>提交申请</text>
        </view>
      </view>
    </view>

    <view 
      class="jpu-modal-overlay" 
      :class="{ 'jpu-open': showEditModal }"
      @click="showEditModal = false"
    ></view>
  </view>
</template>

<script>
import { mapState } from 'vuex'
import api from '../../utils/api'

export default {
  data() {
    return {
      memberId: null,
      member: null,
      loading: true,
      showEditModal: false,
      editForm: {
        name: '',
        birthDate: '',
        birthPlace: '',
        bio: ''
      }
    }
  },

  computed: {
    ...mapState(['currentFamily', 'userInfo'])
  },

  onLoad(options) {
    if (options.id) {
      this.memberId = parseInt(options.id)
    }
  },

  onShow() {
    if (this.currentFamily && this.memberId) {
      this.loadMemberDetail()
    }
  },

  methods: {
    goToIndex() {
      uni.switchTab({ url: '/pages/index/index' })
    },

    async loadMemberDetail() {
      if (!this.currentFamily || !this.memberId) return
      
      this.loading = true
      try {
        const res = await api.member.getDetail(this.currentFamily.id, this.memberId)
        this.member = res
        
        this.editForm = {
          name: res.name || '',
          birthDate: res.birthDate || '',
          birthPlace: res.birthPlace || '',
          bio: res.bio || ''
        }
      } catch (error) {
        console.error('加载成员详情失败', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    goToMember(memberId) {
      uni.navigateTo({
        url: `/pages/member-detail/member-detail?id=${memberId}`
      })
    },

    openApplyEdit() {
      this.showEditModal = true
    },

    async submitEditRequest() {
      if (!this.currentFamily || !this.memberId) return

      const changes = {}
      if (this.editForm.name && this.editForm.name !== this.member.name) {
        changes.name = { oldValue: this.member.name, newValue: this.editForm.name }
      }
      if (this.editForm.birthDate && this.editForm.birthDate !== this.member.birthDate) {
        changes.birthDate = { oldValue: this.member.birthDate || '', newValue: this.editForm.birthDate }
      }
      if (this.editForm.birthPlace && this.editForm.birthPlace !== this.member.birthPlace) {
        changes.birthPlace = { oldValue: this.member.birthPlace || '', newValue: this.editForm.birthPlace }
      }
      if (this.editForm.bio && this.editForm.bio !== this.member.bio) {
        changes.bio = { oldValue: this.member.bio || '', newValue: this.editForm.bio }
      }

      if (Object.keys(changes).length === 0) {
        uni.showToast({ title: '未做任何修改', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '提交中...' })
        await api.member.applyEdit(this.currentFamily.id, this.memberId, { changes })
        uni.hideLoading()
        uni.showToast({ title: '申请已提交', icon: 'success' })
        this.showEditModal = false
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: '提交失败', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.detail-page {
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-text: #3E2A23;
  --theme-primary: #8E292C;
  --theme-border: #D4C9BD;
  
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 32rpx;
}

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
}

.jpu-empty-icon-text {
  font-size: 72rpx;
  color: #FFFFFF;
  font-weight: bold;
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
  padding: 24rpx 48rpx;
  border-radius: 12rpx;
}

.loading-wrap {
  text-align: center;
  padding: 100rpx;
  color: #8D6E63;
}

.detail-card {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.member-header {
  display: flex;
  align-items: center;
}

.member-avatar, .member-avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
}

.member-avatar-placeholder {
  background: linear-gradient(135deg, var(--theme-primary) 0%, #A63A3D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #fff;
  font-weight: bold;
}

.member-basic {
  flex: 1;
}

.member-name {
  font-size: 40rpx;
  font-weight: bold;
  color: var(--theme-text);
  display: block;
  margin-bottom: 12rpx;
}

.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.gender-tag, .age-tag, .creator-tag, .me-tag {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.gender-tag {
  background-color: #EAF2FF;
  color: #1565C0;
  border: 2rpx solid #1565C0;
}

.tag-female {
  background-color: #FDF2F1;
  color: var(--theme-primary);
  border: 2rpx solid var(--theme-primary);
}

.age-tag {
  background-color: #FFF8E1;
  color: #F57F17;
  border: 2rpx solid #FFE082;
}

.creator-tag {
  background-color: #FFF3E0;
  color: #E65100;
  border: 2rpx solid #FFB74D;
}

.me-tag {
  background-color: #E8F5E9;
  color: #2E7D32;
  border: 2rpx solid #66BB6A;
}

.detail-section {
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: bold;
  color: var(--theme-text);
  margin-bottom: 20rpx;
}

.section-line {
  width: 8rpx;
  height: 28rpx;
  background-color: var(--theme-primary);
  margin-right: 16rpx;
  border-radius: 4rpx;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #8D6E63;
  font-size: 28rpx;
}

.info-value {
  color: var(--theme-text);
  font-size: 28rpx;
}

.info-empty {
  color: #BDBDBD;
  font-size: 28rpx;
  text-align: center;
  padding: 20rpx;
}

.bio-content {
  color: var(--theme-text);
  font-size: 28rpx;
  line-height: 1.8;
}

.photos-placeholder {
  text-align: center;
  padding: 40rpx;
  background-color: var(--theme-bg);
  border-radius: 12rpx;
  color: #BDBDBD;
}

.relations-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.relation-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: var(--theme-bg);
  border-radius: 12rpx;
}

.relation-label {
  font-size: 26rpx;
  color: #8D6E63;
  width: 120rpx;
}

.relation-name {
  flex: 1;
  font-size: 28rpx;
  color: var(--theme-text);
  font-weight: 500;
}

.relation-arrow {
  color: #BDBDBD;
}

.action-buttons {
  margin-top: 32rpx;
}

.action-btn {
  text-align: center;
  padding: 28rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.action-btn.primary {
  background-color: var(--theme-primary);
  color: #FFFFFF;
}

/* 弹窗样式 */
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
}

.jpu-modal-close {
  font-size: 48rpx;
  color: #8D6E63;
  line-height: 1;
}

.jpu-modal-body {
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.jpu-form-group {
  margin-bottom: 24rpx;
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
  height: 80rpx;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: var(--theme-text);
  box-sizing: border-box;
}

.jpu-form-textarea {
  width: 100%;
  min-height: 160rpx;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: var(--theme-text);
  box-sizing: border-box;
}

.jpu-modal-footer {
  display: flex;
  padding: 24rpx 32rpx 32rpx;
  gap: 24rpx;
}

.jpu-btn-gray, .jpu-btn-primary {
  flex: 1;
  background-color: var(--theme-bg);
  border: 2rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 24rpx;
  text-align: center;
}

.jpu-btn-gray text, .jpu-btn-primary text {
  font-size: 28rpx;
  font-weight: bold;
  letter-spacing: 4rpx;
}

.jpu-btn-gray text {
  color: var(--theme-text);
}

.jpu-btn-primary {
  background-color: var(--theme-primary);
  border-color: #722023;
}

.jpu-btn-primary text {
  color: #FFFFFF;
}
</style>
