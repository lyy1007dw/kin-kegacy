<script setup lang="ts">
import { ref, onMounted, h, reactive } from 'vue'
import { 
  NDataTable, NButton, NSpace, NCard, NModal, 
  NForm, NFormItem, NInput, NSelect, NDatePicker, 
  useMessage, useDialog, NPagination, NGrid, NGridItem
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { 
  getMemberList, updateMember, deleteMember, addMember,
  updateMemberByAdmin, checkMemberTransfer, type Member, type MemberQueryParams
} from '@/api/member'
import { getFamilyList, getNonAdminUsers, type User } from '@/api/admin'
import { formatValue, formatDate } from '@/utils/format'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const showModal = ref(false)
const showAddModal = ref(false)
const showTransferModal = ref(false)
const editingMember = ref<Member | null>(null)
const userOptions = ref<Array<{label: string, value: number}>>([])
const familyOptions = ref<Array<{label: string, value: number}>>([])
const transferCheckResult = ref<{ warnings: string[], affectedRelations: number } | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [5, 10, 20, 50]
})

const searchForm = reactive<MemberQueryParams>({
  name: '',
  gender: undefined,
  birthDateStart: undefined,
  birthDateEnd: undefined,
  genealogyId: undefined,
  createTimeStart: undefined,
  createTimeEnd: undefined
})

const formValue = reactive({
  userId: null as number | null,
  familyId: null as number | null,
  name: '',
  gender: 'male' as string,
  birthDate: '',
  bio: ''
})

