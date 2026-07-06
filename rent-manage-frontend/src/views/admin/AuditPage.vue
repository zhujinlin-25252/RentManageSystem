<template>
  <div class="audit-page">
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1>
            <el-icon :size="22"><Stamp /></el-icon>
            房源审核管理
          </h1>
          <span class="pending-badge">当前有 <strong>{{ total }}</strong> 套房源待审核</span>
        </div>
        <el-select v-model="filterCity" placeholder="筛选城市" clearable @change="handleFilter" style="width:160px" size="large">
          <el-option label="全部城市" value="" />
          <el-option v-for="city in cityOptions" :key="city" :label="city" :value="city" />
        </el-select>
      </div>

      <!-- 统计 -->
      <div class="stats-row">
        <div class="stat-card stat-pending">
          <span class="stat-num">{{ total }}</span>
          <span class="stat-label">待审核</span>
          <span class="stat-icon">⏳</span>
        </div>
        <div class="stat-card stat-today">
          <span class="stat-num">{{ todayCount }}</span>
          <span class="stat-label">今日新增</span>
          <span class="stat-icon">📅</span>
        </div>
      </div>

      <!-- 审核列表 -->
      <div v-loading="loading" class="audit-list">
        <template v-if="houses.length > 0">
          <div v-for="house in houses" :key="house.houseId" class="audit-card">
            <div class="audit-cover">
              <img :src="getCoverImage(house)" :alt="house.title" @error="handleImgError" :data-house-id="house.houseId" :data-title="house.title" />
              <span class="time-tag">{{ formatTimeAgo(house.createTime) }}</span>
            </div>

            <div class="audit-info">
              <h3 class="audit-title">{{ house.title }}</h3>
              <p class="audit-addr">{{ house.city }} · {{ house.district }} · {{ house.address }}</p>
              <div class="audit-tags">
                <span class="a-tag">{{ house.rooms }}室{{ house.halls }}厅{{ house.bathrooms || 0 }}卫</span>
                <span class="a-tag">{{ house.area }}㎡</span>
                <span class="a-tag price-tag">¥{{ house.rentMonthly }}/月</span>
                <span class="a-tag">{{ paymentTypeText(house.paymentType) }}</span>
              </div>
              <p v-if="house.description" class="audit-desc">{{ truncateDesc(house.description) }}</p>
              <div class="audit-meta">
                <span>房东ID: {{ house.landlordId?.slice(0, 8) }}...</span>
                <span>提交于 {{ formatDate(house.createTime) }}</span>
              </div>
            </div>

            <div class="audit-actions">
              <el-button text @click="$router.push(`/house/${house.houseId}`)">查看</el-button>
              <el-button type="primary" size="large" @click="handleApprove(house)" :loading="house._approving" class="btn-approve">
                通过
              </el-button>
              <el-button type="danger" plain size="large" @click="openRejectDialog(house)" :loading="house._rejecting" class="btn-reject">
                拒绝
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="暂无待审核的房源">
          <template #image><span style="font-size:64px">🎉</span></template>
          <p style="color:var(--color-text-muted);margin-top:8px">所有房源已处理完毕，辛苦了！</p>
        </el-empty>
      </div>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage" :page-size="pageSize" :total="total"
          layout="prev, pager, next" background @current-change="fetchAuditList"
        />
      </div>
    </div>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="审核拒绝" width="480px" :close-on-click-modal="false" destroy-on-close>
      <p class="reject-tip">请填写拒绝原因，帮助房东了解问题所在并修改后重新提交：</p>
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="例如：房源图片模糊不清、地址信息不完整、租金明显偏离市场价格..." maxlength="200" show-word-limit />
      <div class="quick-reasons">
        <span class="quick-label">快捷选择：</span>
        <el-tag v-for="reason in quickRejectReasons" :key="reason" effect="plain" class="quick-tag" @click="rejectReason = reason">{{ reason }}</el-tag>
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" :loading="rejectLoading" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Stamp, Select, CloseBold, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as houseApi from '@/api/house'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterCity = ref('')

