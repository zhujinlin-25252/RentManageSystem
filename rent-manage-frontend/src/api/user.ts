import request from '@/utils/request'

/** 用户完整信息（对齐后端 UserVO） */
export interface UserProfile {
  userId: string
  phone: string
  email: string
  nickname: string
  avatarUrl: string
  gender: number        // 0=未知 1=男 2=女
  role: number          // 0=租客 1=房东 2=管理员 3=客服
  roleName: string
  status: number        // 0=未激活 1=正常 2=禁用
  isVerified: number    // 0=未认证 1=已认证
  realName: string      // 已脱敏（如 "张*"）
}

/** 允许更新的用户字段 */
export interface UpdateProfileParams {
  nickname?: string
  gender?: number
  avatarUrl?: string
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return request.get<UserProfile>('/user/info')
}

/** 更新用户信息（昵称/性别/头像） */
export function updateUserInfo(data: UpdateProfileParams) {
  return request.put<UserProfile>('/user/info', data)
}
