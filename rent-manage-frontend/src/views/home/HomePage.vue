<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero">
      <div class="hero-pattern"></div>
      <div class="hero-glow hero-glow-1"></div>
      <div class="hero-glow hero-glow-2"></div>

      <div class="hero-content fade-in-up">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          真实房源 · 品质保障
        </div>
        <h1 class="hero-title">
          找到你的<br />
          <span class="hero-highlight">理想居所</span>
        </h1>
        <p class="hero-subtitle">
          专为年轻人打造的高品质租房平台，让每一个城市都有家的温度
        </p>

        <!-- 搜索框 -->
        <div class="hero-search">
          <div class="search-box">
            <el-icon class="search-box-icon" :size="20"><Search /></el-icon>
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索城市、区域、小区名称..."
              class="search-box-input"
              @keyup.enter="goSearch"
            />
            <button class="search-box-btn" @click="goSearch">
              搜索房源
            </button>
          </div>
        </div>

        <!-- 热门标签 -->
        <div class="hot-tags">
          <span class="tags-label">热门搜索</span>
          <button
            v-for="tag in hotTags"
            :key="tag"
            class="tag-link"
            @click="quickSearch(tag)"
          >
            {{ tag }}
          </button>
        </div>
      </div>

      <!-- 浮动装饰卡片 -->
      <div class="hero-float-cards">
        <div class="float-card float-card-1">
          <div class="float-card-icon">🏢</div>
          <div class="float-card-text">
            <strong>精装公寓</strong>
            <span>拎包入住</span>
          </div>
        </div>
        <div class="float-card float-card-2">
          <div class="float-card-icon">🏠</div>
          <div class="float-card-text">
            <strong>整租优选</strong>
            <span>独享空间</span>
          </div>
        </div>
        <div class="float-card float-card-3">
          <div class="float-card-icon">✨</div>
          <div class="float-card-text">
            <strong>品质合租</strong>
            <span>遇见室友</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色服务 -->
    <section class="features-section page-container">
      <div class="section-label">Why Choose Us</div>
      <h2 class="section-heading">为什么选择我们</h2>
      <div class="features-grid">
        <div
          v-for="(f, i) in features"
          :key="f.title"
          class="feature-card fade-in-up"
          :style="{ animationDelay: `${i * 0.08}s` }"
        >
          <div class="feature-icon-wrap">
            <span class="feature-emoji">{{ f.icon }}</span>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 最新房源 -->
    <section class="houses-section page-container">
      <div class="section-header">
        <div>
          <div class="section-label">Latest Listings</div>
          <h2 class="section-heading">最新房源</h2>
        </div>
        <router-link to="/house" class="view-all-link">
          查看全部
          <el-icon :size="16"><ArrowRight /></el-icon>
        </router-link>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton-img"></div>
          <div class="skeleton-body">
            <div class="skeleton-line skeleton-line-1"></div>
            <div class="skeleton-line skeleton-line-2"></div>
            <div class="skeleton-line skeleton-line-3"></div>
          </div>
        </div>
      </div>

      <!-- 房源卡片 -->
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
            <div class="card-img-overlay">
              <span class="card-status">{{ getStatusText(house.status) }}</span>
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ house.title }}</h3>
            <p class="card-addr">
              <el-icon :size="14"><Location /></el-icon>
              {{ house.city }} · {{ house.district }}
            </p>
            <div class="card-specs">
              <span>{{ house.area }}m²</span>
              <span class="spec-divider"></span>
              <span>{{ house.rooms }}室{{ house.halls }}厅</span>
              <span class="spec-divider"></span>
              <span>{{ house.orientation || '朝南' }}</span>
            </div>
            <div class="card-price-row">
              <span class="card-price">
                <em>¥</em>{{ house.rentMonthly?.toLocaleString() || '-' }}
                <small>/月</small>
              </span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-wrap">
        <el-empty description="暂无房源数据" :image-size="100">
          <el-button type="primary" @click="fetchHouses">刷新试试</el-button>
        </el-empty>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Location, ArrowRight } from '@element-plus/icons-vue'