interface AuditHouse {
  houseId: string; title: string; city: string; district: string; address: string
  landlordId: string; rooms: number; halls: number; bathrooms: number | null
  area: number | null; rentMonthly: number | null; deposit: number | null
  floor: string; orientation: string; paymentType: number; status: number
  coverImage: string; description?: string; createTime: string
  _approving?: boolean; _rejecting?: boolean
}

const houses = ref<AuditHouse[]>([])

const cityOptions = computed(() => {
  const cities = new Set(houses.value.map(h => h.city).filter(Boolean))
  return Array.from(cities).sort()
})

const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return houses.value.filter(h => h.createTime?.startsWith(today)).length
})

const defaultCover = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="180"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#f59e0b;stop-opacity:0.15"/><stop offset="100%" style="stop-color:#ef4444;stop-opacity:0.25"/></linearGradient></defs><rect fill="url(#g)" width="240" height="180"/><text x="50%" y="48%" text-anchor="middle" font-size="40" fill="#bbb">🏠</text><text x="50%" y="65%" text-anchor="middle" font-size="12" fill="#999">待审核</text></svg>')

function getCoverImage(house: AuditHouse): string {
  if (house.coverImage && house.coverImage.trim().length > 0 && !house.coverImage.startsWith('null')) return house.coverImage
  return defaultCover
}

function handleImgError(e: Event) {
  const img = e.target as HTMLImageElement
  if (img.dataset.fallbacked) return
  img.src = defaultCover
  img.dataset.fallbacked = '1'
}

