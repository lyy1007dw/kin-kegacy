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
      <view class="family-tree-container" v-if="flatNodes.length > 0">
        <!-- 工具栏 -->
        <view class="toolbar">
          <view class="toolbar-btn" @click="resetView">
            <text class="toolbar-icon">⊙</text>
            <text class="toolbar-text">复位</text>
          </view>
          <view class="toolbar-btn" @click="zoomIn">
            <text class="toolbar-icon">+</text>
          </view>
          <view class="scale-display">
            <text>{{ Math.round(scale * 100) }}%</text>
          </view>
          <view class="toolbar-btn" @click="zoomOut">
            <text class="toolbar-icon">−</text>
          </view>
          <view class="toolbar-btn" @click="expandAll">
            <text class="toolbar-text">展开全部</text>
          </view>
        </view>

        <!-- 画布区域 -->
        <scroll-view
          class="canvas-scroll"
          scroll-x
          scroll-y
          :scroll-top="scrollTop"
          :scroll-left="scrollLeft"
        >
          <view
            class="canvas"
            :style="{
              transform: `scale(${scale})`,
              transformOrigin: 'top left',
              width: canvasWidth + 'px',
              height: canvasHeight + 'px'
            }"
          >
            <!-- 连接线层 -->
            <view class="lines-layer">
              <block v-for="line in lines" :key="line.id">
                <view
                  class="connect-line vertical"
                  :style="{
                    left: line.x + 'px',
                    top: line.y1 + 'px',
                    height: (line.y2 - line.y1) + 'px'
                  }"
                />
                <view
                  class="connect-line horizontal"
                  :style="{
                    left: line.x + 'px',
                    top: line.y2 + 'px',
                    width: nodeIndent + 'px'
                  }"
                />
              </block>
            </view>

            <!-- 节点层 -->
            <block v-for="node in flatNodes" :key="node.id">
              <view
                class="node-wrapper"
                :style="{
                  left: node.x + 'px',
                  top: node.y + 'px',
                  width: nodeWidth + 'px'
                }"
              >
                <!-- 折叠按钮 -->
                <view
                  v-if="node.hasChildren"
                  class="collapse-btn"
                  :class="{ collapsed: node.collapsed }"
                  @click="toggleCollapse(node.id)"
                >
                  <text>{{ node.collapsed ? '+' : '−' }}</text>
                </view>

                <!-- 成员卡片 -->
                <view
                  class="member-card"
                  :class="[node.gender === 'female' ? 'card-female' : 'card-male', { 'card-self': node.currentUser }]"
                  @click="onMemberTap(node)"
                >
                  <view class="card-left">
                    <view class="gender-badge" :class="node.gender === 'female' ? 'badge-female' : 'badge-male'">
                      <text>{{ node.gender === 'female' ? '女' : '男' }}</text>
                    </view>
                  </view>
                  <view class="card-right">
                    <view class="name-row">
                      <text class="member-name">{{ node.name }}</text>
                      <view v-if="node.currentUser" class="self-tag"><text>我</text></view>
                    </view>
                    <view class="gen-badge">
                      <text>第 {{ node.generation }} 世</text>
                    </view>
                  </view>
                </view>
              </view>
            </block>
          </view>
        </scroll-view>
      </view>

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
        <view class="jpu-drawer-btn jpu-btn-outline" @click="goToMemberDetail">
          <text class="jpu-drawer-btn-text">查看生平简介</text>
          <text class="jpu-drawer-btn-arrow">⟩</text>
        </view>
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
        </view>
      </view>
    </view>

    <!-- 邀请码-modal -->
    <view class="jpu-modal" :class="{ 'jpu-open': showInviteModal }">
      <view class="jpu-modal-content">
        <view class="jpu-modal-header">
          <text class="jpu-modal-title">邀请亲友</text>
          <text class="jpu-modal-close" @click="showInviteModal = false">×</text>
        </view>
        <view class="jpu-modal-body">
          <view class="jpu-qrcode-wrap">
            <image :src="qrCodeUrl" class="jpu-qrcode" mode="aspectFit" />
          </view>
          <view class="jpu-code-display">
            <text class="jpu-code-label">邀请码</text>
            <text class="jpu-code-value">{{ currentFamily.code }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 录入/修改表单弹窗 -->
    <view class="jpu-modal" :class="{ 'jpu-open': showEditModal }">
      <view class="jpu-modal-content">
        <view class="jpu-modal-header">
          <text class="jpu-modal-title">
            {{ formType === 'add-child' ? '录入子嗣' : (formType === 'add-parent' ? '追溯先祖' : '修正小传') }}
          </text>
          <text class="jpu-modal-close" @click="showEditModal = false">×</text>
        </view>
        <view class="jpu-modal-body">
          <view class="jpu-form-item">
            <text class="jpu-form-label">姓名</text>
            <input class="jpu-form-input" v-model="editForm.name" placeholder="请输入姓名" />
          </view>
          <view class="jpu-form-item" v-if="formType !== 'edit-member'">
            <text class="jpu-form-label">性别</text>
            <view class="jpu-form-radio-group">
              <view class="jpu-form-radio" :class="{ active: editForm.gender === 'male' }" @click="editForm.gender = 'male'">
                <text>男</text>
              </view>
              <view class="jpu-form-radio" :class="{ active: editForm.gender === 'female' }" @click="editForm.gender = 'female'">
                <text>女</text>
              </view>
            </view>
          </view>
          <view class="jpu-form-btn-group">
            <view class="jpu-form-btn jpu-form-btn-cancel" @click="showEditModal = false">
              <text>取消</text>
            </view>
            <view class="jpu-form-btn jpu-form-btn-submit" @click="submitForm">
              <text>提交</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'
import api from '../../utils/api'

// 布局常量
const NODE_WIDTH   = 260
const NODE_HEIGHT  = 72
const NODE_INDENT  = 40
const NODE_GAP_Y   = 20
const CANVAS_PAD   = 24

export default {
  data() {
    return {
      showInviteModal: false,
      showDrawer: false,
      showEditModal: false,
      members: [],
      treeData: [],
      expandedIds: {},
      selectedMember: {},
      formType: '',
      editForm: {
        name: '',
        gender: 'male'
      },

      // 树形布局数据
      NODE_WIDTH,
      NODE_INDENT,
      collapsedMap: {},

      // 缩放 & 平移
      scale: 1,
      minScale: 0.3,
      maxScale: 2.5,
      scrollTop: 0,
      scrollLeft: 0,
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

    // 后端返回的是嵌套树形结构，可能有多个根节点
    tree() {
      if (!this.treeData || this.treeData.length === 0) return null
      
      // 后端返回的是根节点数组，每个节点已有children
      // 需要添加collapsed状态
      const processNode = (node) => {
        if (!node) return null
        return {
          ...node,
          collapsed: !!this.collapsedMap[node.id],
          hasChildren: node.children && node.children.length > 0,
          children: node.children ? node.children.map(c => processNode(c)) : []
        }
      }
      
      // 返回所有根节点
      return this.treeData.map(root => processNode(root))
    },

    //扁平化节点带坐标 - 支持多个根节点
    flatNodes() {
      if (!this.tree) return []
      const result = []
      let currentY = CANVAS_PAD
      
      // 多个根节点水平排列
      const roots = Array.isArray(this.tree) ? this.tree : [this.tree]
      
      roots.forEach(rootNode => {
        if (!rootNode) return
        
        const traverse = (node, depth) => {
          if (!node) return

          const x = CANVAS_PAD + depth * NODE_INDENT
          const y = currentY
          currentY += NODE_HEIGHT + NODE_GAP_Y

          result.push({ ...node, x, y, depth })

          if (!node.collapsed && node.children && node.children.length > 0) {
            node.children.forEach(child => traverse(child, depth + 1))
          }
        }
        
        traverse(rootNode, 0)
        currentY += NODE_HEIGHT // 根节点之间留空
      })
      
      return result
    },

    // 连接线数据 - 通过深度优先搜索找父节点
    lines() {
      const lines = []
      const flatNodeMap = {}
      this.flatNodes.forEach(n => { flatNodeMap[n.id] = n })
      
      // 在树结构中递归查找父节点
      const findParent = (nodeId, nodes) => {
        if (!nodes || nodes.length === 0) return null
        for (const node of nodes) {
          if (node.children && node.children.length > 0) {
            const child = node.children.find(c => c.id === nodeId)
            if (child) {
              return flatNodeMap[node.id]
            }
            const parent = findParent(nodeId, node.children)
            if (parent) return parent
          }
        }
        return null
      }
      
      this.flatNodes.forEach(node => {
        const parent = findParent(node.id, this.tree)
        if (parent) {
          const lineX = parent.x + 20
          const y1 = parent.y + NODE_HEIGHT
          const y2 = node.y + NODE_HEIGHT / 2
          lines.push({ id: `${parent.id}-${node.id}`, x: lineX, y1, y2 })
        }
      })
      return lines
    },
    
    canvasWidth() {
      if (!this.flatNodes.length) return 400
      return Math.max(...this.flatNodes.map(n => n.x + NODE_WIDTH)) + CANVAS_PAD
    },

    canvasHeight() {
      if (!this.flatNodes.length) return 400
      return Math.max(...this.flatNodes.map(n => n.y + NODE_HEIGHT)) + CANVAS_PAD * 2
    },

    nodeWidth() {
      return NODE_WIDTH
    },
    nodeIndent() {
      return NODE_INDENT
    }
  },

  watch: {
    currentFamily: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.loadTreeData()
        }
      }
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
        
        // 初始化全部展开
        this.expandedIds = {}
        this.collapsedMap = {}
      } catch (error) {
        console.error('加载树形数据失败', error)
      }
    },

    copyCode() {
      uni.setClipboardData({
        data: this.currentFamily.code,
        success: () => {
          uni.showToast({ title: '邀请码已复制', icon: 'success' })
        }
      })
    },

    toggleCollapse(id) {
      this.$set(this.collapsedMap, id, !this.collapsedMap[id])
    },

    expandAll() {
      this.collapsedMap = {}
    },

    zoomIn() {
      this.scale = Math.min(this.maxScale, +(this.scale + 0.1).toFixed(1))
    },

    zoomOut() {
      this.scale = Math.max(this.minScale, +(this.scale - 0.1).toFixed(1))
    },

    resetView() {
      this.scale = 1
      this.scrollTop = 0
      this.scrollLeft = 0
    },

    showMemberDetail(member) {
      this.selectedMember = member
      this.showDrawer = true
    },

    onMemberTap(node) {
      this.showMemberDetail(node)
    },

    onTreeNodeClick(node) {
      this.showMemberDetail(node)
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

    goToMemberDetail() {
      if (!this.selectedMember || !this.selectedMember.id) return
      uni.navigateTo({
        url: `/pages/member-detail/member-detail?id=${this.selectedMember.id}`
      })
      this.showDrawer = false
    },

    async submitForm() {
      if (!this.editForm.name) {
        uni.showToast({ title: '请输入姓名', icon: 'none' })
        return
      }

      try {
        if (this.formType === 'add-child') {
          await api.member.addChild(this.currentFamily.id, this.selectedMember.id, this.editForm)
          uni.showToast({ title: '已提交申请', icon: 'success' })
        } else if (this.formType === 'add-parent') {
          await api.member.addParent(this.currentFamily.id, this.selectedMember.id, this.editForm)
          uni.showToast({ title: '已提交申请', icon: 'success' })
        } else if (this.formType === 'edit-member') {
          await api.member.update(this.currentFamily.id, this.selectedMember.id, this.editForm)
          uni.showToast({ title: '已提交修改申请', icon: 'success' })
        }
        this.showEditModal = false
        this.loadTreeData()
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },

    onLoad(options) {
      if (this.currentFamily) {
        this.loadTreeData()
      }
    },

    onShow() {
      if (this.currentFamily) {
        this.loadTreeData()
      }
    },

    onShareAppMessage() {
      return {
        title: '慎终追远 - ' + (this.currentFamily ? this.currentFamily.name : '家谱'),
        path: '/pages/index/index?code=' + (this.currentFamily && this.currentFamily.code || '')
      }
    }
  }
}
</script>