import { getHouseList } from '@/api/house'
import type { HouseInfo } from '@/api/house'

const router = useRouter()
const keyword = ref('')
const houseList = ref<HouseInfo[]>([])
const loading = ref(false)

const hotTags = ['天河', '海珠', '番禺', '白云', '精装修', '近地铁']

const features = [
  {
    icon: '🔍',
    title: '真实房源',
    desc: '所有房源均经过人工审核，杜绝虚假信息，所见即所得',
  },
  {
    icon: '💰',
    title: '透明价格',
    desc: '无中介费、无隐形消费，租金明细一目了然',
  },
  {
    icon: '⚡',
    title: '极速响应',
    desc: '报修工单24小时内处理，管家式贴心服务体验',
  },
  {
    icon: '🛡️',
    title: '安全保障',
    desc: '电子合同+在线支付，全程资金安全保障',
  },
]

function goSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/house', query: { keyword: keyword.value.trim() } })
  }
}

function quickSearch(tag: string) {
  router.push({ path: '/house', query: { keyword: tag } })
}

async function fetchHouses() {
  loading.value = true
  try {
    const res = await getHouseList({ page: 1, size: 6 })
    houseList.value = res.records || res.list || res || []
  } catch {
    houseList.value = []
  } finally {
    loading.value = false
  }
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

onMounted(() => { fetchHouses() })
</script>

<style lang="scss" scoped>
.home-page {
  min-height: calc(100vh - 60px);
  background: var(--color-bg);
}

// ==================== Hero ====================
.hero {
  position: relative;
  padding: 72px 28px 64px;
  overflow: hidden;
  background: linear-gradient(170deg, #fef7f2 0%, #fef2ea 30%, #f0f4ff 100%);

  .hero-pattern {
    position: absolute;
    inset: 0;
    background-image:
      radial-gradient(circle at 15% 85%, rgba(249, 115, 22, 0.06) 0%, transparent 45%),
      radial-gradient(circle at 85% 15%, rgba(59, 130, 246, 0.05) 0%, transparent 45%),
      radial-gradient(circle at 50% 50%, rgba(139, 92, 246, 0.04) 0%, transparent 60%);
    pointer-events: none;
  }

  .hero-glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    pointer-events: none;

    &.hero-glow-1 {
      width: 360px;
      height: 360px;
      top: -120px;
      right: -60px;
      background: rgba(249, 115, 22, 0.08);
    }

    &.hero-glow-2 {
      width: 240px;
      height: 240px;
      bottom: -80px;
      left: -40px;
      background: rgba(59, 130, 246, 0.06);
    }
  }

  .hero-content {
    position: relative;
    max-width: 620px;
    margin: 0 auto;
    text-align: center;
  }

  .hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 16px;
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid var(--color-border-light);
    border-radius: var(--radius-full);
    font-size: 13px;
    font-weight: 500;
    color: var(--color-text-secondary);
    margin-bottom: 24px;
    backdrop-filter: blur(8px);

    .badge-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #10b981;
    }
  }

  .hero-title {
    font-size: 52px;
    font-weight: 800;
    line-height: 1.15;
    letter-spacing: -0.03em;
    color: var(--color-text);
    margin-bottom: 16px;

    .hero-highlight {
      background: linear-gradient(135deg, var(--color-primary) 0%, #fb923c 50%, #f59e0b 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .hero-subtitle {
    font-size: 16px;
    color: var(--color-text-secondary);
    margin-bottom: 36px;
    line-height: 1.6;
    max-width: 440px;
    margin-left: auto;
    margin-right: auto;
  }
}

// 搜索框
.hero-search {
  max-width: 520px;
  margin: 0 auto 24px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 5px;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);

  &:focus-within {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px var(--color-primary-bg), var(--shadow-md);
  }

  .search-box-icon {
    color: var(--color-text-muted);
    margin-left: 14px;
    flex-shrink: 0;
  }

  .search-box-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 12px 14px;
    font-size: 15px;
    color: var(--color-text);
    font-family: inherit;
    background: transparent;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }

  .search-box-btn {
    padding: 10px 28px;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    color: #fff;
    border: none;
    border-radius: var(--radius-md);
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    white-space: nowrap;
    transition: all var(--transition-fast);
    font-family: inherit;

    &:hover {
      box-shadow: 0 4px 16px rgba(249, 115, 22, 0.3);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 热门标签
.hot-tags {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;

  .tags-label {
    font-size: 13px;
    color: var(--color-text-muted);
  }

  .tag-link {
    padding: 5px 16px;
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid var(--color-border-light);
    border-radius: var(--radius-full);
    font-size: 13px;
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: all var(--transition-fast);
    font-family: inherit;
    backdrop-filter: blur(4px);

    &:hover {
      background: #fff;
      border-color: var(--color-primary);
      color: var(--color-primary);
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(249, 115, 22, 0.12);
    }
  }
}

// 浮动装饰卡片
.hero-float-cards {
  display: none;

  @media (min-width: 1100px) {
    display: flex;
    flex-direction: column;
    gap: 14px;
    position: absolute;
    right: 48px;
    top: 50%;
    transform: translateY(-50%);
  }
}

.float-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(12px);
  animation: floatCard 5s ease-in-out infinite;

  .float-card-icon {
    font-size: 28px;
    flex-shrink: 0;
  }

  .float-card-text {
    display: flex;
    flex-direction: column;

    strong {
      font-size: 15px;
      font-weight: 700;
      color: var(--color-text);
    }

    span {
      font-size: 12px;
      color: var(--color-text-muted);
    }
  }

  &.float-card-1 { animation-delay: 0s; }
  &.float-card-2 { animation-delay: 1.2s; }
  &.float-card-3 { animation-delay: 2.4s; }
}

@keyframes floatCard {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

// ==================== 特色服务 ====================
.features-section {
  padding-top: 64px;
  padding-bottom: 20px;
}

.section-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.section-heading {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 36px;
  color: var(--color-text);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 36px 28px;
  background: #fff;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
  opacity: 0;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
    border-color: transparent;
  }

  .feature-icon-wrap {
    width: 52px;
    height: 52px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-primary-bg);
    border-radius: var(--radius-md);
    margin-bottom: 20px;
  }

  .feature-emoji {
    font-size: 24px;
  }

  .feature-title {
    font-size: 17px;
    font-weight: 700;
    margin-bottom: 8px;
    color: var(--color-text);
  }

  .feature-desc {
    font-size: 14px;
    color: var(--color-text-secondary);
    line-height: 1.7;
    margin: 0;
  }
}

