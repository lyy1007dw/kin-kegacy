<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NDataTable, NButton, NSpace, NCard, NModal, 
  NForm, NFormItem, NInput, useMessage, NPagination, NTooltip
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getFamilyList, createFamily, updateFamily, deleteFamily } from '@/api/family'
import { formatValue, formatDate } from '@/utils/format'

export interface Family {
  id: number
  name: string
  code: string
  avatar: string
  description: string
  creatorId: number
  memberCount: number
  createTime: string
}

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [5, 10, 20, 50]
})

const formValue = ref({
  name: '',
  description: ''
})

const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    message.success('家谱码已复制')
  } catch {
    message.error('复制失败')
  }
}

const columns: DataTableColumns<Family> = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
    align: 'center',
    render: (row) => formatValue(row.id)
  },
  {
    title: '家谱名称',
    key: 'name',
    minWidth: 150,
    render: (row) => formatValue(row.name)
  },
  {
    title: '家谱码',
    key: 'code',
    width: 160,
    render: (row) => {
      if (!row.code) return formatValue(row.code)
      return h('div', { 
        class: 'code-cell',
        onClick: () => copyCode(row.code)
      }, h('span', { class: 'code-text' }, row.code))
    }
  },
  {
    title: '成员数',
    key: 'memberCount',
    width: 100,
    align: 'center',
    render: (row) => formatValue(row.memberCount)
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDate(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row) => {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { 
            size: 'small', 
            onClick: () => router.push(`/family/${row.id}`) 
          }, { default: () => '查看' }),
          h(NButton, { 
            size: 'small', 
            type: 'primary',
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, { 
            size: 'small', 
            type: 'error',
            onClick: () => handleDelete(row.id)
          }, { default: () => '删除' })
        ]
      })
    }
  }
]

const data = ref<Family[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFamilyList(pagination.value.page, pagination.value.pageSize)
    data.value = res.data?.records || []
    pagination.value.itemCount = res.data?.total || 0
  } catch (error) {
    message.error('获取家谱列表失败')
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

const handleCreate = async () => {
  if (!formValue.value.name) {
    message.warning('请输入家谱名称')
    return
  }
  try {
    await createFamily(formValue.value)
    message.success('创建成功')
    showModal.value = false
    formValue.value = { name: '', description: '' }
    fetchData()
  } catch (error: any) {
    message.error(error.message || '创建失败')
  }
}

const handleEdit = (row: Family) => {
  isEdit.value = true
  editingId.value = row.id
  formValue.value = {
    name: row.name,
    description: row.description || ''
  }
  showModal.value = true
}

const handleUpdate = async () => {
  if (!editingId.value) return
  try {
    await updateFamily(editingId.value, formValue.value)
    message.success('更新成功')
    showModal.value = false
    formValue.value = { name: '', description: '' }
    isEdit.value = false
    editingId.value = null
    fetchData()
  } catch (error: any) {
    message.error(error.message || '更新失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteFamily(id)
    message.success('删除成功')
    fetchData()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

const handleCloseModal = () => {
  showModal.value = false
  formValue.value = { name: '', description: '' }
  isEdit.value = false
  editingId.value = null
}
</script>

<template>
  <div>
    <NCard>
      <template #header>
        <NSpace justify="space-between" align="center">
          <span class="card-title">家谱列表</span>
          <NButton type="primary" @click="showModal = true">
            创建家谱
          </NButton>
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
      :title="isEdit ? '编辑家谱' : '创建家谱'" 
      style="width: 500px"
      @after-leave="handleCloseModal"
    >
      <NForm :model="formValue" label-placement="left" label-width="80">
        <NFormItem label="家谱名称" required>
          <NInput v-model:value="formValue.name" placeholder="请输入家谱名称" />
        </NFormItem>
        <NFormItem label="简介">
          <NInput v-model:value="formValue.description" type="textarea" placeholder="请输入简介" />
        </NFormItem>
        <NSpace justify="end" style="margin-top: 16px;">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="isEdit ? handleUpdate() : handleCreate()">确定</NButton>
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

.code-cell {
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 6px;
  background: rgba(196, 163, 90, 0.1);
  display: inline-block;
  transition: all 0.2s ease;
}

.code-cell:hover {
  background: rgba(196, 163, 90, 0.2);
}

.code-cell:active {
  transform: scale(0.98);
}

.code-text {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: var(--primary-color);
}
</style>