<style lang="scss">
/* 主题色变量 */
page {
  --theme-primary: #8E292C;
  --theme-primary-light: #A63A3D;
  --theme-bg: #F2ECE4;
  --theme-card: #FBF9F6;
  --theme-border: #D4C9BD;
  --theme-text: #3E2A23;
  --theme-text-secondary: #8D6E63;
}

/* 容器 */
.jpu-page-container {
  min-height: 100vh;
  background-color: var(--theme-bg);
  padding: 24rpx;
}

/* 空状态 */
.jpu-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.jpu-empty-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background-color: var(--theme-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(142, 41, 44, 0.2);
}

.jpu-empty-icon-text {
  font-size: 64rpx;
  color: #FFFFFF;
  font-weight: bold;
  letter-spacing: 8rpx;
}

.jpu-empty-text {
  font-size: 28rpx;
  color: #8D6E63;
  letter-spacing: 4rpx;
}

.jpu-empty-btn {
  margin-top: 32rpx;
  padding: 16rpx 48rpx;
  background-color: var(--theme-primary);
  border-radius: 8rpx;
}

.jpu-empty-btn text {
  color: #FFFFFF;
  font-size: 28rpx;
}

/* 邀请卡 */
.jpu-invite-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;
}

.jpu-invite-label {
  font-size: 24rpx;
  color: #8D6E63;
  display: block;
  margin-bottom: 8rpx;
}

