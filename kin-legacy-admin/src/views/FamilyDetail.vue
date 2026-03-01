<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NButton, NSpace, NTag, NDescriptions, NDescriptionsItem, NEmpty, useMessage, NModal, NImage } from 'naive-ui'
import { getFamilyById } from '@/api/family'
import { getFamilyMembers, getMemberDetail } from '@/api/member'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const familyId = Number(route.params.id)
const familyInfo = ref<any>(null)
const members = ref<any[]>([])

const showDetailModal = ref(false)
const memberDetail = ref<any>(null)
const memberLoading = ref(false)

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

const viewMemberDetail = async (member: any) => {
  showDetailModal.value = true
  memberLoading.value = true
  try {
    const res = await getMemberDetail(familyId, member.id)
    memberDetail.value = res.data
  } catch (error) {
    message.error('获取成员详情失败')
  } finally {
    memberLoading.value = false
  }
}

const getGenderText = (gender: string) => {
  return gender === 'male' ? '男' : (gender === 'female' ? '女' : '未知')
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
        <div v-for="member in members" :key="member.id" class="member-card" @click="viewMemberDetail(member)">
          <div class="member-avatar">{{ member.name?.charAt(0) || '?' }}</div>
          <div class="member-name">{{ member.name }}</div>
          <NTag v-if="member.isCreator === 1" type="success" size="small">创建者</NTag>
        </div>
      </div>
      <NEmpty v-else description="暂无成员" />
    </NCard>

    <NModal v-model:show="showDetailModal" preset="card" title="成员详情" style="width: 600px">
      <div v-if="memberDetail">
        <NDescriptions label-placement="left" :column="2" label-style="font-weight: 500;">
          <NDescriptionsItem label="姓名">{{ memberDetail.name }}</NDescriptionsItem>
          <NDescriptionsItem label="性别">{{ getGenderText(memberDetail.gender) }}</NDescriptionsItem>
          <NDescriptionsItem label="年龄">{{ memberDetail.age || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="出生日期">{{ memberDetail.birthDate || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="出生地">{{ memberDetail.birthPlace || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="去世日期">{{ memberDetail.deathDate || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="角色" v-if="memberDetail.isCreator === 1">
            <NTag type="success">创建者</NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="简介" :span="2">{{ memberDetail.bio || '暂无' }}</NDescriptionsItem>
          <NDescriptionsItem label="照片相册" :span="2">
            <div class="photos-placeholder">{{ memberDetail.photos || '该功能待开发' }}</div>
          </NDescriptionsItem>
        </NDescriptions>
        
        <div v-if="memberDetail.relations && memberDetail.relations.length > 0" style="margin-top: 20px;">
          <div class="relations-title">家庭关系</div>
          <div class="relations-list">
            <NTag v-for="rel in memberDetail.relations" :key="rel.memberId" style="margin-right: 8px; margin-bottom: 8px;">
              {{ rel.relationLabel }}: {{ rel.memberName }}
            </NTag>
          </div>
        </div>
      </div>
      <div v-else-if="memberLoading" style="text-align: center; padding: 40px;">
        加载中...
      </div>
    </NModal>
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
  cursor: pointer;
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

.photos-placeholder {
  padding: 20px;
  text-align: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

.relations-title {
  font-weight: 500;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.relations-list {
  display: flex;
  flex-wrap: wrap;
}
</style>
