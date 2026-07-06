import request from '@/utils/request'

// 租房订单信息接口
export interface RentOrder {
  orderId: string
  orderNo: string
  houseId: string
  tenantId: string
  landlordId: string
  title: string
  rentMonthly: number // 月租金
  deposit: number      // 押金
  startDate: string    // 租期开始
  endDate: string      // 租期结束
  totalAmount: number  // 总金额
  contactName: string
  contactPhone: string
  remark: string
  status: number       // 0=待确认 1=已确认 2=已完成 3=租客取消 4=房东拒绝
  paymentOrderId?: string  // 关联支付单ID
  createTime: string
}

// 提交租房订单参数
export interface CreateOrderParams {
  houseId: string
  startDate: string
  endDate: string
  contactName: string
  contactPhone: string
  remark?: string
}

// ==================== 租客订单接口 ====================

// 提交租房申请（创建订单）
export function createOrder(data: CreateOrderParams) {
  return request.post<RentOrder>('/order', data)
}

// 获取我的订单列表（租客）
export function getMyOrders(page = 1, size = 10) {
  return request.get<any>('/order/my', {
    params: { pageNum: page, pageSize: size },
  })
}

// 取消订单（仅待确认状态可取消）
export function cancelOrder(orderId: string) {
  return request.put(`/order/${orderId}/cancel`)
}

// ==================== 房东订单接口 ====================

// 获取收到的订单列表（房东）
export function getReceivedOrders(page = 1, size = 10) {
  return request.get<any>('/order/received', {
    params: { pageNum: page, pageSize: size },
  })
}

// 确认订单（房东确认后房源变为已出租）
export function confirmOrder(orderId: string) {
  return request.put(`/order/${orderId}/confirm`)
}

// 拒绝订单（房东拒绝租房申请）
export function rejectOrder(orderId: string) {
  return request.put(`/order/${orderId}/reject`)
}