.jpu-invite-code {
  font-size: 40rpx;
  color: var(--theme-primary);
  font-weight: bold;
  letter-spacing: 8rpx;
}

.jpu-copy-btn {
  padding: 12rpx 24rpx;
  background-color: var(--theme-primary);
  border-radius: 8rpx;
}

.jpu-copy-btn-text {
  color: #FFFFFF;
  font-size: 24rpx;
}

/* 标题 */
.jpu-section-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.jpu-section-line {
  width: 8rpx;
  height: 32rpx;
  background-color: var(--theme-primary);
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.jpu-section-title text {
  font-size: 32rpx;
  color: var(--theme-text);
  font-weight: bold;
  letter-spacing: 4rpx;
}

/* ==================== 家谱树容器 ==================== */
.family-tree-container {
  display: flex;
  flex-direction: column;
  height: 55vh;
  background: var(--theme-card);
  border: 2rpx solid var(--theme-border);
  border-radius: 16rpx;
  overflow: hidden;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1rpx solid #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  z-index: 10;
  flex-shrink: 0;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #f5f0eb;
  border-radius: 20px;
}

.toolbar-btn:active {
  opacity: 0.7;
}

.toolbar-icon {
  font-size: 20px;
  color: #c0392b;
  line-height: 1;
}

.toolbar-text {
  font-size: 12px;
  color: #555;
}

.scale-display {
  padding: 0 8px;
  font-size: 13px;
  color: #888;
  min-width: 44px;
  text-align: center;
}

/* 画布滚动区 */
.canvas-scroll {
  flex: 1;
  width: 100%;
  overflow: hidden;
}

.canvas {
  position: relative;
  will-change: transform;
}

/* 连接线 */
.lines-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.connect-line {
  position: absolute;
  background: #d4b8a8;
}

.connect-line.vertical {
  width: 2px;
}

.connect-line.horizontal {
  height: 2px;
}

/* 节点外层 */
.node-wrapper {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 折叠按钮 */
.collapse-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #c0392b;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(192, 57, 43, 0.35);
  z-index: 2;
}

.collapse-btn text {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  line-height: 1;
}

.collapse-btn.collapsed {
  background: #e8826e;
}

/* 成员卡片 */
.member-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1.5px solid transparent;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
}

