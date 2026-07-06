<template>
  <div class="house-list-page">
    <!-- 搜索筛选区 -->
    <section class="filter-bar">
      <div class="filter-inner page-container">
        <el-input
          v-model="query.keyword"
          placeholder="搜索房源名称、地址..."
          :prefix-icon="Search"
          clearable
          size="large"
          class="filter-search"
          @keyup.enter="handleSearch"
        />

        <div class="filter-controls">
          <el-select v-model="query.city" placeholder="城市" clearable size="large">
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
          </el-select>

          <el-select v-model="query.district" placeholder="区域" clearable size="large">
            <el-option label="天河" value="天河" />
            <el-option label="海珠" value="海珠" />
            <el-option label="番禺" value="番禺" />
            <el-option label="白云" value="白云" />
            <el-option label="越秀" value="越秀" />
            <el-option label="南山" value="南山" />
            <el-option label="福田" value="福田" />
          </el-select>

          <el-select v-model="query.priceRange" placeholder="租金范围" clearable size="large">
            <el-option label="¥2000以下" value="0-2000" />
            <el-option label="¥2000-3500" value="2000-3500" />
            <el-option label="¥3500-5000" value="3500-5000" />
            <el-option label="¥5000以上" value="5000-999999" />
          </el-select>

          <el-select v-model="query.roomCount" placeholder="户型" clearable size="large">
            <el-option label="1室" :value="1" />
            <el-option label="2室" :value="2" />
            <el-option label="3室+" :value="3" />
            <el-option label="4室+" :value="4" />
          </el-select>

          <el-select v-model="query.sortBy" placeholder="排序" size="large">
            <el-option label="最新发布" value="newest" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
          </el-select>

          <el-button type="primary" size="large" @click="handleSearch" class="search-btn">
            <el-icon :size="16"><Search /></el-icon>
            搜索
          </el-button>
        </div>
      </div>
    </section>

    <!-- 结果统计 -->
    <div class="result-bar page-container">
      <p class="result-count">
        共找到 <strong>{{ total }}</strong> 套房源
      </p>
      <span v-if="query.keyword" class="result-keyword">
        关键词："{{ query.keyword }}"
        <button class="clear-keyword" @click="query.keyword = ''; handleSearch()">&times;</button>
      </span>
    </div>

    <!-- 房源列表 / 加载 / 空 -->
    <div class="list-container page-container">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton-img"></div>
          <div class="skeleton-body">
            <div class="skeleton-line w-70 h-18"></div>
            <div class="skeleton-line w-50"></div>
            <div class="skeleton-line w-60"></div>
          </div>
        </div>
      </div>

      <!-- 卡片列表 -->
      <div v-else-if="houseList.length > 0" class="house-grid">
        <article
          v-for="house in houseList"
          :key="house.houseId"
          class="house-card fade-in-up"
          @click="$router.push(`/house/${house.houseId}`)"
        >
          <div class="card-img-wrap">
            <img
              :src="getCoverImage(house)"
              :alt="house.title"
              class="card-img"
              @error="handleImgError"
              :data-house-id="house.houseId"
              :data-title="house.title"
            />
            <span class="card-status" :class="'status-' + house.status">
              {{ getStatusText(house.status) }}
            </span>
            <div class="card-img-gradient"></div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ house.title }}</h3>
            <p class="card-addr">
              <el-icon :size="14"><Location /></el-icon>
              {{ house.city }}{{ house.district }} · {{ house.address }}
            </p>
            <div class="card-tags">
              <span class="tag">{{ house.area }}m²</span>
              <span class="tag">{{ house.rooms }}室{{ house.halls }}厅</span>
              <span class="tag">{{ house.orientation || '-' }}</span>
              <span class="tag">{{ house.floor || '-' }}</span>
            </div>
            <div class="card-footer">
              <span class="card-price">
                <em>¥</em>{{ house.rentMonthly?.toLocaleString() || '-' }}
                <small>/月</small>
              </span>
              <span class="card-time">{{ formatTime(house.createTime) }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-wrap">
        <el-empty description="没有找到符合条件的房源" :image-size="100">
          <el-button type="primary" @click="resetFilters">清空筛选条件</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrap page-container">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[6, 12, 18]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="fetchHouses"
        @size-change="fetchHouses"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Location } from '@element-plus/icons-vue'
