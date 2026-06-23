<template>
  <el-container class="layout">
    <el-aside width="224px" class="aside">
      <div class="brand">
        <el-icon><OfficeBuilding /></el-icon>
        <span>服务管理</span>
      </div>
      <el-menu router :default-active="$route.path" background-color="#111827" text-color="#d1d5db" active-text-color="#ffffff">
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>
          <strong>{{ currentLabel }}</strong>
          <span class="muted">核心业务闭环演示</span>
        </div>
        <div class="userbar">
          <el-tag effect="plain">{{ roleLabel }}</el-tag>
          <span>{{ auth.staffName }}</span>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { authApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'
import { menuRoutes } from '../router'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const visibleMenus = computed(() => menuRoutes.filter((item) => item.roles.includes(auth.role)))
const currentLabel = computed(() => menuRoutes.find((item) => item.path === route.path)?.label || '统计看板')
const roleLabel = computed(() => ({ admin: '管理员', receptionist: '接待员', finance: '财务' }[auth.role] || auth.role))

const logout = async () => {
  try {
    await authApi.logout()
  } finally {
    auth.clear()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #111827;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.el-menu {
  border-right: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.header > div:first-child {
  display: flex;
  align-items: center;
  gap: 12px;
}

.userbar {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.main {
  padding: 18px;
  background: #f4f6f8;
}

@media (max-width: 820px) {
  .aside {
    width: 68px !important;
  }

  .brand span,
  .el-menu-item span,
  .header .muted {
    display: none;
  }
}
</style>

