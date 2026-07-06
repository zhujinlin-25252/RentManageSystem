<template>
  <div class="landlord-page">
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1>
            <el-icon :size="22"><OfficeBuilding /></el-icon>
            我的房源
          </h1>
          <span class="count-badge">共 {{ total }} 套</span>
        </div>
        <el-button type="primary" @click="$router.push('/landlord/publish')">
          <el-icon :size="16"><Plus /></el-icon>
          发布新房源
        </el-button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-row">
        <div v-for="item in statsData" :key="item.label" class="stat-card" :style="{ '--accent': item.color }">
          <span class="stat-num">{{ item.count }}</span>
          <span class="stat-label">{{ item.label }}</span>
          <span class="stat-icon">{{ item.icon }}</span>
        </div>
      </div>

      <!-- 房源列表 -->
      <div v-loading="loading" class="house-list">
        <template v-if="houses.length > 0">
          <div
            v-for="house in houses" :key="house.houseId"
            class="house-item" :class="{ rejected: house.status === 4 }"
          >
            <div class="item-cover">
              <img :src="getCoverImage(house)" :alt="house.title" @error="handleImgError" :data-house-id="house.houseId" :data-title="house.title" />
              <span class="item-status" :class="'s-' + house.status">{{ statusText(house.status) }}</span>
            </div>

            <div class="item-info">
              <h3 class="item-title">{{ house.title }}</h3>
              <p class="item-addr">{{ house.city }} · {{ house.district }} · {{ house.address }}</p>
              <div class="item-tags">
                <span class="item-tag">{{ house.rooms }}室{{ house.halls }}厅{{ house.bathrooms || 0 }}卫</span>
                <span class="item-tag">{{ house.area }}㎡</span>
                <span class="item-tag price-tag">¥{{ house.rentMonthly }}/月</span>
                <span class="item-tag">{{ paymentTypeText(house.paymentType) }}</span>
              </div>
              <div class="item-meta">
                <span>👁 {{ house.viewCount || 0 }}</span>
                <span>❤️ {{ house.favoriteCount || 0 }}</span>
                <span>{{ formatDate(house.createTime) }}</span>
              </div>

              <div v-if="house.status === 4" class="rejection-notice">
                <span class="rej-head">⚠️ 审核未通过</span>
                <span v-if="house.auditTime" class="rej-time">· {{ formatDate(house.auditTime) }}</span>
                <p v-if="house.auditRemark" class="rej-remark">{{ house.auditRemark }}</p>
              </div>
            </div>

            <div class="item-actions">
              <el-button text @click="$router.push(`/house/${house.houseId}`)">查看</el-button>
              <el-button text type="primary" @click="$router.push({ path: '/landlord/publish', query: { id: house.houseId } })">编辑</el-button>
              <el-button
                text
                :type="house.status === 3 ? 'success' : 'warning'"
                :disabled="house.status === 2 || house.status === 0 || house.status === 4"
                @click="handleToggleStatus(house)"
              >
                {{ house.status === 3 ? '重新上架' : '下架' }}
              </el-button>
              <el-button
                text type="danger" :disabled="house.status === 1 || house.status === 2"
                @click="handleDelete(house)"
              >删除</el-button>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="还没有发布任何房源，快去发布吧！">
          <el-button type="primary" @click="$router.push('/landlord/publish')">立即发布</el-button>
        </el-empty>
      </div>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage" :page-size="pageSize" :total="total"
          layout="prev, pager, next" background @current-change="fetchHouses"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { OfficeBuilding, Plus, EditPen, Delete, Top, Bottom, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as houseApi from '@/api/house'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

interface HouseItem {
  houseId: string; title: string; city: string; district: string; address: string
  rooms: number; halls: number; bathrooms: number | null; area: number | null
  rentMonthly: number | null; deposit: number | null; floor: string; orientation: string
  paymentType: number; status: number; coverImage: string
  viewCount: number | null; favoriteCount: number | null; createTime: string
  auditRemark?: string | null; auditTime?: string | null
}

const houses = ref<HouseItem[]>([])

const defaultCover = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="180"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#f97316;stop-opacity:0.12"/><stop offset="100%" style="stop-color:#3b82f6;stop-opacity:0.18"/></linearGradient></defs><rect fill="url(#g)" width="240" height="180"/><text x="50%" y="48%" text-anchor="middle" font-size="40" fill="#bbb">🏠</text><text x="50%" y="65%" text-anchor="middle" font-size="12" fill="#999">暂无图片</text></svg>')

function getCoverImage(house: HouseItem): string {
  if (house.coverImage && house.coverImage.trim().length > 0 && !house.coverImage.startsWith('null')) return house.coverImage
  return defaultCover
}

function handleImgError(e: Event) {
  const img = e.target as HTMLImageElement
  if (img.dataset.fallbacked) return
  img.src = defaultCover
  img.dataset.fallbacked = '1'
}

const statsData = computed(() => [
  { icon: '📊', label: '全部', count: total.value, color: '#3b82f6' },
  { icon: '⏳', label: '待审核', count: houses.value.filter(h => h.status === 0).length, color: '#f59e0b' },
  { icon: '✅', label: '已发布', count: houses.value.filter(h => h.status === 1).length, color: '#10b981' },
  { icon: '🏷️', label: '已出租', count: houses.value.filter(h => h.status === 2).length, color: '#8b5cf6' },
  { icon: '📦', label: '已下架', count: houses.value.filter(h => h.status === 3).length, color: '#a8a29e' },
  { icon: '⚠️', label: '审核拒绝', count: houses.value.filter(h => h.status === 4).length, color: '#ef4444' },
])

function statusText(status: number): string {
  const map: Record<number, string> = { 0: '待审核', 1: '已发布', 2: '已出租', 3: '已下架', 4: '审核拒绝' }
  return map[status] || '未知'
}

function paymentTypeText(type: number): string {
  const map: Record<number, string> = { 0: '月付', 1: '季付', 2: '半年付', 3: '年付' }
  return map[type] || ''
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function fetchHouses(page = 1) {
  loading.value = true
  try {
    const res = await houseApi.getMyHouses(page, pageSize.value)
    houses.value = res.records || []
    total.value = res.total || 0
    currentPage.value = page
  } catch { /* handled */ }
  finally { loading.value = false }
}

async function handleToggleStatus(house: HouseItem) {
  const isOff = house.status === 3
  const action = isOff ? '重新上架' : '下架'
  const msg = isOff ? '上架后需等待管理员审核通过才能公开显示' : '下架后该房源将不再对租客展示'
  try {
    await ElMessageBox.confirm(msg, `确认${action}`, {
      confirmButtonText: isOff ? '提交审核' : '确认下架', cancelButtonText: '取消', type: 'warning',
    })
    if (isOff) {
      await houseApi.onShelfHouse(house.houseId)
      ElMessage.success('已提交上架审核')
    } else {
      await houseApi.offShelfHouse(house.houseId)
      ElMessage.success('房源已下架')
    }
    fetchHouses(currentPage.value)
  } catch (err: any) { if (err !== 'cancel') { /* handled */ } }
}

async function handleDelete(house: HouseItem) {
  if (house.status === 1 || house.status === 2) { ElMessage.warning('请先将房源下架后才能删除'); return }
  try {
    await ElMessageBox.confirm('确定要删除该房源吗？删除后将无法恢复！', '⚠️ 确认删除', {
      confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error',
    })
    await houseApi.deleteHouse(house.houseId)
    ElMessage.success('房源已删除')
    if (houses.value.length <= 1 && currentPage.value > 1) {
      fetchHouses(currentPage.value - 1)
    } else {
      fetchHouses(currentPage.value)
    }
  } catch (err: any) { if (err !== 'cancel') { /* handled */ } }
}

onMounted(() => { fetchHouses(1) })
</script>

<style lang="scss" scoped>
.landlord-page {
  background: var(--color-bg);
  min-height: calc(100vh - 60px - 72px);
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 0 20px;

  h1 {
    font-size: 24px;
    font-weight: 800;
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 0 4px;
    .el-icon { color: var(--color-primary); }
  }

  .count-badge {
    font-size: 13px;
    color: var(--color-text-muted);
  }

  .el-button--primary {
    height: 40px !important;
    border-radius: var(--radius-sm) !important;
    background: linear-gradient(135deg, var(--color-primary), #fb923c) !important;
    border: none !important;
    font-weight: 600 !important;

    &:hover { transform: translateY(-2px); box-shadow: var(--shadow-primary); }
  }
}

// 统计卡片
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 18px 16px;
  border: 1px solid var(--color-border-light);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-fast);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-num {
    display: block;
    font-size: 30px;
    font-weight: 800;
    color: var(--accent);
    line-height: 1.1;
  }

  .stat-label {
    font-size: 12px;
    color: var(--color-text-secondary);
    margin-top: 4px;
  }

  .stat-icon {
    position: absolute;
    right: 14px;
    bottom: 6px;
    font-size: 32px;
    opacity: 0.1;
  }
}

// 房源列表
.house-list { min-height: 300px; }

.house-item {
  display: flex;
  gap: 20px;
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 12px;
  transition: all var(--transition-fast);

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateX(2px);
  }

  &.rejected {
    border-left: 4px solid #ef4444;
    background: linear-gradient(90deg, #fef5f5 0%, #fff 6%);
  }
}

.item-cover {
  width: 200px;
  height: 150px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  position: relative;

  img { width: 100%; height: 100%; object-fit: cover; }

  .item-status {
    position: absolute;
    top: 8px;
    left: 8px;
    padding: 3px 10px;
    border-radius: var(--radius-full);
    font-size: 11px;
    font-weight: 600;
    color: #fff;

    &.s-0 { background: #f59e0b; }
    &.s-1 { background: #10b981; }
    &.s-2 { background: #8b5cf6; }
    &.s-3 { background: #a8a29e; }
    &.s-4 { background: #ef4444; }
  }
}

.item-info {
  flex: 1;
  min-width: 0;

  .item-title {
    font-size: 17px;
    font-weight: 700;
    margin: 0 0 6px;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }

  .item-addr {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 0 0 10px;
  }

  .item-tags {
    display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px;

    .item-tag {
      padding: 3px 10px;
      background: var(--color-bg);
      border-radius: var(--radius-xs);
      font-size: 12px;
      color: var(--color-text-secondary);
      font-weight: 500;

      &.price-tag { color: var(--color-primary); background: var(--color-primary-bg); font-weight: 600; }
    }
  }

  .item-meta {
    display: flex; gap: 16px; font-size: 12px; color: var(--color-text-muted);
  }
}

.item-actions {
  display: flex; flex-direction: column; justify-content: center; gap: 2px; flex-shrink: 0;
  .el-button { font-size: 13px; }
}

.rejection-notice {
  margin-top: 10px;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: var(--radius-sm);
  border-left: 3px solid #ef4444;

  .rej-head { font-size: 12px; font-weight: 700; color: #dc2626; }
  .rej-time { font-size: 11px; color: var(--color-text-muted); }
  .rej-remark { font-size: 13px; color: #991b1b; margin: 4px 0 0; line-height: 1.5; }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