import { getHouseList } from '@/api/house'
import type { HouseInfo } from '@/api/house'

const route = useRoute()
const router = useRouter()

const query = reactive({
  keyword: (route.query.keyword as string) || '',
  city: (route.query.city as string) || '',
  district: (route.query.district as string) || '',
  priceRange: (route.query.priceRange as string) || '',
  roomCount: route.query.roomCount ? Number(route.query.roomCount) : (null as number | null),
  sortBy: (route.query.sortBy as string) || 'newest',
  page: route.query.page ? Number(route.query.page) : 1,
  size: route.query.size ? Number(route.query.size) : 12,
})

const houseList = ref<HouseInfo[]>([])
const total = ref(0)
const loading = ref(false)

async function fetchHouses() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.city) params.city = query.city
    if (query.district) params.district = query.district
    if (query.priceRange) {
      const [min, max] = query.priceRange.split('-').map(Number)
      params.minPrice = min
      params.maxPrice = max
    }
    if (query.roomCount) params.roomCount = query.roomCount
    if (query.sortBy && query.sortBy !== 'newest') params.sortBy = query.sortBy

    const res = await getHouseList(params)
    houseList.value = res.records || res.list || []
    total.value = res.total || res.totalSize || houseList.value.length
  } catch {
    houseList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  const q: Record<string, any> = {}
  if (query.keyword) q.keyword = query.keyword
  if (query.city) q.city = query.city
  if (query.district) q.district = query.district
  if (query.priceRange) q.priceRange = query.priceRange
  if (query.roomCount) q.roomCount = query.roomCount
  if (query.sortBy && query.sortBy !== 'newest') q.sortBy = query.sortBy
  router.replace({ query: q })
  fetchHouses()
}

function resetFilters() {
  query.keyword = ''
  query.city = ''
  query.district = ''
  query.priceRange = ''
  query.roomCount = null
  query.sortBy = 'newest'
  query.page = 1
  router.replace({ query: {} })
  fetchHouses()
}

function getCoverImage(house: HouseInfo): string {
  if (house.coverImage && house.coverImage.trim().length > 0 && !house.coverImage.startsWith('null'))
    return house.coverImage
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b', '#ef4444']
  const idx = (house.houseId?.charCodeAt(0) || 0) % colors.length
  return genPlaceholder(colors[idx], (house.title || '🏠').substring(0, 6), `${house.rooms || '-'}室${house.halls || '-'}厅 · ${house.area || '?'}m²`)
}

function handleImgError(e: Event) {
  const img = e.target as HTMLImageElement
  if (img.dataset.fallbacked) return
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b']
  img.src = genPlaceholder(colors[Math.floor(Math.random() * colors.length)], (img.dataset.title || '🏠').substring(0, 6), '')
  img.dataset.fallbacked = '1'
}

function genPlaceholder(color: string, text: string, info: string): string {
  const iSvg = info ? `<text x='50%' y='80%' dominant-baseline='middle' text-anchor='middle' font-size='14' fill='white' opacity='0.7'>${info}</text>` : ''
  return `data:image/svg+xml,${encodeURIComponent(`<svg xmlns='http://www.w3.org/2000/svg' width='400' height='300'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop offset='0%' stop-color='${color}'/><stop offset='100%' stop-color='${color}aa'/></linearGradient></defs><rect fill='url(%23g)' width='400' height='300'/><text x='50%' y='42%' dominant-baseline='middle' text-anchor='middle' font-size='48' fill='white' opacity='0.9'>🏠</text><text x='50%' y='65%' dominant-baseline='middle' text-anchor='middle' font-size='18' fill='white' font-weight='bold'>${text}</text>${iSvg}</svg>`)}`
}

function getStatusText(status: number): string {
  const map: Record<number, string> = { 0: '待审核', 1: '已发布', 2: '已出租', 3: '已下架', 4: '审核拒绝' }
  return map[status] || '未知'
}

