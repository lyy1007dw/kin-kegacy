<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const formValue = ref({
  username: '',
  password: ''
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  try {
    loading.value = true
    await formRef.value?.validate()
    await userStore.login(formValue.value.username, formValue.value.password)
    router.push('/')
  } catch (error: any) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="login-bg-pattern"></div>
    </div>
    
    <div class="login-content">
      <div class="login-header">
        <h1 class="login-title">慎重追远</h1>
        <p class="login-subtitle">家谱管理后台</p>
      </div>
      
      <NCard class="login-card">
        <NForm
          ref="formRef"
          :model="formValue"
          :rules="rules"
          label-placement="left"
          label-width="70"
        >
          <NFormItem path="username" label="用户名">
            <NInput 
              v-model:value="formValue.username" 
              placeholder="请输入用户名"
              size="large"
            />
          </NFormItem>
          <NFormItem path="password" label="密码">
            <NInput 
              v-model:value="formValue.password" 
              type="password" 
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
            />
          </NFormItem>
          <NSpace vertical :size="20" style="margin-top: 8px;">
            <NButton 
              type="primary" 
              block 
              size="large"
              :loading="loading" 
              @click="handleLogin"
              class="login-btn"
            >
              登 录
            </NButton>
            <div class="tips">
              <span>默认管理员账号: admin / 123456</span>
            </div>
          </NSpace>
        </NForm>
      </NCard>
      
      <div class="login-footer">
        <p>慎终追远 · 民德归厚</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FDF8F3 0%, #FAF6F1 100%);
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.03;
  background-image: 
    radial-gradient(circle at 20% 30%, #8B5A2B 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, #C4A35A 0%, transparent 50%);
}

.login-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%238B5A2B' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 24px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 36px;
  font-weight: 600;
  color: #3D2914;
  margin: 0 0 12px 0;
  letter-spacing: 8px;
}

.login-subtitle {
  font-size: 12px;
  color: #8B7355;
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.login-card {
  border-radius: 16px !important;
  border: 1px solid #E8DDD4 !important;
  box-shadow: 0 8px 32px rgba(74, 55, 40, 0.1) !important;
  padding: 8px;
}

.login-card :deep(.n-card__content) {
  padding: 28px !important;
}

.login-card :deep(.n-form-item-label) {
  font-weight: 500;
  color: #3D2914;
}

.login-btn {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 8px !important;
  border-radius: 12px !important;
}

.tips {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
  background: rgba(196, 163, 90, 0.1);
  border-radius: 10px;
  font-size: 13px;
  color: #8B7355;
}

.login-footer {
  text-align: center;
  margin-top: 36px;
}

.login-footer p {
  font-size: 13px;
  color: #9C8575;
  margin: 0;
  letter-spacing: 3px;
}
</style>
