# Day 13 - 前端性能优化

**开发日期**：第 13 天
**优先级**：P3（较低）
**所属阶段**：体验优化

---

## 一、功能需求描述

### 1.1 背景

前端性能影响用户体验，需要进行优化提升加载速度和响应速度。

### 1.2 优化目标

- 减少首屏加载时间
- 提升路由切换速度
- 优化长列表性能
- 减小打包体积

---

## 二、管理后台优化

### 2.1 路由懒加载

```typescript
// router/index.ts
const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/family',
    component: () => import('@/views/family/index.vue'),
    meta: { title: '家谱管理' }
  },
  {
    path: '/member',
    component: () => import('@/views/member/index.vue'),
    meta: { title: '成员管理' }
  },
  {
    path: '/approval',
    component: () => import('@/views/approval/index.vue'),
    meta: { title: '审批管理' }
  }
]
```

### 2.2 组件按需加载

```typescript
// 按需引入 Element Plus
import { 
  ElButton, 
  ElTable, 
  ElForm, 
  ElDialog,
  ElMessage 
} from 'element-plus'

// 或使用自动导入插件
// vite.config.ts
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
})
```

### 2.3 图片优化

```vue
<template>
  <!-- 使用懒加载 -->
  <img v-lazy="avatar" loading="lazy" />
  
  <!-- 使用 WebP -->
  <picture>
    <source :srcset="avatarWebP" type="image/webp" />
    <img :src="avatar" />
  </picture>
</template>

<script setup>
// 懒加载指令
const lazy = {
  mounted(el) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          el.src = el.dataset.src
          observer.unobserve(el)
        }
      })
    })
    observer.observe(el)
  }
}
</script>
```

### 2.4 长列表虚拟滚动

```vue
<template>
  <div class="virtual-list-container" :style="{ height: containerHeight + 'px' }">
    <div class="virtual-list-phantom" :style="{ height: totalHeight + 'px' }"></div>
    <div 
      class="virtual-list-content" 
      :style="{ transform: `translateY(${offsetY}px)` }"
    >
      <div 
        v-for="item in visibleData" 
        :key="item.id" 
        class="virtual-list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        {{ item.name }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  data: any[]
  itemHeight: number
  containerHeight: number
}>()

const scrollTop = ref(0)

const totalHeight = computed(() => props.data.length * props.itemHeight)

const startIndex = computed(() => Math.floor(scrollTop.value / props.itemHeight))

const endIndex = computed(() => 
  Math.min(startIndex.value + Math.ceil(props.containerHeight / props.itemHeight) + 5, props.data.length)
)

const visibleData = computed(() => 
  props.data.slice(startIndex.value, endIndex.value)
)

const offsetY = computed(() => startIndex.value * props.itemHeight)

let rafId: number
const handleScroll = (e: Event) => {
  if (rafId) cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => {
    scrollTop.value = (e.target as HTMLElement).scrollTop
  })
}
</script>
```

---

## 三、小程序优化

### 3.1 分包加载

```json
// pages.json
{
  "pages": [
    {
      "path": "pages/index/index",
      "style": {
        "navigationBarTitleText": "首页"
      }
    },
    {
      "path": "pages/family/tree",
      "style": {
        "navigationBarTitleText": "家谱树"
      }
    }
  ],
  "subpackages": [
    {
      "root": "pages-sub/admin",
      "name": "admin",
      "pages": [
        {
          "path": "member",
          "style": {
            "navigationBarTitleText": "成员管理"
          }
        },
        {
          "path": "family",
          "style": {
            "navigationBarTitleText": "家谱管理"
          }
        }
      ]
    }
  ]
}
```

### 3.2 数据预加载

```javascript
// onLoad 预加载
onLoad(options) {
  // 预加载家谱列表
  this.loadFamilyList()
  
  // 预加载配置
  this.loadConfig()
}

async loadFamilyList() {
  const cache = uni.getStorageSync('familyList')
  if (cache) {
    this.familyList = cache
  }
  
  const res = await this.$api.get('/api/family')
  this.familyList = res.data.records
  uni.setStorageSync('familyList', res.data.records)
}
```

### 3.3 列表性能优化

