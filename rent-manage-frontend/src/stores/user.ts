import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { getUserInfo } from '@/api/user'

// 用户信息接口
export interface UserInfo {
  userId: string
  phone: string
  nickname: string
  avatar?: string
  role: number // 0=租客 1=房东 2=管理员 3=客服
  email?: string
  avatarUrl?: string
  gender?: number
  roleName?: string
  status?: number
  isVerified?: number
  realName?: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null')
  )

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isTenant = computed(() => userInfo.value?.role === 0)
  const isLandlord = computed(() => userInfo.value?.role === 1)
  const isAdmin = computed(() => userInfo.value?.role === 2)

  // 角色名称映射
  const roleName = computed(() => {
    const map: Record<number, string> = {
      0: '租客',
      1: '房东',
      2: '管理员',
      3: '客服',
    }
    return userInfo.value ? map[userInfo.value.role] || '未知' : ''
  })

  // 登录
  async function login(phone: string, password: string) {
    const res = await request.post<any>('/auth/login', { phone, password })
    token.value = res.token
    localStorage.setItem('token', res.token)
    // 存储用户信息
    if (res.userInfo) {
      userInfo.value = res.userInfo as unknown as UserInfo
      localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
    }
    return res
  }

  // 注册
  async function register(data: {
    phone: string
    password: string
    nickname: string
    role: number
    realName?: string
    email?: string
    gender?: number
  }) {
    const res = await request.post('/auth/register', data)
    return res
  }

  // 获取当前用户信息
  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res as unknown as UserInfo
      localStorage.setItem('userInfo', JSON.stringify(res))
    } catch {
      // 静默失败，不影响流程
    }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isTenant,
    isLandlord,
    isAdmin,
    roleName,
    login,
    register,
    fetchUserInfo,
    logout,
  }
})