.member-card:active {
  transform: scale(0.97);
}

.card-male {
  background: #fff;
  border-color: #d6e8f7;
}

.card-female {
  background: #fff5f7;
  border-color: #f7d6de;
}

.card-self {
  border-color: #c0392b !important;
  box-shadow: 0 2px 14px rgba(192,57,43,0.18) !important;
}

/* 性别徽章 */
.gender-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.gender-badge text {
  font-size: 13px;
  color: #fff;
  font-weight: 600;
}

.badge-male   { background: #5b9bd5; }
.badge-female { background: #e87fa3; }

/* 卡片右侧 */
.card-right {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c2c2c;
}

.self-tag {
  background: #c0392b;
  border-radius: 4px;
  padding: 1px 5px;
}

.self-tag text {
  font-size: 11px;
  color: #fff;
}

.gen-badge {
  background: #fff0ee;
  border-radius: 6px;
  padding: 2px 8px;
  display: inline-flex;
  align-self: flex-start;
}

.gen-badge text {
  font-size: 11px;
  color: #c0392b;
}

/* 弹层 */
.jpu-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  z-index: 100;
}

.jpu-modal-overlay.jpu-open {
  opacity: 1;
  visibility: visible;
}

.jpu-drawer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--theme-card);
  border-radius: 24rpx 24rpx 0 0;
  transform: translateY(100%);
  transition: transform 0.3s;
  z-index: 200;
  padding-bottom: env(safe-area-inset-bottom);
}

