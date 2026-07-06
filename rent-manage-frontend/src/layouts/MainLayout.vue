<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="navbar" :class="{ 'is-scrolled': isScrolled }">
      <div class="navbar-inner">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <span class="logo-mark">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="url(#logo-grad)"/>
              <path d="M14 6L6 12v10h6v-7h4v7h6V12l-8-6z" fill="white" opacity="0.95"/>
              <defs>
                <linearGradient id="logo-grad" x1="0" y1="0" x2="28" y2="28">
                  <stop stop-color="#f97316"/><stop offset="1" stop-color="#fb923c"/>
                </linearGradient>
              </defs>
            </svg>
          </span>
          <span class="logo-text">青年品质租房</span>
        </router-link>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/home" class="nav-item" active-class="is-active">
            <el-icon :size="16"><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/house" class="nav-item" active-class="is-active">
            <el-icon :size="16"><OfficeBuilding /></el-icon>
            <span>找房</span>
          </router-link>

          <!-- 租客：我的订单 -->
          <router-link
            v-if="userStore.isLoggedIn && !userStore.isLandlord && !userStore.isAdmin"
            to="/tenant/orders"
            class="nav-item nav-tenant"
            active-class="is-active"
          >
            <el-icon :size="16"><Tickets /></el-icon>
            <span>我的订单</span>
          </router-link>

          <!-- 房东专属 -->
          <template v-if="userStore.isLandlord">
            <router-link to="/landlord/publish" class="nav-item nav-landlord" active-class="is-active">
              <el-icon :size="16"><Plus /></el-icon>
              <span>发布房源</span>
            </router-link>
            <router-link to="/landlord/houses" class="nav-item nav-landlord" active-class="is-active">
              <el-icon :size="16"><List /></el-icon>
              <span>我的房源</span>
            </router-link>
            <router-link to="/landlord/orders" class="nav-item nav-landlord" active-class="is-active">
              <el-icon :size="16"><Bell /></el-icon>
              <span>订单管理</span>
              <el-badge
                v-if="landlordPendingCount > 0"
                :value="landlordPendingCount"
                :max="99"
                class="nav-badge"
              />
            </router-link>
          </template>

          <!-- 管理员专属 -->
          <template v-if="userStore.isAdmin">
            <router-link to="/admin/audit" class="nav-item nav-admin" active-class="is-active">
              <el-icon :size="16"><Stamp /></el-icon>
              <span>审核管理</span>
              <el-badge
                v-if="pendingAuditCount > 0"
                :value="pendingAuditCount"
                :max="99"
                class="nav-badge"
              />
            </router-link>
          </template>
        </nav>

        <!-- 右侧用户区 -->
        <div class="nav-right">
          <!-- 搜索框 -->
          <div class="search-wrapper" :class="{ 'is-focused': searchFocused }">
            <el-icon class="search-icon" :size="16"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索房源..."
              class="search-input"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
              @keyup.enter="handleSearch"
            />
            <button
              v-if="searchKeyword"
              class="search-clear"
              @click="searchKeyword = ''"
            >
              <el-icon :size="12"><Close /></el-icon>
            </button>
          </div>

          <!-- 已登录：用户下拉 -->
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand" popper-class="user-popper">
              <div class="user-trigger">
                <el-avatar :size="34" :style="{ backgroundColor: avatarColor }" class="user-avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
                <el-icon class="chevron" :size="14"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <div class="popper-user-info">
                    <el-avatar :size="40" :style="{ backgroundColor: avatarColor }">
                      {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div>
                      <div class="popper-name">{{ userStore.userInfo?.nickname || '用户' }}</div>
                      <div class="popper-role">{{ userStore.roleName }}</div>
                    </div>
                  </div>
                  <el-dropdown-item divided command="profile">
                    <el-icon><Setting /></el-icon>个人设置
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" class="logout-item">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 未登录 -->
          <template v-else>
            <el-button type="primary" class="login-btn" @click="$router.push('/login')">
              登录 / 注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="footer-logo-text">青年品质租房</span>
          <span class="footer-dot">·</span>
          <span class="footer-desc">为年轻人打造的高品质居住体验</span>
        </div>
        <p class="footer-copy">&copy; 2026 青年品质租房管理系统 · 连梓祺 & 团队</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  OfficeBuilding,
  Search,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Plus,
  List,
  Stamp,
  Tickets,
  Bell,
  Close,
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 搜索
const searchKeyword = ref('')
const searchFocused = ref(false)

// 角标数量
const pendingAuditCount = ref(0)
const landlordPendingCount = ref(0)

// 滚动状态
const isScrolled = ref(false)

// 头像颜色（基于用户 ID）
const avatarColor = computed(() => {
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b', '#ec4899']
  const idx = (userStore.userInfo?.userId?.charCodeAt(0) || 0) % colors.length
  return colors[idx]
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/house', query: { keyword: searchKeyword.value.trim() } })
  }
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
      })
      userStore.logout()
      router.push({ name: 'Login' })
    } catch { /* 取消 */ }
  } else if (command === 'profile') {
    router.push({ name: 'UserProfile' })
  }
}

function handleScroll() {
  isScrolled.value = window.scrollY > 20
}

async function fetchPendingAuditCount() {
  try {
    const { getPendingAuditList } = await import('@/api/house')
    const res = await getPendingAuditList({ page: 1, size: 1 })
    pendingAuditCount.value = res.total || 0
  } catch { /* 静默 */ }
}

