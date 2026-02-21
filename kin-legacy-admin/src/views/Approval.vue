<script setup lang="ts">
import { ref, watch, onMounted, h } from 'vue'
import { 
  NTabs, NTabPane, NDataTable, NButton, NSpace, NTag, NCard, 
  NSelect, NPagination, useMessage, useDialog 
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getApprovalList, handleApproval } from '@/api/admin'

export interface ApprovalItem {
  id: number
  type: string
  familyId: number
  applicantUserId: number
  applicantName: string
  relationDesc?: string
  memberId?: number
  fieldName?: string
  oldValue?: string
  newValue?: string
  status: string
  createTime: string
}

const message = useMessage()
const dialog = useDialog()

const activeTab = ref('join')
const loading = ref(false)

const joinData = ref<ApprovalItem[]>([])
const editData = ref<ApprovalItem[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [5, 10, 20, 50]
})
const statusFilter = ref<string | null>(null)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' }
]

const getStatusType = (status: string) => {
  switch (status?.toLowerCase()) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'error'
    default: return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status?.toLowerCase()) {
    case 'pending': return '待审批'
    case 'approved': return '已通过'
    case 'rejected': return '已拒绝'
    default: return status
  }
}

const joinColumns: DataTableColumns<ApprovalItem> = [
  { title: '申请人', key: 'applicantName', width: 120 },
  { title: '申请加入', key: 'relationDesc', minWidth: 150 },
  { title: '申请时间', key: 'createTime', width: 180 },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => h(NTag, { type: getStatusType(row.status), size: 'small' }, { default: () => getStatusText(row.status) })
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) => {
      if (row.status?.toLowerCase() !== 'pending') return null
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { 
            size: 'small', 
            type: 'success',
            onClick: () => handleApprove(row, true)
          }, { default: () => '同意' }),
          h(NButton, { 
            size: 'small', 
            type: 'error',
            onClick: () => handleApprove(row, false)
          }, { default: () => '拒绝' })
        ]
      })
    }
  }
]

const editColumns: DataTableColumns<ApprovalItem> = [
  { title: '申请人', key: 'applicantName', width: 120 },
  { 
    title: '修改字段', 
    key: 'fieldName',
    width: 100,
    render: (row) => {
      const fieldMap: Record<string, string> = {
        name: '姓名',
        avatar: '头像',
        bio: '简介',
        birthDate: '出生日期'
      }
      return fieldMap[row.fieldName || ''] || row.fieldName
    }
  },
  { 
    title: '修改内容', 
    key: 'content',
    minWidth: 200,
    render: (row) => h('div', { class: 'edit-content' }, [
      h('span', { class: 'old-value' }, row.oldValue || '(无)'),
      h('span', { class: 'arrow' }, ' → '),
      h('span', { class: 'new-value' }, row.newValue || '(无)')
    ])
  },
  { title: '申请时间', key: 'createTime', width: 180 },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    align: 'center',
    render: (row) => h(NTag, { type: getStatusType(row.status), size: 'small' }, { default: () => getStatusText(row.status) })
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) => {
      if (row.status?.toLowerCase() !== 'pending') return null
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { 
            size: 'small', 
            type: 'success',
            onClick: () => handleApprove(row, true)
          }, { default: () => '同意' }),
          h(NButton, { 
            size: 'small', 
            type: 'error',
            onClick: () => handleApprove(row, false)
          }, { default: () => '拒绝' })
        ]
      })
    }
  }
]

const fetchApprovals = async () => {
  loading.value = true
  try {
    const res = await getApprovalList({
      type: activeTab.value, 
      status: statusFilter.value || undefined,
      page: pagination.value.page, 
      size: pagination.value.pageSize
    })
    const list = res.data?.records || []
    pagination.value.itemCount = res.data?.total || 0
    
    if (activeTab.value === 'join') {
      joinData.value = list
    } else {
      editData.value = list
    }
  } catch (error: any) {
    message.error(error.message || '获取审批列表失败')
    pagination.value.itemCount = 0
  } finally {
    loading.value = false
  }
}

const handleApprove = (row: ApprovalItem, approved: boolean) => {
  const actionText = approved ? '同意' : '拒绝'
  dialog.warning({
    title: '确认操作',
    content: `确定要${actionText}该申请吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await handleApproval(row.familyId, row.id, { approved })
        message.success(`已${actionText}`)
        fetchApprovals()
      } catch (error: any) {
        message.error(error.message || '操作失败')
      }
    }
  })
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  fetchApprovals()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  fetchApprovals()
}

const handleStatusFilterChange = () => {
  pagination.value.page = 1
  fetchApprovals()
}

watch(activeTab, () => {
  pagination.value.page = 1
  fetchApprovals()
})

onMounted(() => {
  fetchApprovals()
})
</script>

<template>
  <div>
    <NCard>
      <template #header>
        <NSpace justify="space-between" align="center">
          <span class="card-title">审批管理</span>
          <NSelect
            v-model:value="statusFilter"
            :options="statusOptions"
            style="width: 150px"
            placeholder="筛选状态"
            @update:value="handleStatusFilterChange"
          />
        </NSpace>
      </template>
      
      <NTabs v-model:value="activeTab" type="line">
        <NTabPane name="join" tab="加入申请">
          <NDataTable
            :columns="joinColumns"
            :data="joinData"
            :loading="loading"
            :bordered="false"
          />
        </NTabPane>
        <NTabPane name="edit" tab="修改申请">
          <NDataTable
            :columns="editColumns"
            :data="editData"
            :loading="loading"
            :bordered="false"
          />
        </NTabPane>
      </NTabs>

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

:deep(.edit-content) {
  font-size: 13px;
}

:deep(.old-value) {
  color: var(--text-secondary);
  text-decoration: line-through;
}

:deep(.arrow) {
  margin: 0 8px;
  color: var(--text-secondary);
}

:deep(.new-value) {
  color: var(--success-color);
  font-weight: 500;
}
</style>
