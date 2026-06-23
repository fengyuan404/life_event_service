import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Families from '../views/Families.vue'
import Deceased from '../views/Deceased.vue'
import Graves from '../views/Graves.vue'
import Rents from '../views/Rents.vue'
import Payments from '../views/Payments.vue'
import SacrificeBooks from '../views/SacrificeBooks.vue'

export const menuRoutes = [
  { path: '/dashboard', label: '统计看板', icon: 'DataAnalysis', roles: ['admin', 'receptionist', 'finance'], component: Dashboard },
  { path: '/families', label: '家属管理', icon: 'User', roles: ['admin', 'receptionist'], component: Families },
  { path: '/deceased', label: '逝者档案', icon: 'Collection', roles: ['admin', 'receptionist'], component: Deceased },
  { path: '/graves', label: '墓区墓穴', icon: 'Place', roles: ['admin', 'receptionist'], component: Graves },
  { path: '/rents', label: '租赁办理', icon: 'Tickets', roles: ['admin', 'receptionist', 'finance'], component: Rents },
  { path: '/payments', label: '缴费管理', icon: 'Wallet', roles: ['admin', 'finance'], component: Payments },
  { path: '/sacrifice-books', label: '祭扫预约', icon: 'Calendar', roles: ['admin', 'receptionist'], component: SacrificeBooks }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: menuRoutes.map((route) => ({
        path: route.path,
        component: route.component,
        meta: { roles: route.roles }
      }))
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.loggedIn) {
    return '/login'
  }
  if (to.path === '/login' && auth.loggedIn) {
    return '/dashboard'
  }
  const roles = to.meta.roles
  if (roles && !roles.includes(auth.role)) {
    return '/dashboard'
  }
})

export default router