.jpu-drawer.jpu-open {
  transform: translateY(0);
}

.jpu-drawer-handle {
  width: 72rpx;
  height: 8rpx;
  background: #D4C9BD;
  border-radius: 4rpx;
  margin: 16rpx auto;
}

.jpu-drawer-header {
  padding: 0 32rpx 24rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.jpu-drawer-title {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--theme-text);
  display: block;
}

.jpu-drawer-subtitle {
  font-size: 24rpx;
  color: #8D6E63;
  margin-top: 8rpx;
  display: block;
}

.jpu-drawer-actions {
  padding: 24rpx 32rpx;
}

.jpu-drawer-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.jpu-btn-outline {
  background: var(--theme-card);
  border: 2rpx solid var(--theme-border);
}

.jpu-btn-outline text {
  color: var(--theme-text);
}

.jpu-btn-danger {
  background: #FDF2F1;
  border: 2rpx solid #E6B0AA;
}

.jpu-btn-danger text {
  color: var(--theme-primary);
}

.jpu-drawer-btn-text {
  font-size: 28rpx;
}

.jpu-drawer-btn-arrow {
  color: #8D6E63;
  font-size: 32rpx;
}

/* Modal */
.jpu-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  z-index: 300;
}

.jpu-modal.jpu-open {
  opacity: 1;
  visibility: visible;
}

.jpu-modal-content {
  width: 600rpx;
  background: var(--theme-card);
  border-radius: 24rpx;
  overflow: hidden;
}

.jpu-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.jpu-modal-title {
  font-size: 32rpx;
  font-weight: bold;
}

.jpu-modal-close {
  font-size: 48rpx;
  color: #8D6E63;
}

.jpu-modal-body {
  padding: 48rpx;
  text-align: center;
}

.jpu-qrcode-wrap {
  width: 400rpx;
  height: 400rpx;
  margin: 0 auto 32rpx;
  background: #f5f5f5;
}

.jpu-qrcode {
  width: 100%;
  height: 100%;
}

.jpu-code-display {
  text-align: center;
}

.jpu-code-label {
  font-size: 24rpx;
  color: #8D6E63;
  display: block;
  margin-bottom: 8rpx;
}

.jpu-code-value {
  font-size: 48rpx;
  color: var(--theme-primary);
  font-weight: bold;
  letter-spacing: 8rpx;
}

/* 表单样式 */
.jpu-form-item {
  margin-bottom: 24rpx;
}

.jpu-form-label {
  font-size: 28rpx;
  color: var(--theme-text);
  display: block;
  margin-bottom: 12rpx;
}

.jpu-form-input {
  width: 100%;
  height: 80rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.jpu-form-radio-group {
  display: flex;
  gap: 24rpx;
}

.jpu-form-radio {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
}

.jpu-form-radio.active {
  background: #fff;
  border-color: var(--theme-primary);
}

.jpu-form-radio text {
  font-size: 28rpx;
  color: var(--theme-text);
}

.jpu-form-radio.active text {
  color: var(--theme-primary);
  font-weight: bold;
}

.jpu-form-btn-group {
  display: flex;
  gap: 24rpx;
  margin-top: 32rpx;
}

.jpu-form-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 30rpx;
}

.jpu-form-btn-cancel {
  background: #f5f5f5;
}

.jpu-form-btn-cancel text {
  color: #666;
}

.jpu-form-btn-submit {
  background: var(--theme-primary);
}

.jpu-form-btn-submit text {
  color: #fff;
}
</style>
