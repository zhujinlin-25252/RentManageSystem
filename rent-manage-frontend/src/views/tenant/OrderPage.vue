<template>
  <div class="order-page">
    <div class="page-header page-container">
      <h1 class="page-title">
        <el-icon :size="22"><Document /></el-icon>
        我的租房订单
      </h1>
      <p class="page-subtitle">查看和管理你的所有租房申请</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row page-container">
      <div
        v-for="stat in stats" :key="stat.key"
        class="stat-card" :class="{ active: currentTab === stat.key }"
        @click="currentTab = stat.key; fetchOrders()"
      >
        <div class="stat-icon-wrap">
          <span>{{ stat.icon }}</span>
        </div>
        <div class="stat-info">
          <strong>{{ stat.count }}</strong>
          <span>{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list page-container">
      <div v-if="loading" style="padding:40px"><el-skeleton :rows="4" animated /></div>

      <template v-else-if="orders.length > 0">
        <div class="order-card" v-for="order in orders" :key="order.orderId">
          <div class="card-head">
            <div class="head-left">
              <el-tag
                :type="statusMap[order.status]?.type || 'info'"
                size="large" effect="dark" round
                :style="{ background: statusMap[order.status]?.color || '' }"
              >
                {{ statusMap[order.status]?.label || '未知' }}
              </el-tag>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
            </div>
            <span class="order-time">{{ formatRelativeTime(order.createTime) }}</span>
          </div>

          <div class="card-body">
            <router-link :to="`/house/${order.houseId}`" class="house-link">
              <h3 class="house-title">{{ order.title || '房源标题' }}</h3>
              <p class="house-meta">📅 {{ order.startDate }} ~ {{ order.endDate }}</p>
              <p class="house-meta">👤 {{ order.contactName }} · {{ order.contactPhone }}</p>
            </router-link>
            <div class="fee-box">
              <div class="fee-row"><span>月租金</span><strong>¥{{ order.rentMonthly?.toLocaleString() }}/月</strong></div>
              <div class="fee-row"><span>押金</span><strong>¥{{ order.deposit?.toLocaleString() }}</strong></div>
              <div class="fee-divider"></div>
              <div class="fee-row fee-total"><span>总金额</span><strong class="total-num">¥{{ order.totalAmount?.toLocaleString() }}</strong></div>
            </div>
          </div>

          <div v-if="order.remark" class="remark-block">
            <span class="remark-label">备注：</span>{{ order.remark }}
          </div>

          <div class="card-actions">
            <router-link :to="`/house/${order.houseId}`">
              <el-button size="small" plain>查看房源</el-button>
            </router-link>
            <el-button
              v-if="order.status === 0"
              type="danger" size="small" plain
              :loading="cancellingId === order.orderId" @click="handleCancel(order)"
            >取消订单</el-button>
            <template v-else-if="order.status === 1">
              <el-button type="primary" size="small" @click="handlePay(order)">去支付</el-button>
              <el-tag type="success" size="small" effect="light" round>房东已确认</el-tag>
            </template>
            <el-tag v-else-if="order.status === 2" type="info" size="small" effect="light" round>已完成</el-tag>
            <el-tag v-else-if="order.status === 3" type="warning" size="small" effect="light" round>已取消</el-tag>
            <el-tag v-else-if="order.status === 4" type="danger" size="small" effect="light" round>房东已拒绝</el-tag>
          </div>
        </div>

        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage" :page-size="pageSize" :total="total"
            layout="prev, pager, next" @current-change="fetchOrders" background
          />
        </div>
      </template>

      <div v-else class="empty-state">
        <el-empty description="暂无租房订单">
          <el-button type="primary" @click="$router.push('/house')">去找房</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="确认支付" width="420px" :close-on-click-modal="false">
      <div class="pay-body" v-if="payingOrder">
        <div class="pay-summary">
          <p class="pay-title">{{ payingOrder.title }}</p>
          <div class="pay-items">
            <div class="pay-row"><span>押金</span><strong>¥{{ payingOrder.deposit?.toLocaleString() }}</strong></div>
            <div class="pay-row"><span>首月租金</span><strong>¥{{ payingOrder.rentMonthly?.toLocaleString() }}</strong></div>
            <div class="pay-divider"></div>
            <div class="pay-row pay-total-row"><span>合计支付</span><strong>¥{{ payAmount?.toLocaleString() }}</strong></div>
          </div>
        </div>
        <el-form @submit.prevent="confirmPay">
          <el-form-item label="支付密码">
            <el-input v-model="payPassword" type="password" show-password maxlength="6" placeholder="请输入6位支付密码" @keyup.enter="confirmPay" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, cancelOrder } from '@/api/order'
