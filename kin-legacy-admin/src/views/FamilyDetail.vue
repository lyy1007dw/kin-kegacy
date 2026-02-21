<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NButton, NSpace, NTag, NDescriptions, NDescriptionsItem, NEmpty, useMessage } from 'naive-ui'
import { getFamilyById } from '@/api/family'
import { getFamilyMembers } from '@/api/member'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const familyId = Number(route.params.id)
const familyInfo = ref<any>(null)
const members = ref<any[]>([])

const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    message.success('家谱码已复制')
  } catch {
    message.error('复制失败')
  }
}

const fetchData = async () => {
  try {
    const [familyRes, membersRes] = await Promise.all([
      getFamilyById(familyId),
      getFamilyMembers(familyId)
    ])
    familyInfo.value = familyRes.data
    members.value = membersRes.data || []
  } catch (error) {
    message.error('获取家谱详情失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div>
    <NCard>
      <template #header>
        <NSpace justify="space-between" align="center">
          <span class="card-title">家谱详情 - {{ familyInfo?.name }}</span>
          <NButton @click="router.push('/family')">返回列表</NButton>
        </NSpace>
      </template>
      <NDescriptions label-placement="left" :column="2" label-style="width: 100px; font-weight: 500;">
        <NDescriptionsItem label="家谱名称">{{ familyInfo?.name }}</NDescriptionsItem>
        <NDescriptionsItem label="家谱码">
          <span class="code-text" @click="copyCode(familyInfo?.code)">{{ familyInfo?.code }}</span>
        </NDescriptionsItem>
        <NDescriptionsItem label="成员数量">{{ familyInfo?.memberCount }}</NDescriptionsItem>
        <NDescriptionsItem label="创建时间">{{ familyInfo?.createTime }}</NDescriptionsItem>
        <NDescriptionsItem label="简介" :span="2">{{ familyInfo?.description || '暂无简介' }}</NDescriptionsItem>
      </NDescriptions>
    </NCard>

    <NCard style="margin-top: 20px;">
      <template #header>
        <span class="card-title">成员列表</span>
      </template>
      <div v-if="members.length > 0" class="member-grid">
        <div v-for="member in members" :key="member.id" class="member-card">
          <div class="member-avatar">{{ member.name?.charAt(0) || '?' }}</div>
          <div class="member-name">{{ member.name }}</div>
          <NTag v-if="member.isCreator === 1" type="success" size="small">创建者</NTag>
        </div>
      </div>
      <NEmpty v-else description="暂无成员" />
    </NCard>
  </div>
</template>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}

.code-text {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: var(--primary-color);
  cursor: pointer;
  padding: 2px 10px;
  border-radius: 6px;
  background: rgba(196, 163, 90, 0.1);
  transition: all 0.2s ease;
}

.code-text:hover {
  background: rgba(196, 163, 90, 0.2);
}

.code-text:active {
  transform: scale(0.98);
}

.member-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.member-card {
  width: 120px;
  padding: 20px;
  text-align: center;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.member-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.1);
}

.member-avatar {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 500;
}

.member-name {
  font-size: 15px;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-primary);
}
</style>
