<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { 
  NDataTable, NButton, NSpace, NCard, NModal, 
  NForm, NFormItem, NInput, NSelect, NDatePicker, useMessage, useDialog, NPagination
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getMemberList, updateMember, deleteMember, addMember } from '@/api/member'
import { getFamilyList, getNonAdminUsers, type User } from '@/api/admin'

export interface Member {
  id: number
  familyId: number
  familyName: string
  userId: number
  name: string
  gender: string
  avatar: string
  birthDate: string
  bio: string
  isCreator: number
  createTime: string
}

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const showModal = ref(false)
const showAddModal = ref(false)
const editingMember = ref<Member | null>(null)
const userOptions = ref<Array<{label: string, value: number}>>([])
const familyOptions = ref<Array<{label: string, value: number}>>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [5, 10, 20, 50]
})

const formValue = ref({
  userId: null as number | null,
  familyId: null as number | null,
  name: '',
  gender: 'male',
  birthDate: '',
  bio: ''
})

const newMemberForm = ref({
  userId: null as number | null,
  familyId: null as number | null,
  name: '',
  gender: 'male',
  birthDate: '',
  bio: ''
})

const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' }
]

const columns: DataTableColumns<Member> = [
  { title: 'ID', key: 'id', width: 80, align: 'center' },
  { title: '姓名', key: 'name', width: 120 },
  { title: '性别', key: 'gender', width: 80, align: 'center', render: (row) => row.gender === 'male' ? '男' : '女' },
  { title: '出生日期', key: 'birthDate', width: 120 },
  { title: '所属家谱', key: 'familyName', width: 150 },
  { title: '简介', key: 'bio', ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { 
            size: 'small', 
            type: 'primary',
            disabled: row.isCreator === 1,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, { 
            size: 'small', 
            type: 'error',
            disabled: row.isCreator === 1,
            onClick: () => handleDelete(row)
          }, { default: () => '删除' })
        ]
      })
    }
  }
]

const data = ref<Member[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMemberList(pagination.value.page, pagination.value.pageSize)
    data.value = res.data?.records || []
    pagination.value.itemCount = res.data?.total || 0
  } catch (error) {
    message.error('获取成员列表失败')
    pagination.value.itemCount = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  fetchData()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  fetchData()
}

const fetchUserOptions = async () => {
  try {
    const res = await getNonAdminUsers()
    userOptions.value = (res.data || []).map((u: User) => ({
      label: u.nickname || `用户${u.id}`,
      value: u.id
    }))
  } catch (error) {
    message.error('获取用户列表失败')
  }
}

const fetchFamilyOptions = async () => {
  try {
    const res = await getFamilyList(1, 1000)
    familyOptions.value = (res.data.records || []).map((f: any) => ({
      label: f.name,
      value: f.id
    }))
  } catch (error) {
    message.error('获取家谱列表失败')
  }
}

onMounted(() => {
  fetchData()
  fetchUserOptions()
  fetchFamilyOptions()
})

const handleAdd = () => {
  showAddModal.value = true
}

const handleCreate = async () => {
  if (!newMemberForm.value.userId || !newMemberForm.value.familyId || !newMemberForm.value.name) {
    message.warning('请填写必要信息')
    return
  }
  try {
    await addMember({
      userId: newMemberForm.value.userId,
      familyId: newMemberForm.value.familyId,
      name: newMemberForm.value.name,
      gender: newMemberForm.value.gender,
      birthDate: newMemberForm.value.birthDate || undefined,
      bio: newMemberForm.value.bio || undefined
    })
    message.success('添加成功')
    showAddModal.value = false
    newMemberForm.value = {
      userId: null,
      familyId: null,
      name: '',
      gender: 'male',
      birthDate: '',
      bio: ''
    }
    fetchData()
  } catch (error: any) {
    message.error(error.message || '添加失败')
  }
}

const handleCloseAddModal = () => {
  showAddModal.value = false
  newMemberForm.value = {
    userId: null,
    familyId: null,
    name: '',
    gender: 'male',
    birthDate: '',
    bio: ''
  }
}

const handleEdit = (row: Member) => {
  editingMember.value = row
  formValue.value = {
    userId: row.userId,
    familyId: row.familyId,
    name: row.name,
    gender: row.gender,
    birthDate: row.birthDate || '',
    bio: row.bio || ''
  }
  showModal.value = true
}