function formatTime(time: string): string {
  const d = new Date(time)
  const diff = Date.now() - d.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天发布'
  if (days === 1) return '昨天发布'
  if (days < 7) return `${days}天前`
  return time.substring(5, 10)
}

onMounted(() => { fetchHouses() })
</script>

<style lang="scss" scoped>
.house-list-page {
  min-height: calc(100vh - 60px);
  background: var(--color-bg);
  padding-bottom: 48px;
}

// ==================== 筛选栏 ====================
.filter-bar {
  position: sticky;
  top: 60px;
  z-index: 50;
  padding: 16px 0;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--color-border-light);
}

.filter-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-search {
  width: 280px;
  flex-shrink: 0;
}

.filter-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;

  :deep(.el-select) {
    width: 140px;
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--radius-sm) !important;
  }
}

.search-btn {
  height: 40px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-sm) !important;
}

// ==================== 统计栏 ====================
.result-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 28px 8px;
}

.result-count {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;

  strong {
    font-size: 22px;
    font-weight: 800;
    color: var(--color-primary);
    margin: 0 2px;
  }
}

.result-keyword {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-muted);
  background: var(--color-bg);
  padding: 2px 8px;
  border-radius: var(--radius-full);

  .clear-keyword {
    border: none;
    background: none;
    font-size: 16px;
    cursor: pointer;
    color: var(--color-text-muted);
    padding: 0;
    line-height: 1;

    &:hover { color: var(--color-danger); }
  }
}

// ==================== 列表 ====================
.list-container {
  padding-top: 16px;
}

// 骨架屏
.loading-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.skeleton-card {
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);

  .skeleton-img {
    height: 200px;
    background: linear-gradient(90deg, var(--color-bg) 25%, var(--color-border-light) 50%, var(--color-bg) 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }

  .skeleton-body {
    padding: 20px;
  }

  .skeleton-line {
    height: 14px;
    border-radius: 4px;
    background: linear-gradient(90deg, var(--color-bg) 25%, var(--color-border-light) 50%, var(--color-bg) 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    margin-bottom: 10px;

    &.w-70 { width: 70%; }
    &.w-50 { width: 50%; }
    &.w-60 { width: 60%; }
    &.h-18 { height: 18px; }
  }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

// 房源网格
.house-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

// 房源卡片
.house-card {
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  opacity: 0;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
    border-color: transparent;

    .card-img { transform: scale(1.04); }
  }

  .card-img-wrap {
    position: relative;
    height: 210px;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f5f4, #e7e5e4);

    .card-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    .card-img-gradient {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60px;
      background: linear-gradient(transparent, rgba(0,0,0,0.3));
      pointer-events: none;
    }

    .card-status {
      position: absolute;
      top: 12px;
      left: 12px;
      padding: 3px 10px;
      border-radius: var(--radius-full);
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      z-index: 1;

      &.status-0 { background: #f59e0b; }
      &.status-1 { background: #10b981; }
      &.status-2 { background: #3b82f6; }
      &.status-3 { background: #a8a29e; }
      &.status-4 { background: #ef4444; }
    }
  }

  .card-content {
    padding: 18px 20px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--color-text);
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .card-addr {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 0 0 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .card-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 14px;

    .tag {
      padding: 3px 10px;
      background: var(--color-bg);
      border-radius: var(--radius-xs);
      font-size: 12px;
      color: var(--color-text-secondary);
      font-weight: 500;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 14px;
    border-top: 1px solid var(--color-border-light);
  }

  .card-price {
    font-size: 22px;
    font-weight: 800;
    color: var(--color-primary);

    em {
      font-style: normal;
      font-size: 15px;
      font-weight: 500;
    }

    small {
      font-size: 13px;
      font-weight: 400;
      color: var(--color-text-muted);
    }
  }

  .card-time {
    font-size: 12px;
    color: var(--color-text-muted);
  }
}

.empty-wrap {
  padding: 60px 0;
}

// 分页
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}
</style>
