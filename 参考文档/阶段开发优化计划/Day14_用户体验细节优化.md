# Day 14 - 用户体验细节优化

**开发日期**：第 14 天
**优先级**：P3（较低）
**所属阶段**：体验优化

---

## 一、功能需求描述

### 1.1 背景

用户体验细节决定产品品质，需要完善各类交互细节提升用户满意度。

### 1.2 优化方向

- 管理后台体验优化
- 小程序体验优化
- 交互细节完善

---

## 二、管理后台优化

### 2.1 面包屑导航

```vue
<template>
  <div class="breadcrumb-container">
    <n-breadcrumb>
      <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
        <router-link :to="item.path">{{ item.title }}</router-link>
      </n-breadcrumb-item>
    </n-breadcrumb>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map((item, index) => ({
    path: item.path,
    title: item.meta.title,
    isLast: index === matched.length - 1
  }))
})
</script>
```

### 2.2 操作确认

```typescript
// 确认删除
const handleDelete = (row) => {
  $dialog.warning({
    title: '确认删除',
    content: `确定要删除成员「${row.name}」吗？此操作不可恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await api.delete(`/api/member/${row.id}`)
      $message.success('删除成功')
      loadData()
    }
  })
}

// 批量确认
const handleBatchDelete = (selection) => {
  $dialog.warning({
    title: '批量删除',
    content: `确定要删除选中的 ${selection.length} 条记录吗？`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      const ids = selection.map(item => item.id)
      await api.post('/api/member/batch-delete', { ids })
      $message.success('删除成功')
      loadData()
    }
  })
}
```

### 2.3 数据导出

```typescript
// 导出 Excel
const handleExport = async () => {
  $dialog.warning({
    title: '导出确认',
    content: '确定要导出当前数据吗？',
    positiveText: '导出',
    negativeText: '取消',
    onPositiveClick: async () => {
      // 获取所有数据
      const { data } = await api.get('/api/member', { 
        ...searchParams, 
        page: 1, 
        size: 10000 
      })
      
      // 生成 Excel
      const worksheet = XLSX.utils.json_to_sheet(data.records)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '成员列表')
      
      // 下载
      XLSX.writeFile(workbook, `成员列表_${dateFormat(new Date())}.xlsx`)
      $message.success('导出成功')
    }
  })
}
```

### 2.4 快捷编辑

```vue
<template>
  <n-modal v-model:show="showModal" preset="card" title="编辑成员">
    <n-form :model="formData" label-placement="left">
      <n-form-item label="姓名">
        <n-input v-model:value="formData.name" />
      </n-form-item>
      <n-form-item label="性别">
        <n-radio-group v-model:value="formData.gender">
          <n-radio>男</n-radio>
          <n-radio>女</n-radio>
        </n-radio-group>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-button @click="showModal = false">取消</n-button>
      <n-button type="primary" @click="handleSave">保存</n-button>
    </template>
  </n-modal>
</template>

<script setup>
const showModal = ref(false)
const formData = ref({})

// 表格中快速编辑
const columns = [
  {
    title: '姓名',
    key: 'name',
    render(row) {
      return h(NEditableText, {
        value: row.name,
        onUpdateValue: (val) => updateField(row.id, 'name', val)
      })
    }
  }
]
</script>
```

---

## 三、小程序优化

### 3.1 加载状态

```vue
<template>
  <view class="container">
    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton">
      <view class="skeleton-avatar"></view>
      <view class="skeleton-title"></view>
      <view class="skeleton-content"></view>
    </view>
    
    <!-- 实际内容 -->
    <view v-else>
      <slot></slot>
    </view>
    
    <!-- 空状态 -->
    <view v-if="!loading && isEmpty" class="empty">
      <image src="/static/empty.png" class="empty-image" />
      <text class="empty-text">暂无数据</text>
    </view>
    
    <!-- 加载更多 -->
    <view v-if="loadingMore" class="loading-more">
      <text>加载中...</text>
    </view>
  </view>
</template>
```

### 3.2 下拉刷新

```javascript
// pages.json
{
  "pages": [
    {
      "path": "pages/member/list",
      "style": {
        "enablePullDownRefresh": true,
        "backgroundTextStyle": "dark"
      }
    }
  ]
}

