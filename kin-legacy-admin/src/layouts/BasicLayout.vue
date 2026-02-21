<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout, NLayoutSider, NLayoutContent, NMenu, NAvatar, NDropdown, NSwitch, NTooltip } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const isDark = ref(false)

const menuOptions = [
  { label: '首页概览', key: '/dashboard' },
  { label: '家谱管理', key: '/family' },
  { label: '成员管理', key: '/member' },
  { label: '审批管理', key: '/approval' },
  { label: '用户管理', key: '/user' }
]

const activeKey = computed(() => route.path)

const handleMenuSelect = (key: string) => {
  router.push(key)
}

const userMenuOptions = [
  { label: '退出登录', key: 'logout' }
]

const handleUserMenuSelect = (key: string) => {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
  window.dispatchEvent(new CustomEvent('theme-change', { detail: isDark.value }))
}
</script>

<template>
  <NLayout has-sider style="height: 100vh" :class="{ 'dark-theme': isDark }">
    <NLayoutSider 
      bordered 
      collapse-mode="width" 
      :collapsed-width="72" 
      :width="240" 
      show-trigger
      :native-scrollbar="false"
      class="sidebar"
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo-container">
        <div v-if="!collapsed" class="logo-text">
          <span class="logo-title">慎重追远</span>
          <span class="logo-subtitle">家谱管理</span>
        </div>
        <div v-else class="logo-text-collapsed">追</div>
      </div>
      <NMenu
        :collapsed="collapsed"
        :collapsed-width="72"
        :options="menuOptions"
        :value="activeKey"
        :indent="24"
        class="side-menu"
        @update:value="handleMenuSelect"
      />
      <div v-if="!collapsed" class="sidebar-footer">
        <div class="footer-decoration"></div>
        <p class="footer-text">慎终追远</p>
      </div>
    </NLayoutSider>

    <NLayout class="main-layout">
      <div class="header">
        <div class="header-left">
          <h1 class="header-title">{{ route.meta.title }}</h1>
          <div class="header-breadcrumb">
            <span>慎重追远</span>
            <span class="separator">/</span>
            <span class="current">{{ route.meta.title }}</span>
          </div>
        </div>
        <div class="header-right">
          <NTooltip trigger="hover">
            <template #trigger>
              <div class="theme-switch" @click="toggleTheme">
                <span class="theme-label">{{ isDark ? '深色' : '浅色' }}</span>
              </div>
            </template>
            切换主题
          </NTooltip>
          <NDropdown 
            :options="userMenuOptions" 
            @select="handleUserMenuSelect"
            placement="bottom-end"
          >
            <div class="user-info">
              <NAvatar round size="medium" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || '管' }}
              </NAvatar>
              <div class="user-detail">
                <span class="username">{{ userStore.userInfo?.nickname || '管理员' }}</span>
                <span class="user-role">系统管理员</span>
              </div>
            </div>
          </NDropdown>
        </div>
      </div>
      <NLayoutContent content-style="padding: 24px;" class="main-content">
        <router-view />
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.sidebar {
  background: linear-gradient(180deg, #4A3728 0%, #3D2914 100%) !important;
}

.dark-theme .sidebar {
  background: linear-gradient(180deg, #1a1a1a 0%, #0d0d0d 100%) !important;
}

.sidebar :deep(.n-layout-sider-scroll-container) {
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-title {
  font-size: 20px;
  font-weight: 600;
  color: #FDF8F3;
  letter-spacing: 4px;
}

.logo-subtitle {
  font-size: 11px;
  color: #C4A35A;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-top: 4px;
}

.logo-text-collapsed {
  font-size: 24px;
  font-weight: 600;
  color: #C4A35A;
}

.side-menu {
  flex: 1;
  padding: 16px 8px;
}

.side-menu :deep(.n-menu-item) {
  height: 52px;
  margin: 4px 0;
  border-radius: 10px;
}

.side-menu :deep(.n-menu-item-content) {
  padding: 0 24px !important;
  border-radius: 10px;
}

.side-menu :deep(.n-menu-item-content__header) {
  color: rgba(255, 255, 255, 0.85) !important;
  font-weight: 500;
  font-size: 15px;
}

.side-menu :deep(.n-menu-item-content--selected) {
  background: rgba(196, 163, 90, 0.25) !important;
}

.side-menu :deep(.n-menu-item-content--selected .n-menu-item-content__header) {
  color: #C4A35A !important;
}

.side-menu :deep(.n-menu-item-content:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
}

.side-menu :deep(.n-menu-item-content:hover .n-menu-item-content__header) {
  color: #FDF8F3 !important;
}

.sidebar-footer {
  padding: 24px;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-decoration {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #C4A35A, transparent);
  margin: 0 auto 12px;
}

.footer-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  letter-spacing: 3px;
}

.main-layout {
  background-color: #FDF8F3 !important;
}

.dark-theme .main-layout {
  background-color: #121212 !important;
}

.header {
  height: 72px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border-bottom: 1px solid #E8DDD4;
}

.dark-theme .header {
  background: #1e1e1e;
  border-bottom-color: #333;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 22px;
  font-weight: 600;
  color: #3D2914;
  margin: 0;
}

.dark-theme .header-title {
  color: #e0e0e0;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #9C8575;
}

.dark-theme .header-breadcrumb {
  color: #888;
}

.header-breadcrumb .separator {
  color: #C4B5A5;
}

.header-breadcrumb .current {
  color: #8B5A2B;
  font-weight: 500;
}

.dark-theme .header-breadcrumb .current {
  color: #C4A35A;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.theme-switch {
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(139, 90, 43, 0.08);
}

.dark-theme .theme-switch {
  background: rgba(255, 255, 255, 0.1);
}

.theme-switch:hover {
  background: rgba(139, 90, 43, 0.15);
}

.dark-theme .theme-switch:hover {
  background: rgba(255, 255, 255, 0.15);
}

.theme-label {
  font-size: 13px;
  color: #8B5A2B;
  font-weight: 500;
}

.dark-theme .theme-label {
  color: #C4A35A;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(139, 90, 43, 0.08);
}

.dark-theme .user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #8B5A2B 0%, #A0522D 100%) !important;
  color: #FFFFFF !important;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #3D2914;
}

.dark-theme .username {
  color: #e0e0e0;
}

.user-role {
  font-size: 11px;
  color: #9C8575;
}

.dark-theme .user-role {
  color: #888;
}

.main-content {
  background-color: transparent !important;
}

:deep(.n-layout-sider-toggle) {
  background: rgba(196, 163, 90, 0.3) !important;
  border-radius: 0 8px 8px 0 !important;
}

:deep(.n-layout-sider-toggle:hover) {
  background: rgba(196, 163, 90, 0.5) !important;
}

:deep(.n-layout-sider-toggle .n-icon) {
  color: #FDF8F3 !important;
}
</style>
