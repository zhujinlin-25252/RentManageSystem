import request from '@/utils/request'

/**
 * 上传单张图片
 * @param file 图片文件
 * @param houseId 房源ID（编辑时传入，发布时可不传）
 * @param isCover 是否设为封面
 */
export function uploadImage(file: File, houseId?: string, isCover = false) {
  const formData = new FormData()
  formData.append('file', file)
  if (houseId) formData.append('houseId', houseId)
  if (isCover) formData.append('isCover', 'true')

  return request.post<any>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000, // 大文件给长超时
  })
}

/**
 * 批量上传图片（最多9张）
 */
export function batchUploadImages(files: File[], houseId: string) {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  formData.append('houseId', houseId)

  return request.post<any>('/upload/images/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

/**
 * 获取某房源的所有图片URL列表
 */
export function getHouseImages(houseId: string) {
  return request.get<any>(`/upload/images/${houseId}`)
}