import type { RentOrder } from '@/api/order'
import { hasPaymentPassword, getPaymentByRentOrder, payOrder } from '@/api/payment'

const router = useRouter()

const loading = ref(false)
const orders = ref<RentOrder[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const cancellingId = ref<string | null>(null)
const currentTab = ref('all')

const payDialogVisible = ref(false)
const payingOrder = ref<RentOrder | null>(null)
const payPassword = ref('')
const paying = ref(false)

const payAmount = computed(() => {
  if (!payingOrder.value) return 0
  return (payingOrder.value.deposit || 0) + (payingOrder.value.rentMonthly || 0)
})

const stats = reactive([
  { key: 'all', label: '全部订单', icon: '📋', count: 0 },
  { key: 'pending', label: '待确认', icon: '⏳', count: 0 },
  { key: 'confirmed', label: '已确认', icon: '✅', count: 0 },
  { key: 'completed', label: '已完成', icon: '🏠', count: 0 },
])

const statusMap: Record<number, { label: string; type: string; color: string }> = {
  0: { label: '待确认', type: 'warning', color: 'linear-gradient(135deg,#f59e0b,#d97706)' },
  1: { label: '租住中', type: 'success', color: 'linear-gradient(135deg,#10b981,#059669)' },
  2: { label: '已完成', type: '', color: 'linear-gradient(135deg,#3b82f6,#2563eb)' },
  3: { label: '已取消', type: 'danger', color: 'linear-gradient(135deg,#a8a29e,#78716c)' },
  4: { label: '房东已拒绝', type: 'danger', color: 'linear-gradient(135deg,#ef4444,#dc2626)' },
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getMyOrders(currentPage.value, pageSize.value)
    const list: RentOrder[] = res.records || res.list || []
    orders.value = list
    total.value = res.total || 0
    stats[0].count = total.value
    stats[1].count = list.filter(o => o.status === 0).length
    stats[2].count = list.filter(o => o.status === 1).length
    stats[3].count = list.filter(o => o.status === 2).length
  } catch { ElMessage.error('获取订单列表失败') }
  finally { loading.value = false }
}

async function handleCancel(order: RentOrder) {
  try {
    await ElMessageBox.confirm(`确定要取消订单 ${order.orderNo} 吗？`, '取消订单', {
      confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning',
    })
    cancellingId.value = order.orderId
    await cancelOrder(order.orderId)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (err: any) { if (err !== 'cancel') ElMessage.error(err?.message || '取消失败') }
  finally { cancellingId.value = null }
}

async function handlePay(order: RentOrder) {
  try {
    const hasPwd = await hasPaymentPassword()
    if (!hasPwd) {
      await ElMessageBox.confirm('您尚未设置支付密码，请先在个人信息中设置。', '提示', {
        confirmButtonText: '去设置', cancelButtonText: '取消', type: 'warning',
      })
      router.push({ name: 'UserProfile' })
      return
    }
  } catch (err: any) { if (err !== 'cancel') return; return }

  try {
    const payment = await getPaymentByRentOrder(order.orderId)
    if (!payment) { ElMessage.warning('未找到支付单'); return }
    if (payment.status !== 0) { ElMessage.info('该订单已支付或已过期'); return }
    payingOrder.value = order
    payPassword.value = ''
    payDialogVisible.value = true
  } catch { ElMessage.error('获取支付信息失败') }
}

async function confirmPay() {
  if (!payPassword.value || payPassword.value.length !== 6) { ElMessage.warning('请输入6位支付密码'); return }
  if (!payingOrder.value) return
  try {
    const payment = await getPaymentByRentOrder(payingOrder.value.orderId)
    if (!payment) { ElMessage.warning('支付单不存在'); return }
    paying.value = true
    await payOrder(payment.orderId, payPassword.value)
    ElMessage.success('支付成功！')
    payDialogVisible.value = false
    payPassword.value = ''
    payingOrder.value = null
    fetchOrders()
  } catch { /* handled */ }
  finally { paying.value = false }
}

function formatRelativeTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const diffMin = Math.floor((Date.now() - date.getTime()) / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 30) return `${diffDay}天前`
  return dateStr.substring(0, 10)
}

