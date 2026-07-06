import request from '@/utils/request'

/** 收藏列表项（含房源展示字段） */
export interface FavoriteItem {
  id: number
  houseId: string
  createTime: string
  // 房源展示字段
  title: string
  coverImage: string
  address: string
  city: string
  district: string
  rentMonthly: number
  area: number
  rooms: number
  halls: number
  orientation: string
  status: number
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

/** 添加收藏 */
export function addFavorite(houseId: string) {
  return request.post(`/favorite/${houseId}`)
}

/** 取消收藏 */
export function removeFavorite(houseId: string) {
  return request.delete(`/favorite/${houseId}`)
}

/** 检查是否已收藏某房源 */
export function checkFavorited(houseId: string) {
  return request.get<boolean>(`/favorite/check/${houseId}`)
}

/** 获取我的收藏列表（分页） */
export function getMyFavorites(page = 1, size = 12) {
  return request.get<PageResult<FavoriteItem>>('/favorite/list', {
    params: { page, size },
  })
}
