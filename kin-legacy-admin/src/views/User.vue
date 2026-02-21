<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { 
  NDataTable, NButton, NSpace, NCard, NTag, NModal, 
  NForm, NFormItem, NInput, useMessage, useDialog, NPagination
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getUserList, updateUser, disableUser, enableUser } from '@/api/user'

export interface User {
  id: number
  nickname: string
  avatar: string
  phone: string
  role: string
  status: string
  createTime: string
}

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const showModal = ref(false)
const editingUser = ref<User | null>(null)

const formValue = ref({
  nickname: '',
  phone: ''
})

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [5, 10, 20, 50]
})

const isAdmin = (row: User) => row.role === 'admin' || row.id === 1

const columns: DataTableColumns<User> = [
  { title: 'ID', key: 'id', width: 80, align: 'center' },
  { title: '昵称', key: 'nickname', minWidth: 120 },
  { title: '手机号', key: 'phone', minWidth: 130 },
  { 
    title: '角色', 
    key: 'role',
    width: 120,
    align: 'center',
    render: (row) => h(NTag, { type: row.role === 'admin' ? 'error' : 'default', size: 'small' }, { default: () => row.role === 'admin' ? '管理员' : '普通用户' })
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => h(NTag, { type: row.status === 'disabled' ? 'error' : 'success', size: 'small' }, { default: () => row.status === 'disabled' ? '已禁用' : '正常' })
  },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      const isAdminUser = isAdmin(row)
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { 
            size: 'small', 
            type: 'primary',
            disabled: isAdminUser,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, { 
            size: 'small', 
            type: row.status === 'disabled' ? 'success' : 'error',
            disabled: isAdminUser,
            onClick: () => row.status === 'disabled' ? handleEnable(row) : handleDisable(row)
          }, { default: () => row.status === 'disabled' ? '启用' : '禁用' })
        ]
      })
    }
  }
]

const data = ref<User[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(pagination.value.page, pagination.value.pageSize)
    data.value = res.data?.records || []
    pagination.value.itemCount = res.data?.total || 0
  } catch (error) {
    message.error('获取用户列表失败')
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

onMounted(() => {
  fetchData()
})

const handleEdit = (row: User) => {
  editingUser.value = row
  formValue.value = {
    nickname: row.nickname,
    phone: row.phone || ''
  }
  showModal.value = true
}

const handleUpdate = async () => {
  if (!editingUser.value) return
  try {
    await updateUser(editingUser.value.id, {
      nickname: formValue.value.nickname,
      phone: formValue.value.phone
    })
    message.success('更新成功')
    showModal.value = false
    editingUser.value = null
    formValue.value = { nickname: '', phone: '' }
    fetchData()
  } catch (error: any) {
    message.error(error.message || '更新失败')
  }
}

const handleDisable = (row: User) => {
  dialog.warning({
    title: '确认禁用',
    content: `确定要禁用用户 "${row.nickname}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await disableUser(row.id)
        message.success('禁用成功')
        fetchData()
      } catch (error: any) {
        message.error(error.message || '禁用失败')
      }
    }
  })
}

const handleEnable = (row: User) => {
  dialog.warning({
    title: '确认启用',
    content: `确定要启用用户 "${row.nickname}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await enableUser(row.id)
        message.success('启用成功')
        fetchData()
      } catch (error: any) {
        message.error(error.message || '启用失败')
      }
    }
  })
}

const handleCloseModal = () => {
  showModal.value = false
  editingUser.value = null
  formValue.value = { nickname: '', phone: '' }
}
</script>

<template>
  <div>
    <NCard>
      <template #header>
        <span class="card-title">用户管理</span>
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
      title="编辑用户" 
      style="width: 500px"
      @after-leave="handleCloseModal"
    >
      <NForm :model="formValue" label-placement="left" label-width="70">
        <NFormItem label="昵称" required>
          <NInput v-model:value="formValue.nickname" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="formValue.phone" placeholder="请输入手机号" />
        </NFormItem>
        <NSpace justify="end" style="margin-top: 16px;">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleUpdate">确定</NButton>
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
