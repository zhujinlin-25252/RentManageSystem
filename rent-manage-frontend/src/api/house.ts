import request from '@/utils/request'

// 房源信息接口
export interface HouseInfo {
  houseId: string
  landlordId?: string
  title: string
  description: string
  address: string
  city: string
  district: string
  area: number // 面积（平方米）
  rentMonthly: number // 月租金
  rooms: number
  halls: number
  bathrooms: number | null
  floor: string
  orientation: string
  paymentType: number
  status: number // 0=待审核 1=已发布 2=已出租 3=下架 4=审核拒绝
  coverImage: string
  facilities?: any
  viewCount: number | null
  favoriteCount: number | null
  createTime: string
  auditRemark?: string | null
  auditTime?: string | null
}

// 分页查询参数
export interface HouseQuery {
  page?: number
  size?: number
  keyword?: string
  city?: string
  district?: string
  minPrice?: number
  maxPrice?: number
  roomCount?: number
  sortBy?: string
}

// ==================== 公开接口 ====================

// 获取公开房源列表
export function getHouseList(params?: HouseQuery) {
  return request.get<any>('/house/public/list', { params })
}

// 获取房源详情（公开），支持传入 axios config（如 { _silent: true } 跳过自动错误提示）
export function getHouseDetail(id: string, config?: Record<string, any>) {
  return request.get<HouseInfo>(`/house/public/${id}`, config)
}

// ==================== 房东管理接口 ====================

// 发布新房源
export function publishHouse(data: Partial<HouseInfo>) {
  return request.post('/house', data)
}

// 编辑房源信息
export function updateHouse(id: string, data: Partial<HouseInfo>) {
  return request.put(`/house/${id}`, data)
}

// 下架房源
export function offShelfHouse(houseId: string) {
  return request.put(`/house/${houseId}/offshelf`)
}

// 重新上架（重新提交审核）
export function onShelfHouse(houseId: string) {
  return request.put(`/house/${houseId}/onshelf`)
}

// 删除房源
export function deleteHouse(houseId: string) {
  return request.delete(`/house/${houseId}`)
}

// 获取我的房源详情（编辑回填，不受状态限制）
export function getMyHouseDetail(houseId: string, config?: Record<string, any>) {
  return request.get<HouseInfo>(`/house/my/${houseId}`, config)
}

// 获取我的房源列表（房东）
export function getMyHouses(page = 1, size = 10) {
  return request.get<any>('/house/my', {
    params: { pageNum: page, pageSize: size },
  })
}

// ==================== 管理员审核接口 ====================

// 审核查询参数
export interface AuditQuery {
  page?: number
  size?: number
  city?: string
}

// 获取待审核房源列表（管理员）
export function getPendingAuditList(params?: AuditQuery) {
  return request.get<any>('/house/admin/audit/list', { params })
}

// 审核通过
export function approveHouse(houseId: string) {
  return request.put(`/house/admin/${houseId}/approve`)
}

// 审核拒绝（需传拒绝原因 remark）
export function rejectHouse(houseId: string, remark: string) {
  return request.put(`/house/admin/${houseId}/reject`, { remark })
}