// pages/member/list.vue
onPullDownRefresh() {
  this.loadData().then(() => {
    uni.stopPullDownRefresh()
  })
},

onReachBottom() {
  if (!this.loadingMore && this.hasMore) {
    this.page++
    this.loadMore()
  }
}
```

### 3.3 骨架屏

```vue
<template>
  <view class="skeleton">
    <view class="skeleton-item" v-for="i in 5" :key="i">
      <view class="skeleton-avatar"></view>
      <view class="skeleton-content">
        <view class="skeleton-title"></view>
        <view class="skeleton-desc"></view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.skeleton {
  padding: 20rpx;
  
  &-item {
    display: flex;
    margin-bottom: 20rpx;
  }
  
  &-avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
  
  &-title {
    width: 60%;
    height: 32rpx;
    margin-bottom: 16rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
  }
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
```

### 3.4 错误提示

```javascript
// 统一错误处理
const showError = (error) => {
  let message = '操作失败'
  
  if (error.response?.data?.message) {
    message = error.response.data.message
  } else if (error.message) {
    message = error.message
  }
  
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })
}

// 使用
try {
  await api.delete(`/api/member/${id}`)
  uni.showToast({ title: '删除成功', icon: 'success' })
} catch (error) {
  showError(error)
}
```

---

## 四、交互细节

### 4.1 表单校验提示

```vue
<template>
  <n-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-placement="left"
  >
    <n-form-item label="姓名" path="name">
      <n-input v-model:value="formData.name" placeholder="请输入姓名" />
      <template #feedback>
        <span style="color: #18a058">请输入真实姓名</span>
      </template>
    </n-form-item>
  </n-form>
</template>
```

### 4.2 操作反馈动画

```css
/* 按钮点击反馈 */
.button:active {
  transform: scale(0.95);
  opacity: 0.8;
}

/* 删除动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 列表项动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
```

### 4.3 手势操作

```javascript
// 长按显示操作菜单
const handleLongPress = (row) => {
  uni.showActionSheet({
    itemList: ['查看详情', '编辑', '删除'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          goToDetail(row.id)
          break
        case 1:
          goToEdit(row.id)
          break
        case 2:
          handleDelete(row)
          break
      }
    }
  })
}

// 左滑删除
const handleSwipeLeft = (row) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除 ${row.name} 吗？`,
    success: (res) => {
      if (res.confirm) {
        deleteRow(row.id)
      }
    }
  })
}
```

---

## 五、Accessibility 优化

### 5.1 语义化标签

```vue
<!-- 不推荐 -->
<div class="button" @click="handleClick">点击</div>

<!-- 推荐 -->
<button @click="handleClick">点击</button>

<!-- 不推荐 -->
<div class="card">
  <div class="title">标题</div>
  <div class="content">内容</div>
</div>

<!-- 推荐 -->
<article>
  <h2>标题</h2>
  <p>内容</p>
</article>
```

### 5.2 焦点管理

```javascript
// 弹窗打开时聚焦第一个输入框
const showModal = ref(false)
watch(showModal, (val) => {
  if (val) {
    nextTick(() => {
      document.querySelector('.modal-input')?.focus()
    })
  }
})

// 键盘收起
uni.onKeyboardHeightChange(res => {
  if (res.height === 0) {
    // 失去焦点
    uni.pageScrollTo({ scrollTop: 0 })
  }
})
```

---

## 六、交付物

- [ ] 面包屑导航组件
- [ ] 操作确认弹窗
- [ ] 数据导出功能
- [ ] 骨架屏组件
- [ ] 下拉刷新配置
- [ ] 错误提示封装
- [ ] 动画效果
- [ ] Accessibility 优化

---

## 七、测试检查清单

| 检查项 | 说明 |
|--------|------|
| 按钮反馈 | 点击有视觉反馈 |
| 加载状态 | 请求时显示加载 |
| 空状态 | 无数据时友好提示 |
| 错误提示 | 失败时明确提示原因 |
| 动画流畅 | 无卡顿 |
| 响应式 | 各尺寸正常 |
