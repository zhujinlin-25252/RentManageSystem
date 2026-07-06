<template>
  <div class="ll-orders-page">
    <div class="page-header page-container">
      <h1 class="page-title">
        <el-icon :size="22"><Bell /></el-icon>
        租房申请管理
      </h1>
      <p class="page-subtitle">处理租客的租房申请，确认后房源将变为已出租状态</p>
    </div>

    <div class="stats-row page-container">
      <div
        v-for="stat in stats" :key="stat.key"
        class="stat-card" :class="{ active: currentTab === stat.key }"
        @click="currentTab = stat.key; fetchOrders()"
      >
        <div class="stat-icon-wrap"><span>{{ stat.icon }}</span></div>
        <div class="stat-info">
          <strong>{{ stat.count }}</strong>
          <span>{{ stat.label }}</span>
        </div>
        <span v-if="stat.key === 'pending' && stat.count > 0" class="pulse-dot"></span>
      </div>
    </div>

    <div class="order-list page-container">
      <div v-if="loading" style="padding:40px"><el-skeleton :rows="4" animated /></div>

      <template v-else-if="orders.length > 0">
        <div class="order-card" v-for="order in filteredOrders" :key="order.orderId">
          <div class="card-head">
            <div class="head-left">
              <el-tag :type="statusMap[order.status]?.type || 'info'" size="large" effect="dark" round
                :style="{ background: statusMap[order.status]?.color || '' }">
                {{ statusMap[order.status]?.label || '未知' }}
              </el-tag>
              <span class="order-no">{{ order.orderNo }}</span>
            </div>
            <span class="order-time">{{ formatRelativeTime(order.createTime) }}</span>
          </div>

          <div class="card-body">
            <div class="info-col">
              <router-link :to="`/house/${order.houseId}`" class="house-link">
                <h3 class="house-title">
                  <el-icon :size="16"><House /></el-icon>
                  {{ order.title || '房源标题' }}
                </h3>
              </router-link>
              <div class="tenant-box">
                <div class="info-row">
                  <el-icon :size="14"><User /></el-icon>
                  <span>申请人：<strong>{{ order.contactName }}</strong></span>
                  <el-tag size="small" type="info" round>{{ order.contactPhone }}</el-tag>
                </div>
                <div class="info-row">
                  <el-icon :size="14"><Calendar /></el-icon>
                  <span>期望租期：<b>{{ order.startDate }}</b> 至 <b>{{ order.endDate }}</b></span>
                </div>
                <div v-if="order.remark" class="remark-text">📝 {{ order.remark }}</div>
              </div>
            </div>

            <div class="fee-box">
              <div class="fee-row"><span>月租金</span><strong>¥{{ order.rentMonthly?.toLocaleString() }}/月</strong></div>
              <div class="fee-row"><span>押金</span><strong>¥{{ order.deposit?.toLocaleString() }}</strong></div>
              <div class="fee-divider"></div>
              <div class="fee-row fee-total"><span>总金额</span><strong class="total-num">¥{{ order.totalAmount?.toLocaleString() }}</strong></div>
            </div>
          </div>

          <div class="card-actions">
            <router-link :to="`/house/${order.houseId}`">
              <el-button size="small" plain>查看房源</el-button>
            </router-link>
            <template v-if="order.status === 0">
              <el-button type="primary" size="small" @click="handleConfirm(order)" :loading="confirmingId === order.orderId">确认出租</el-button>
              <el-popconfirm title="确定要拒绝此租房申请吗？" @confirm="handleReject(order)">
                <template #reference><el-button type="danger" size="small" plain>拒绝</el-button></template>
              </el-popconfirm>
            </template>
            <el-tag v-else-if="order.status === 1" type="success" size="small" effect="light" round>已确认 — 房源已出租</el-tag>
            <el-tag v-else-if="order.status === 2" type="info" size="small" effect="light" round>已完成</el-tag>
            <el-tag v-else-if="order.status === 3" type="info" size="small" effect="light" round>租客已取消</el-tag>
            <el-tag v-else-if="order.status === 4" type="danger" size="small" effect="light" round>已拒绝</el-tag>
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
        <el-empty description="暂无租房申请">
          <p class="empty-hint">当租客对你的房源发起租房申请时，会显示在这里</p>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Bell, User, Calendar, House, Select } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReceivedOrders, confirmOrder, rejectOrder } from '@/api/order'
import type { RentOrder } from '@/api/order'