const newMemberForm = reactive({
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

const genderSearchOptions = [
  { label: '全部', value: undefined },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' }
]

const roleOptions = [
  { label: '普通成员', value: 'MEMBER' },
  { label: '管理员', value: 'ADMIN' }
]

const columns: DataTableColumns<Member> = [
  { title: 'ID', key: 'id', width: 80, align: 'center', render: (row) => formatValue(row.id) },
  { title: '姓名', key: 'name', width: 120, render: (row) => formatValue(row.name) },
  { title: '性别', key: 'gender', width: 80, align: 'center', render: (row) => row.gender === 'male' ? '男' : (row.gender === 'female' ? '女' : '-') },
  { title: '出生日期', key: 'birthDate', width: 120, render: (row) => formatValue(row.birthDate) },
  { title: '所属家谱', key: 'familyName', width: 150, render: (row) => formatValue(row.familyName || row.genealogyName) },
  { title: '角色', key: 'accountRole', width: 100, align: 'center', render: (row) => row.accountRole === 'ADMIN' ? '管理员' : '成员' },
  { title: '简介', key: 'bio', ellipsis: { tooltip: true }, render: (row) => formatValue(row.bio) },
  { title: '创建时间', key: 'createTime', width: 180, render: (row) => formatDate(row.createTime) },
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
    const params: MemberQueryParams = {
      page: pagination.page,
      size: pagination.pageSize,
      name: searchForm.name || undefined,
      gender: searchForm.gender || undefined,
      birthDateStart: searchForm.birthDateStart || undefined,
      birthDateEnd: searchForm.birthDateEnd || undefined,
      genealogyId: searchForm.genealogyId || undefined,
      createTimeStart: searchForm.createTimeStart || undefined,
      createTimeEnd: searchForm.createTimeEnd || undefined
    }
    const res = await getMemberList(params)
    data.value = res.data?.records || []
    pagination.itemCount = res.data?.total || 0
  } catch (error) {
    message.error('获取成员列表失败')
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.gender = undefined
  searchForm.birthDateStart = undefined
  searchForm.birthDateEnd = undefined
  searchForm.genealogyId = undefined
  searchForm.createTimeStart = undefined
  searchForm.createTimeEnd = undefined
  pagination.page = 1
  fetchData()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchData()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.page = 1
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
  if (!newMemberForm.userId || !newMemberForm.familyId || !newMemberForm.name) {
    message.warning('请填写必要信息')
    return
  }
  try {
    await addMember({
      userId: newMemberForm.userId,
      familyId: newMemberForm.familyId,
      name: newMemberForm.name,
      gender: newMemberForm.gender,
      birthDate: newMemberForm.birthDate || undefined,
      bio: newMemberForm.bio || undefined
    })
    message.success('添加成功')
    showAddModal.value = false
    Object.assign(newMemberForm, {
      userId: null,
      familyId: null,
      name: '',
      gender: 'male',
      birthDate: '',
      bio: ''
    })
    fetchData()
  } catch (error: any) {
    message.error(error.message || '添加失败')
  }
}

const handleCloseAddModal = () => {
  showAddModal.value = false
  Object.assign(newMemberForm, {
    userId: null,
    familyId: null,
    name: '',
    gender: 'male',
    birthDate: '',
    bio: ''
  })
}

const handleEdit = (row: Member) => {
  editingMember.value = row
  Object.assign(formValue, {
    userId: row.userId,
    familyId: row.familyId || row.genealogyId,
    name: row.name,
    gender: row.gender,
    birthDate: row.birthDate || '',
    bio: row.bio || ''
  })
  showModal.value = true
}

const handleCheckTransfer = async () => {
  if (!editingMember.value || !formValue.familyId) return
  
  if (formValue.familyId === editingMember.value.familyId) {
    transferCheckResult.value = null
    return
  }

  try {
    const res = await checkMemberTransfer(editingMember.value.id, formValue.familyId)
    transferCheckResult.value = res.data
    if (res.data.warnings && res.data.warnings.length > 0) {
      showTransferModal.value = true
    }
  } catch (error: any) {
    message.error(error.message || '检查迁移失败')
  }
}

const handleUpdate = async () => {
  if (!editingMember.value) return
  if (!formValue.name) {
    message.warning('请填写成员姓名')
    return
  }
  try {
    await updateMemberByAdmin(editingMember.value.id, {
      name: formValue.name,
      gender: formValue.gender as any,
      birthDate: formValue.birthDate || undefined,
      bio: formValue.bio || undefined,
      genealogyId: formValue.familyId || undefined
    })
    message.success('更新成功')
    showModal.value = false
    editingMember.value = null
    Object.assign(formValue, {
      userId: null, familyId: null, name: '', gender: 'male', birthDate: '', bio: ''
    })
    fetchData()
  } catch (error: any) {
    message.error(error.message || '更新失败')
  }
}

const confirmTransfer = async () => {
  showTransferModal.value = false
  await handleUpdate()
}

const handleDelete = (row: Member) => {
  const familyId = row.familyId || row.genealogyId
  if (!familyId) {
    message.error('无法获取家谱ID')
    return
  }
  dialog.warning({
    title: '确认删除',
    content: `确定要删除成员 "${row.name}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMember(familyId, row.id)
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
  transferCheckResult.value = null
  Object.assign(formValue, {
    userId: null, familyId: null, name: '', gender: 'male', birthDate: '', bio: ''
  })
}
</script>

<template>
  <div>
    <NCard class="search-card">
      <NForm :model="searchForm" inline>
        <NGrid :cols="24" :x-gap="16">
          <NGridItem :span="6">
            <NFormItem label="姓名">
              <NInput v-model:value="searchForm.name" placeholder="请输入姓名" clearable />
            </NFormItem>
          </NGridItem>
          <NGridItem :span="6">
            <NFormItem label="性别">
              <NSelect v-model:value="searchForm.gender" :options="genderSearchOptions" clearable />
            </NFormItem>
          </NGridItem>
          <NGridItem :span="6">
            <NFormItem label="所属家谱">
              <NSelect v-model:value="searchForm.genealogyId" :options="familyOptions" placeholder="请选择" clearable />
            </NFormItem>
          </NGridItem>
          <NGridItem :span="6">
            <NFormItem label="出生日期">
              <NDatePicker 
                v-model:formatted-value="searchForm.birthDateStart"
                value-format="yyyy-MM-dd"
                type="date" 
                placeholder="开始"
                clearable
                style="width: 48%"
              />
              <span style="margin: 0 8px">-</span>
              <NDatePicker 
                v-model:formatted-value="searchForm.birthDateEnd"
                value-format="yyyy-MM-dd"
                type="date" 
                placeholder="结束"
                clearable
                style="width: 48%"
              />
            </NFormItem>
          </NGridItem>
        </NGrid>
        <NSpace justify="end">
          <NButton type="primary" @click="handleSearch">搜索</NButton>
          <NButton @click="handleReset">重置</NButton>
        </NSpace>
      </NForm>
    </NCard>
    
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
        <NFormItem label="所属家谱">
          <NSelect 
            v-model:value="formValue.familyId" 
            :options="familyOptions" 
            placeholder="选择家谱"
            @update:value="handleCheckTransfer"
          />
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
      v-model:show="showTransferModal" 
      preset="card" 
      title="迁移确认" 
      style="width: 450px"
    >
      <div v-if="transferCheckResult">
        <p style="margin-bottom: 12px; color: #f0a020;">迁移将产生以下影响：</p>
        <ul style="padding-left: 20px; color: #666;">
          <li v-for="(warning, idx) in transferCheckResult.warnings" :key="idx">{{ warning }}</li>
        </ul>
      </div>
      <template #footer>
        <NButton @click="showTransferModal = false">取消</NButton>
        <NButton type="warning" @click="confirmTransfer">确认迁移</NButton>
      </template>
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

.search-card {
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