onMounted(() => { fetchOrders() })
</script>

<style lang="scss" scoped>
.order-page {
  min-height: calc(100vh - 60px);
  background: var(--color-bg);
  padding-bottom: 40px;
}

.page-header {
  padding-top: 24px; padding-bottom: 8px;

  .page-title {
    display: flex; align-items: center; gap: 10px;
    font-size: 24px; font-weight: 800; margin: 0 0 6px;
    .el-icon { font-size: 22px; color: var(--color-primary); }
  }
  .page-subtitle { font-size: 14px; color: var(--color-text-muted); margin: 0; }
}

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px;
  padding-top: 16px; padding-bottom: 16px;
}

.stat-card {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }

  &.active {
    border-color: var(--color-primary);
    box-shadow: 0 4px 16px rgba(249,115,22,0.12);
    strong { color: var(--color-primary); }
  }

  .stat-icon-wrap {
    width: 44px; height: 44px; border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px; background: var(--color-bg); flex-shrink: 0;
  }

  .stat-info {
    display: flex; flex-direction: column;
    strong { font-size: 22px; font-weight: 800; line-height: 1.2; }
    span { font-size: 12px; color: var(--color-text-muted); }
  }
}

.order-list { padding-top: 12px; }

.order-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.25s ease;

  &:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
}

.card-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 22px; border-bottom: 1px solid var(--color-border-light);

  .head-left { display: flex; align-items: center; gap: 12px; }
  .order-no { font-size: 13px; color: var(--color-text-muted); font-family: monospace; }
  .order-time { font-size: 12px; color: var(--color-text-muted); }
}

.card-body {
  display: flex; justify-content: space-between; gap: 24px; padding: 20px 22px;
}

.house-link {
  text-decoration: none; color: inherit; flex: 1;

  &:hover .house-title { color: var(--color-primary); }

  .house-title { font-size: 17px; font-weight: 700; margin: 0 0 10px; transition: color 0.2s; }
  .house-meta { font-size: 13px; color: var(--color-text-secondary); margin: 6px 0; }
}

.fee-box {
  min-width: 180px; text-align: right;
  padding: 12px 16px; background: var(--color-bg); border-radius: var(--radius-sm);

  .fee-row {
    display: flex; justify-content: space-between; align-items: center;
    padding: 4px 0; font-size: 13px;
    span { color: var(--color-text-muted); }
    strong { color: var(--color-text-secondary); }
  }
  .fee-divider { height: 1px; background: var(--color-border-light); margin: 8px 0; }
  .fee-total {
    padding-top: 6px;
    span { font-weight: 700; color: var(--color-text); font-size: 14px; }
  }
  .total-num { font-size: 20px !important; font-weight: 800 !important; color: var(--color-primary) !important; }
}

.remark-block {
  padding: 10px 22px; font-size: 13px; color: var(--color-text-secondary);
  background: var(--color-primary-bg); border-top: 1px solid rgba(249,115,22,0.08);
  .remark-label { color: var(--color-primary); font-weight: 500; }
}

.card-actions {
  display: flex; gap: 10px; padding: 14px 22px; border-top: 1px solid var(--color-border-light);
}

.pagination-wrap { display: flex; justify-content: center; padding: 24px 0 12px; }
.empty-state { padding: 80px 0; }

.pay-body {
  .pay-summary {
    background: var(--color-bg); border-radius: var(--radius-sm); padding: 16px; margin-bottom: 20px;
    .pay-title { font-size: 15px; font-weight: 600; margin: 0 0 12px; }
  }
  .pay-items {
    .pay-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px;
      span { color: var(--color-text-muted); }
      strong { color: var(--color-text-secondary); }
    }
    .pay-divider { height: 1px; background: var(--color-border-light); margin: 8px 0; }
    .pay-total-row strong { font-size: 22px; font-weight: 800; color: var(--color-primary); }
  }
}
</style>