const loading = ref(false)
const orders = ref<RentOrder[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const confirmingId = ref<string | null>(null)
const currentTab = ref('all')

const stats = reactive([
  { key: 'all', label: '全部', icon: '📋', count: 0 },
  { key: 'pending', label: '待确认', icon: '⏳', count: 0 },
  { key: 'confirmed', label: '已确认', icon: '✅', count: 0 },
])

const statusMap: Record<number, { label: string; type: string; color: string }> = {
  0: { label: '待确认', type: 'warning', color: 'linear-gradient(135deg,#f59e0b,#d97706)' },
  1: { label: '租住中', type: 'success', color: 'linear-gradient(135deg,#10b981,#059669)' },
  2: { label: '已完成', type: '', color: 'linear-gradient(135deg,#3b82f6,#2563eb)' },
  3: { label: '租客已取消', type: 'info', color: 'linear-gradient(135deg,#a8a29e,#78716c)' },
  4: { label: '已拒绝', type: 'danger', color: 'linear-gradient(135deg,#ef4444,#dc2626)' },
}

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') return orders.value
  const filterMap: Record<string, number> = { pending: 0, confirmed: 1, rejected: 4 }
  const targetStatus = filterMap[currentTab.value]
  if (targetStatus !== undefined) return orders.value.filter(o => o.status === targetStatus)
  return orders.value
})

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getReceivedOrders(currentPage.value, pageSize.value)
    const list: RentOrder[] = res.records || res.list || []
    orders.value = list
    total.value = res.total || 0
    stats[0].count = total.value
    stats[1].count = list.filter(o => o.status === 0).length
    stats[2].count = list.filter(o => o.status === 1).length
  } catch { ElMessage.error('获取订单列表失败') }
  finally { loading.value = false }
}

async function handleConfirm(order: RentOrder) {
  try {
    await ElMessageBox.confirm(
      `确认接受「${order.contactName}」对「${order.title}」的租房申请？确认后该房源将变为【已出租】状态。`,
      '确认出租',
      { confirmButtonText: '确认出租', cancelButtonText: '再考虑下', type: 'success' }
    )
    confirmingId.value = order.orderId
    await confirmOrder(order.orderId)
    ElMessage.success('已确认！房源已标记为出租')
    fetchOrders()
  } catch (err: any) { if (err !== 'cancel') ElMessage.error(err?.message || '操作失败') }
  finally { confirmingId.value = null }
}

async function handleReject(order: RentOrder) {
  try {
    await rejectOrder(order.orderId)
    ElMessage.success(`已拒绝 ${order.orderNo}`)
    fetchOrders()
  } catch { /* handled */ }
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
.ll-orders-page {
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
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  padding-top: 16px; padding-bottom: 16px;
}

.stat-card {
  position: relative;
  display: flex; align-items: center; gap: 12px;
  padding: 18px 20px; background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md); cursor: pointer;
  transition: all 0.25s ease;

  &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }

  &.active {
    border-color: var(--color-primary);
    box-shadow: 0 4px 16px rgba(249,115,22,0.12);
    strong { color: var(--color-primary); }
  }

  .stat-icon-wrap {
    width: 48px; height: 48px; border-radius: 14px;
    display: flex; align-items: center; justify-content: center;
    font-size: 22px; background: var(--color-bg); flex-shrink: 0;
  }

  .stat-info {
    display: flex; flex-direction: column;
    strong { font-size: 24px; font-weight: 800; line-height: 1.2; }
    span { font-size: 12px; color: var(--color-text-muted); }
  }

  .pulse-dot {
    position: absolute; top: 12px; right: 12px;
    width: 10px; height: 10px; border-radius: 50%; background: #ef4444;
    animation: pulse 1.5s infinite;
  }

  @keyframes pulse {
    0%, 100% { opacity: 1; transform: scale(1); }
    50% { opacity: 0.5; transform: scale(1.3); }
  }
}

.order-list { padding-top: 12px; }

.order-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  margin-bottom: 16px; overflow: hidden;
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

.card-body { display: flex; justify-content: space-between; gap: 24px; padding: 20px 22px; }

.info-col { flex: 1; min-width: 0; }

.house-link {
  text-decoration: none; color: inherit; display: block; margin-bottom: 14px;
  &:hover .house-title { color: var(--color-primary); }
}

.house-title {
  display: flex; align-items: center; gap: 6px;
  font-size: 17px; font-weight: 700; margin: 0; transition: color 0.2s;
  .el-icon { color: var(--color-primary); opacity: 0.7; }
}

.tenant-box {
  background: var(--color-primary-bg);
  border: 1px solid rgba(249,115,22,0.1);
  border-radius: var(--radius-sm); padding: 12px 16px;

  .info-row {
    display: flex; align-items: center; gap: 6px;
    font-size: 13px; color: var(--color-text-secondary); margin: 6px 0;
    .el-icon { color: var(--color-primary); }
    strong { color: var(--color-text); }
    b { color: var(--color-primary); }
  }

  .remark-text {
    font-size: 13px; color: var(--color-text-muted);
    margin-top: 8px; padding-top: 8px;
    border-top: 1px dashed rgba(249,115,22,0.15);
  }
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

.card-actions { display: flex; gap: 10px; padding: 14px 22px; border-top: 1px solid var(--color-border-light); }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0 12px; }
.empty-state { padding: 80px 0; .empty-hint { font-size: 13px; color: var(--color-text-muted); margin-top: 12px; } }
</style>
