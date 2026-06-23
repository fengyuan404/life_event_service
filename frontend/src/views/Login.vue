<template>
  <main class="login-page">
    <section class="login-panel">
      <div>
        <h1>人生大事服务管理系统</h1>
        <p>租赁、缴费、祭扫预约一体化演示后台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" show-password prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <div class="demo-accounts">
        <span>admin01 / hash_admin01</span>
        <span>recv01 / hash_recv01</span>
        <span>fin01 / hash_fin01</span>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin01', password: 'hash_admin01' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await authApi.login(form)
    auth.setAuth(result)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(15, 118, 110, 0.88), rgba(30, 64, 175, 0.82)),
    url("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1600&q=80") center / cover;
}

.login-panel {
  width: min(420px, 100%);
  padding: 28px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  box-shadow: 0 24px 70px rgba(17, 24, 39, 0.26);
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 26px;
  letter-spacing: 0;
}

p {
  margin: 8px 0 24px;
  color: #4b5563;
}

.el-button {
  width: 100%;
}

.demo-accounts {
  display: grid;
  gap: 6px;
  margin-top: 18px;
  color: #64748b;
  font-size: 13px;
}
</style>