const handleUpdate = async () => {
  if (!editingMember.value) return
  if (!formValue.value.name) {
    message.warning('请填写成员姓名')
    return
  }
  try {
    await updateMember(editingMember.value.familyId, editingMember.value.id, {
      name: formValue.value.name,
      gender: formValue.value.gender as any,
      birthDate: formValue.value.birthDate || undefined,
      bio: formValue.value.bio || undefined
    })
    message.success('更新成功')
    showModal.value = false
    editingMember.value = null
    formValue.value = { userId: null, familyId: null, name: '', gender: 'male', birthDate: '', bio: '' }
    fetchData()
  } catch (error: any) {
    message.error(error.message || '更新失败')
  }
}

const handleDelete = (row: Member) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除成员 "${row.name}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMember(row.familyId, row.id)
        message.success('删除成功')
        fetchData()
      } catch (error: any) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

const handleCloseModal = () => {
  showModal.value = false
  editingMember.value = null
  formValue.value = { userId: null, familyId: null, name: '', gender: 'male', birthDate: '', bio: '' }
}
</script>

<template>
  <div>
    <NCard>
      <template #header>
        <NSpace justify="space-between" align="center">
          <span class="card-title">成员管理</span>
          <NButton type="primary" @click="handleAdd">新增成员</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :bordered="false"
      />
      <div class="pagination-wrapper">
        <NPagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="pagination.pageSizes"
          :item-count="pagination.itemCount"
          show-size-picker
          show-quick-jumper
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        >
          <template #prefix="{ itemCount }">
            共 {{ itemCount }} 条
          </template>
        </NPagination>
      </div>
    </NCard>

    <NModal 
      v-model:show="showModal" 
      preset="card" 
      title="编辑成员" 
      style="width: 500px"
      @after-leave="handleCloseModal"
    >
      <NForm :model="formValue" label-placement="left">
        <NFormItem label="成员姓名" required>
          <NInput v-model:value="formValue.name" placeholder="请输入成员姓名" />
        </NFormItem>
        <NFormItem label="性别">
          <NSelect v-model:value="formValue.gender" :options="genderOptions" />
        </NFormItem>
        <NFormItem label="出生日期">
          <NDatePicker 
            :value="formValue.birthDate ? new Date(formValue.birthDate).getTime() : null" 
            @update:value="(ts) => formValue.birthDate = ts ? new Date(ts).toISOString().split('T')[0] : ''"
            value-format="yyyy-MM-dd" 
            type="date" 
            placeholder="选择日期" 
            style="width: 100%" 
            clearable
          />
        </NFormItem>
        <NFormItem label="简介">
          <NInput v-model:value="formValue.bio" type="textarea" placeholder="请输入简介" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleUpdate">确定</NButton>
        </NSpace>
      </NForm>
    </NModal>

    <NModal 
      v-model:show="showAddModal" 
      preset="card" 
      title="新增成员" 
      style="width: 500px"
      @after-leave="handleCloseAddModal"
    >
      <NForm :model="newMemberForm" label-placement="left">
        <NFormItem label="选择用户" required>
          <NSelect v-model:value="newMemberForm.userId" :options="userOptions" placeholder="请选择用户" />
        </NFormItem>
        <NFormItem label="选择家谱" required>
          <NSelect v-model:value="newMemberForm.familyId" :options="familyOptions" placeholder="请选择家谱" />
        </NFormItem>
        <NFormItem label="成员姓名" required>
          <NInput v-model:value="newMemberForm.name" placeholder="请输入成员姓名" />
        </NFormItem>
        <NFormItem label="性别">
          <NSelect v-model:value="newMemberForm.gender" :options="genderOptions" />
        </NFormItem>
        <NFormItem label="出生日期">
          <NDatePicker 
            :value="newMemberForm.birthDate ? new Date(newMemberForm.birthDate).getTime() : null" 
            @update:value="(ts) => newMemberForm.birthDate = ts ? new Date(ts).toISOString().split('T')[0] : ''"
            value-format="yyyy-MM-dd" 
            type="date" 
            placeholder="选择日期" 
            style="width: 100%" 
            clearable
          />
        </NFormItem>
        <NFormItem label="简介">
          <NInput v-model:value="newMemberForm.bio" type="textarea" placeholder="请输入简介" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="showAddModal = false">取消</NButton>
          <NButton type="primary" @click="handleCreate">确定</NButton>
        </NSpace>
      </NForm>
    </NModal>
  </div>
</template>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
