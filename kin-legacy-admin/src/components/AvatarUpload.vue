<template>
  <div class="avatar-upload">
    <n-upload
      accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
      :max="1"
      :show-file-list="false"
      :custom-request="handleUpload"
      @before-upload="beforeUpload"
    >
      <div class="avatar-wrapper">
        <n-avatar
          v-if="imageUrl"
          :src="imageUrl"
          :size="size"
          round
          fallback-src="https://via.placeholder.com/100"
        />
        <div v-else class="upload-placeholder" :style="{ width: size + 'px', height: size + 'px' }">
          <n-icon :size="Math.min(size / 3, 32)">
            <CameraOutline />
          </n-icon>
          <span>上传头像</span>
        </div>
      </div>
    </n-upload>
    <div v-if="loading" class="loading-mask">
      <n-spin :size="Math.min(size / 3, 32)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { NUpload, NAvatar, NIcon, NSpin } from 'naive-ui'
import { CameraOutline } from '@vicons/ionicons5'
import { uploadFile, UploadResult } from '@/api/upload'

const props = defineProps<{
  modelValue?: string
  size?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const imageUrl = ref(props.modelValue)
const loading = ref(false)

watch(() => props.modelValue, (newVal) => {
  imageUrl.value = newVal
})

const beforeUpload = (options: { file: File }) => {
  const file = options.file
  const isImage = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    window.$message.error('只能上传图片文件 (jpg, png, gif, webp)')
    return false
  }
  if (!isLt5M) {
    window.$message.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUpload = async ({ file }: { file: File }) => {
  loading.value = true
  try {
    const result: UploadResult = await uploadFile(file, 'avatar')
    imageUrl.value = result.fileUrl
    emit('update:modelValue', result.fileUrl)
    window.$message.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    window.$message.error('上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.avatar-upload {
  position: relative;
  display: inline-block;
}

.avatar-wrapper {
  cursor: pointer;
  transition: opacity 0.2s;
}

.avatar-wrapper:hover {
  opacity: 0.8;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  color: #999;
  gap: 4px;
  font-size: 12px;
}

.upload-placeholder span {
  font-size: 12px;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
}
</style>
