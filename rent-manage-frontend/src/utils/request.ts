import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // 通过 Vite proxy 转发到 localhost:8080/api
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 自动附加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 后端统一返回格式：{ code, message, data }
    if (res.code !== 200) {
      // 支持 silent 模式：调用方可传 _silent: true 跳过自动 toast
      if (!(response.config as any)?._silent) {
        ElMessage.error(res.message || '请求失败')
      }

      // Token 过期/无效 → 跳转登录
      if (res.code === 204 || res.code === 205 || res.code === 206) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push({ name: 'Login' })
      }

      const err: any = new Error(res.message || '请求失败')
      err.code = res.code
      return Promise.reject(err)
    }

    return res.data // 直接返回 data 部分
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      const messages: Record<number, string> = {
        400: '请求参数有误',
        401: '登录已过期，请重新登录',
        403: '没有权限执行此操作',
        404: '请求的资源不存在',
        500: '服务器内部错误',
      }
      ElMessage.error(messages[status] || '网络请求失败')

      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push({ name: 'Login' })
      }
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接服务器，请检查网络或后端是否启动')
    }

    return Promise.reject(error)
  }
)

export default request
