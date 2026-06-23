import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers['X-Auth-Token'] = auth.token
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 0) {
      return body.data
    }
    ElMessage.error(body?.message || '请求失败')
    return Promise.reject(body)
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '服务暂不可用，请确认后端与数据库已启动'
    if (status === 401) {
      useAuthStore().clear()
      router.push('/login')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http