async function fetchLandlordPendingCount() {
  try {
    const { getReceivedOrders } = await import('@/api/order')
    const res = await getReceivedOrders(1, 100)
    const list = res.records || res.list || []
    landlordPendingCount.value = list.filter((o: any) => o.status === 0).length
  } catch { /* 静默 */ }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  if (userStore.isAdmin) fetchPendingAuditCount()
  if (userStore.isLandlord) fetchLandlordPendingCount()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// ==================== 导航栏 ====================
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  transition: all var(--transition-normal);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border-bottom: 1px solid transparent;

  &.is-scrolled {
    background: rgba(255, 255, 255, 0.88);
    border-bottom-color: var(--color-border-light);
    box-shadow: 0 1px 0 var(--color-border-light), 0 4px 24px rgba(0, 0, 0, 0.04);
  }
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 28px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 8px;
}

// Logo
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  margin-right: 8px;

  .logo-mark {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--color-text);
    letter-spacing: -0.02em;
    white-space: nowrap;
  }

  &:hover {
    .logo-text { color: var(--color-primary); }
    .logo-mark { transform: scale(1.05); }
    .logo-mark { transition: transform var(--transition-fast); }
  }
}

// 导航菜单
.nav-menu {
  display: flex;
  gap: 2px;
  flex: 1;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-weight: 500;
  font-size: 14px;
  transition: all var(--transition-fast);
  position: relative;
  white-space: nowrap;

  &:hover {
    color: var(--color-text);
    background: var(--color-bg);
  }

  &.is-active {
    color: var(--color-primary);
    background: var(--color-primary-bg);
    font-weight: 600;
  }

  // 房东专属样式
  &.nav-landlord {
    &.is-active {
      color: #fff;
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));

      .el-icon { color: #fff; }
    }
  }

  // 租客订单样式
  &.nav-tenant {
    &.is-active {
      color: #fff;
      background: linear-gradient(135deg, #3b82f6, #60a5fa);

      .el-icon { color: #fff; }
    }
  }

  // 管理员样式
  &.nav-admin {
    &.is-active {
      color: #fff;
      background: linear-gradient(135deg, #ef4444, #f59e0b);

      .el-icon { color: #fff; }
    }
  }

  .nav-badge {
    margin-left: 2px;

    :deep(.el-badge__content) {
      font-size: 10px;
      height: 16px;
      line-height: 16px;
      padding: 0 5px;
      transform: scale(0.88);
    }
  }
}

// 右侧区域
.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}

// 搜索框
.search-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: var(--color-bg);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  width: 200px;

  &:hover {
    background: #fff;
    border-color: var(--color-border-light);
  }

  &.is-focused {
    background: #fff;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px var(--color-primary-bg);
    width: 240px;
  }

  .search-icon {
    color: var(--color-text-muted);
    flex-shrink: 0;
  }

  .search-input {
    border: none;
    outline: none;
    background: transparent;
    font-size: 14px;
    color: var(--color-text);
    width: 100%;
    font-family: inherit;

    &::placeholder {
      color: var(--color-text-muted);
      font-size: 13px;
    }
  }

  .search-clear {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2px;
    border: none;
    background: var(--color-border-light);
    border-radius: 50%;
    cursor: pointer;
    color: var(--color-text-muted);
    flex-shrink: 0;

    &:hover {
      background: var(--color-border);
      color: var(--color-text-secondary);
    }
  }
}

// 用户下拉触发
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--color-bg);
  }

  .user-avatar {
    flex-shrink: 0;
    font-weight: 600;
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    max-width: 72px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--color-text);
  }

  .chevron {
    color: var(--color-text-muted);
    transition: transform var(--transition-fast);
  }

  &:hover .chevron {
    transform: translateY(2px);
  }
}

// 登录按钮
.login-btn {
  height: 38px !important;
  padding: 0 22px !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  border-radius: var(--radius-full) !important;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.25);

  &:hover {
    box-shadow: 0 4px 16px rgba(249, 115, 22, 0.35) !important;
    transform: translateY(-1px);
  }
}

// ==================== 主内容 ====================
.main-content {
  flex: 1;
  margin-top: 60px;
  min-height: calc(100vh - 60px - 72px);
}

// ==================== 页脚 ====================
.footer {
  border-top: 1px solid var(--color-border-light);
  background: #fff;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);

  .footer-logo-text {
    font-weight: 600;
    color: var(--color-text);
  }

  .footer-dot {
    color: var(--color-text-muted);
  }
}

.footer-copy {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
}

// ==================== 页面过渡动画 ====================
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>

<!-- 全局样式（下拉菜单弹出层，不受 scoped 限制） -->
<style lang="scss">
.user-popper {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--color-border-light) !important;
  box-shadow: var(--shadow-xl) !important;
  padding: 6px !important;
  margin-top: 6px !important;

  .popper-user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px 12px;

    .popper-name {
      font-size: 15px;
      font-weight: 600;
      color: var(--color-text);
    }

    .popper-role {
      font-size: 12px;
      color: var(--color-text-muted);
      margin-top: 2px;
    }
  }

  .el-dropdown-menu__item {
    border-radius: var(--radius-sm) !important;
    padding: 9px 14px !important;
    font-size: 14px;

    &.logout-item {
      color: var(--color-danger) !important;

      &:hover {
        background: var(--color-danger-bg) !important;
      }
    }
  }
}
</style>
