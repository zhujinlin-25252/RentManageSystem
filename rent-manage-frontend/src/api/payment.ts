import request from '@/utils/request'

/** 支付单信息 */
export interface PaymentOrder {
  orderId: string
  orderNo: string
  contractId?: string
  payerId: string
  payeeId: string
  orderType: number  // 1=押金+租金
  amount: number
  status: number     // 0=待支付 2=已支付
  paidAt?: string
  expireAt: string
  createTime: string
}

/** 查询是否已设置支付密码 */
export function hasPaymentPassword() {
  return request.get<boolean>('/user/has-payment-password')
}

/** 设置/修改支付密码 */
export function setPaymentPassword(password: string, newPassword: string) {
  return request.put('/user/payment-password', { password, newPassword })
}

/** 根据租房订单ID查询关联支付单 */
export function getPaymentByRentOrder(rentOrderId: string) {
  return request.get<PaymentOrder>(`/payment/order/by-rent/${rentOrderId}`)
}

/** 支付（验证支付密码） */
export function payOrder(paymentOrderId: string, password: string) {
  return request.put<PaymentOrder>(`/payment/order/${paymentOrderId}/pay`, { password })
}