function paymentTypeText(type: number): string {
  const map: Record<number, string> = { 0: '月付', 1: '季付', 2: '半年付', 3: '年付' }
  return map[type] || ''
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

function formatTimeAgo(dateStr: string): string {
  if (!dateStr) return ''
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  return `${Math.floor(hours / 24)}天前`
}

function truncateDesc(desc: string, maxLen = 80): string {
  if (!desc) return ''
  return desc.length > maxLen ? desc.slice(0, maxLen) + '...' : desc
}

async function fetchAuditList(page = 1) {
  loading.value = true
  try {
    const params: any = { pageNum: page, pageSize: pageSize.value }
    if (filterCity.value) params.city = filterCity.value
    const res = await houseApi.getPendingAuditList(params)
    houses.value = (res.records || []).map((h: AuditHouse) => ({ ...h, _approving: false, _rejecting: false }))
    total.value = res.total || 0
    currentPage.value = page
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleFilter() { fetchAuditList(1) }

async function handleApprove(house: AuditHouse) {
  try {
    await ElMessageBox.confirm(`确认通过「${house.title}」的审核？`, '审核通过', {
      confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'success',
    })
    house._approving = true
    await houseApi.approveHouse(house.houseId)
    ElMessage.success('已通过审核并发布')
    fetchAuditList(currentPage.value)
  } catch (err: any) { if (err !== 'cancel') { /* handled */ } }
  finally { house._approving = false }
}

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)
let currentRejectHouse: AuditHouse | null = null

const quickRejectReasons = [
  '房源图片模糊或不清晰', '地址信息不完整或不准确',
  '租金价格异常（过高或过低）', '描述包含违规内容或联系方式', '户型面积与实际不符',
]

function openRejectDialog(house: AuditHouse) {
  currentRejectHouse = house
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!currentRejectHouse || !rejectReason.value.trim()) return
  try {
    rejectLoading.value = true
    await houseApi.rejectHouse(currentRejectHouse.houseId, rejectReason.value.trim())
    ElMessage.success('已拒绝该房源并告知房东原因')
    rejectDialogVisible.value = false
    fetchAuditList(currentPage.value)
  } catch { /* handled */ }
  finally { rejectLoading.value = false; currentRejectHouse = null }
}

onMounted(() => { fetchAuditList(1) })
</script>

<style lang="scss" scoped>
.audit-page {
  background: var(--color-bg);
  min-height: calc(100vh - 60px - 72px);
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 0 20px;
  flex-wrap: wrap; gap: 16px;

  h1 {
    font-size: 24px; font-weight: 800;
    display: flex; align-items: center; gap: 10px;
    margin: 0 0 4px;
    .el-icon { color: var(--color-primary); }
  }

  .pending-badge {
    font-size: 14px; color: var(--color-text-muted);
    strong { color: #f59e0b; font-size: 20px; font-weight: 800; }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  margin-bottom: 24px;
  max-width: 400px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 18px 20px;
  border: 1px solid var(--color-border-light);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-fast);

  &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }

  .stat-num { display: block; font-size: 30px; font-weight: 800; line-height: 1.1; }
  .stat-label { font-size: 13px; color: var(--color-text-secondary); margin-top: 4px; }
  .stat-icon { position: absolute; right: 14px; bottom: 6px; font-size: 36px; opacity: 0.1; }

  &.stat-pending { border-left: 4px solid #f59e0b; .stat-num { color: #f59e0b; } }
  &.stat-today { border-left: 4px solid #3b82f6; .stat-num { color: #3b82f6; } }
}

.audit-list { min-height: 300px; }

.audit-card {
  display: flex; gap: 20px;
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 20px; margin-bottom: 12px;
  transition: all var(--transition-fast);

  &:hover { box-shadow: var(--shadow-md); transform: translateX(2px); }
}

.audit-cover {
  width: 180px; height: 135px;
  border-radius: var(--radius-sm);
  overflow: hidden; flex-shrink: 0; position: relative;

  img { width: 100%; height: 100%; object-fit: cover; }

  .time-tag {
    position: absolute; bottom: 6px; left: 6px;
    padding: 2px 8px; border-radius: var(--radius-full);
    font-size: 11px; background: rgba(0,0,0,0.6); color: #fff; backdrop-filter: blur(4px);
  }
}

.audit-info {
  flex: 1; min-width: 0;

  .audit-title { font-size: 17px; font-weight: 700; margin: 0 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .audit-addr { font-size: 13px; color: var(--color-text-secondary); margin: 0 0 10px; }
  .audit-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }

  .a-tag {
    padding: 3px 10px; background: var(--color-bg); border-radius: var(--radius-xs);
    font-size: 12px; color: var(--color-text-secondary); font-weight: 500;

    &.price-tag { color: var(--color-primary); background: var(--color-primary-bg); font-weight: 600; }
  }

  .audit-desc {
    font-size: 13px; color: var(--color-text-muted); line-height: 1.5; margin: 0 0 8px;
    padding: 8px 12px; background: var(--color-bg); border-radius: var(--radius-sm);
  }

  .audit-meta { display: flex; gap: 16px; font-size: 12px; color: var(--color-text-muted); }
}

.audit-actions {
  display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; justify-content: center;

  .btn-approve {
    min-width: 80px;
    background: linear-gradient(135deg, #10b981, #059669) !important;
    border: none !important;
    &:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(16,185,129,0.35); }
  }
  .btn-reject {
    &:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(239,68,68,0.25); }
  }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }

.reject-tip { font-size: 14px; color: var(--color-text-secondary); margin: 0 0 14px; line-height: 1.5; }

.quick-reasons {
  margin-top: 14px; display: flex; align-items: flex-start; flex-wrap: wrap; gap: 8px;

  .quick-label { font-size: 13px; color: var(--color-text-muted); white-space: nowrap; margin-top: 4px; }
  .quick-tag { cursor: pointer; transition: all 0.2s;
    &:hover { color: #ef4444; border-color: #ef4444; transform: scale(1.03); }
  }
}
</style>