```javascript
// 使用 diff 算法减少渲染
const oldList = [/* 旧数据 */]
const newList = [/* 新数据 */]

// 计算差异
function diff(oldList, newList) {
  const result = []
  const oldMap = new Map(oldList.map((item, index) => [item.id, { ...item, index }]))
  
  newList.forEach((item, newIndex) => {
    const oldItem = oldMap.get(item.id)
    if (oldItem) {
      // 更新
      result.push({ type: 'update', index: newIndex, data: item })
      oldMap.delete(item.id)
    } else {
      // 新增
      result.push({ type: 'add', index: newIndex, data: item })
    }
  })
  
  // 删除
  oldMap.forEach((value) => {
    result.push({ type: 'remove', index: value.index })
  })
  
  return result.sort((a, b) => a.index - b.index)
}
```

### 3.4 图片懒加载

```vue
<template>
  <image 
    v-for="item in list" 
    :key="item.id"
    :src="item.avatar"
    :lazy-load="true"
    mode="aspectFill"
  />
</template>
```

---

## 四、打包优化

### 4.1 Vite 配置

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  
  build: {
    // 目标浏览器
    target: 'es2015',
    
    // 打包分块
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
        }
      }
    },
    
    // 压缩
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    
    // 生成 sourcemap
    sourcemap: false,
    
    // 资源内联阈值
    assetsInlineLimit: 5000,
  },
  
  // 依赖优化
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus']
  }
})
```

### 4.2 Gzip 压缩

```typescript
// vite.config.ts
import compression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    compression({
      algorithm: 'gzip',
      ext: '.gz',
    }),
  ],
})
```

---

## 五、接口性能优化

### 5.1 请求合并

```typescript
// 批量请求
async function batchRequest(ids: number[]) {
  // 方案1：后端批量接口
  const res = await api.post('/api/batch', { ids })
  
  // 方案2：Promise.all（慎用，可能导致并发过高）
  const promises = ids.map(id => api.get(`/api/item/${id}`))
  const results = await Promise.all(promises)
}
```

### 5.2 请求缓存

```typescript
// 请求缓存装饰器
const cache = new Map<string, { data: any, time: number }>()
const CACHE_TIME = 5 * 60 * 1000 // 5分钟

function cachedRequest(key: string, request: () => Promise<any>) {
  const cached = cache.get(key)
  if (cached && Date.now() - cached.time < CACHE_TIME) {
    return Promise.resolve(cached.data)
  }
  
  return request().then(data => {
    cache.set(key, { data, time: Date.now() })
    return data
  })
}

// 使用
const data = await cachedRequest('family:1', () => api.get('/api/family/1'))
```

### 5.3 数据压缩

```typescript
// 请求时压缩
const compressed = pako.deflate(JSON.stringify(data))
const response = await fetch('/api/data', {
  method: 'POST',
  body: compressed,
  headers: { 'Content-Encoding': 'gzip' }
})
```

---

## 六、代码优化

### 6.1 减少重渲染

```vue
<script setup>
import { shallowRef, triggerRef } from 'vue'

// 使用 shallowRef 避免深层响应
const largeData = shallowRef({/* 大对象 */})

// 修改后手动触发
function updateData() {
  largeData.value.name = 'newName'
  triggerRef(largeData)
}
</script>
```

### 6.2 防抖节流

```typescript
// 防抖
function debounce(fn: Function, delay: number) {
  let timer: number
  return function(...args: any[]) {
    clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

// 节流
function throttle(fn: Function, delay: number) {
  let last = 0
  return function(...args: any[]) {
    const now = Date.now()
    if (now - last > delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

// 使用
const handleSearch = debounce(() => {
  // 搜索逻辑
}, 300)
```

---

## 七、交付物

- [ ] 路由懒加载配置
- [ ] 组件按需加载
- [ ] 图片懒加载实现
- [ ] 长列表虚拟滚动
- [ ] 小程序分包配置
- [ ] Vite 打包优化
- [ ] 请求缓存实现
- [ ] 防抖节流工具

---

## 八、性能指标

| 指标 | 目标值 |
|------|--------|
| 首屏加载 | < 2s |
| 路由切换 | < 300ms |
| 列表滚动 | 60fps |
| 包体积 | < 500KB |
