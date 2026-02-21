<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NStatistic, NGrid, NGi, useMessage } from 'naive-ui'
import { getStatistics } from '@/api/statistics'

const router = useRouter()
const message = useMessage()

const stats = ref({
  familyCount: 0,
  memberCount: 0,
  pendingApproval: 0,
  userCount: 0
})

const fetchData = async () => {
  try {
    const res = await getStatistics()
    if (res.data) {
      stats.value = {
        familyCount: res.data.familyCount || 0,
        memberCount: res.data.memberCount || 0,
        pendingApproval: res.data.pendingApproval || 0,
        userCount: res.data.userCount || 0
      }
    }
  } catch (error) {
    message.error('获取统计数据失败')
  }
}

const navigateTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎使用家谱管理系统</h2>
      <p class="welcome-subtitle">传承家族记忆，记录世世代代</p>
    </div>
    
    <NGrid :cols="4" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
      <NGi span="4 m:2 l:1">
        <NCard class="stat-card stat-card-family" hoverable @click="navigateTo('/family')">
          <div class="stat-content">
            <div class="stat-info">
              <NStatistic :value="stats.familyCount" class="stat-value">
                <template #label>
                  <span class="stat-label">家谱总数</span>
                </template>
              </NStatistic>
              <span class="stat-desc">记录家族传承</span>
            </div>
          </div>
          <div class="stat-footer">
            <span>点击查看详情</span>
          </div>
        </NCard>
      </NGi>
      
      <NGi span="4 m:2 l:1">
        <NCard class="stat-card stat-card-member" hoverable @click="navigateTo('/member')">
          <div class="stat-content">
            <div class="stat-info">
              <NStatistic :value="stats.memberCount" class="stat-value">
                <template #label>
                  <span class="stat-label">成员总数</span>
                </template>
              </NStatistic>
              <span class="stat-desc">家族成员档案</span>
            </div>
          </div>
          <div class="stat-footer">
            <span>点击查看详情</span>
          </div>
        </NCard>
      </NGi>
      
      <NGi span="4 m:2 l:1">
        <NCard class="stat-card stat-card-approval" hoverable @click="navigateTo('/approval')">
          <div class="stat-content">
            <div class="stat-info">
              <NStatistic :value="stats.pendingApproval" class="stat-value">
                <template #label>
                  <span class="stat-label">待审批</span>
                </template>
              </NStatistic>
              <span class="stat-desc">等待处理申请</span>
            </div>
          </div>
          <div class="stat-footer">
            <span>点击查看详情</span>
          </div>
        </NCard>
      </NGi>
      
      <NGi span="4 m:2 l:1">
        <NCard class="stat-card stat-card-user" hoverable @click="navigateTo('/user')">
          <div class="stat-content">
            <div class="stat-info">
              <NStatistic :value="stats.userCount" class="stat-value">
                <template #label>
                  <span class="stat-label">用户总数</span>
                </template>
              </NStatistic>
              <span class="stat-desc">注册用户账户</span>
            </div>
          </div>
          <div class="stat-footer">
            <span>点击查看详情</span>
          </div>
        </NCard>
      </NGi>
    </NGrid>

    <div class="tips-section">
      <NCard class="tips-card">
        <template #header>
          <span class="tips-header">使用提示</span>
        </template>
        <div class="tips-content">
          <div class="tip-item">
            <span class="tip-number">01</span>
            <span class="tip-text">点击上方统计卡片可快速跳转到对应管理页面</span>
          </div>
          <div class="tip-item">
            <span class="tip-number">02</span>
            <span class="tip-text">家谱码是家谱的唯一标识，用户可通过家谱码申请加入家谱</span>
          </div>
          <div class="tip-item">
            <span class="tip-number">03</span>
            <span class="tip-text">审批管理中可处理用户的加入申请和成员信息修改申请</span>
          </div>
        </div>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  animation: fadeIn 0.4s ease-out;
}

.welcome-section {
  margin-bottom: 28px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.stat-card {
  cursor: pointer;
  border-radius: 16px !important;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none !important;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(74, 55, 40, 0.12) !important;
}

.stat-card :deep(.n-card__content) {
  padding: 0 !important;
}

.stat-content {
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  text-align: center;
}

.stat-value :deep(.n-statistic-value__content) {
  font-size: 36px !important;
  font-weight: 700 !important;
  color: var(--text-primary) !important;
}

.stat-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
}

.stat-desc {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 8px;
  opacity: 0.8;
}

.stat-footer {
  padding: 14px 24px;
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.stat-card-family .stat-footer {
  background: rgba(139, 90, 43, 0.08);
  color: #8B5A2B;
}

.stat-card-member .stat-footer {
  background: rgba(124, 179, 66, 0.1);
  color: #558B2F;
}

.stat-card-approval .stat-footer {
  background: rgba(249, 168, 37, 0.1);
  color: #F57F17;
}

.stat-card-user .stat-footer {
  background: rgba(92, 107, 192, 0.1);
  color: #3949AB;
}

.tips-section {
  margin-top: 28px;
}

.tips-card {
  background: linear-gradient(135deg, var(--bg-warm) 0%, var(--bg-cream) 100%) !important;
  border: 1px solid var(--border-color) !important;
}

.tips-header {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.tip-number {
  font-size: 12px;
  font-weight: 700;
  color: #C4A35A;
  background: rgba(196, 163, 90, 0.15);
  padding: 4px 10px;
  border-radius: 6px;
  flex-shrink: 0;
}

.tip-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .welcome-title {
    font-size: 22px;
  }
  
  .stat-content {
    padding: 20px;
  }
  
  .stat-value :deep(.n-statistic-value__content) {
    font-size: 28px !important;
  }
}
</style>