// ==================== 房源列表 ====================
.houses-section {
  padding-top: 48px;
  padding-bottom: 72px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
}

.view-all-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  padding: 8px 0;
  transition: gap var(--transition-fast);

  &:hover {
    gap: 8px;
  }
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

    &.skeleton-line-1 { width: 70%; height: 18px; }
    &.skeleton-line-2 { width: 50%; }
    &.skeleton-line-3 { width: 60%; margin-bottom: 0; }
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

    .card-img {
      transform: scale(1.04);
    }
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

    .card-img-overlay {
      position: absolute;
      top: 12px;
      left: 12px;

      .card-status {
        padding: 4px 12px;
        background: rgba(16, 185, 129, 0.9);
        backdrop-filter: blur(4px);
        color: #fff;
        border-radius: var(--radius-full);
        font-size: 12px;
        font-weight: 600;
      }
    }
  }

  .card-content {
    padding: 18px 20px;
  }

  .card-title {
    font-size: 17px;
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
  }

  .card-specs {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    color: var(--color-text-muted);
    margin-bottom: 16px;

    .spec-divider {
      width: 3px;
      height: 3px;
      border-radius: 50%;
      background: var(--color-border);
    }
  }

  .card-price-row {
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
}

.empty-wrap {
  padding: 60px 0;
}
</style>
