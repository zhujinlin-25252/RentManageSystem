<template>
  <div class="profile-page">
    <div class="page-container">
      <h2 class="page-title">个人信息</h2>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- Tab 1: 个人信息 -->
        <el-tab-pane label="个人信息" name="info">
          <div class="profile-card" v-loading="profileLoading">
            <div class="profile-top">
              <el-avatar :size="72" :src="profile?.avatarUrl || undefined" class="p-avatar">
                {{ profile?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div>
                <h3 class="p-name">{{ profile?.nickname || '用户' }}</h3>
                <div class="p-badges">
                  <el-tag size="small" type="primary" round>{{ profile?.roleName || '-' }}</el-tag>
                  <el-tag v-if="profile?.isVerified === 1" size="small" type="success" round>已认证</el-tag>
                  <el-tag v-else size="small" type="info" round>未认证</el-tag>
                </div>
              </div>
            </div>

            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">真实姓名</span>
                <span class="info-value">{{ profile?.realName || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ maskPhone(profile?.phone) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ profile?.email || '未设置' }}</span>
              </div>

              <div class="info-item">
                <span class="info-label">昵称</span>
                <span v-if="!editMode" class="info-value">{{ profile?.nickname || '-' }}</span>
                <el-input v-else v-model="editForm.nickname" size="small" class="edit-input" />
              </div>
              <div class="info-item">
                <span class="info-label">性别</span>
                <span v-if="!editMode" class="info-value">{{ genderLabel(profile?.gender) }}</span>
                <el-radio-group v-else v-model="editForm.gender" size="small">
                  <el-radio-button :value="0">未知</el-radio-button>
                  <el-radio-button :value="1">男</el-radio-button>
                  <el-radio-button :value="2">女</el-radio-button>
                </el-radio-group>
              </div>
              <div class="info-item">
                <span class="info-label">头像URL</span>
                <span v-if="!editMode" class="info-value url-value">{{ profile?.avatarUrl || '未设置' }}</span>
                <el-input v-else v-model="editForm.avatarUrl" size="small" class="edit-input" placeholder="输入图片URL" />
              </div>
              <div class="info-item">
                <span class="info-label">支付密码</span>
                <span class="info-value">
                  <template v-if="hasPayPwd">已设置</template>
                  <template v-else>未设置</template>
                  <el-button link type="primary" size="small" style="margin-left:8px" @click="openPayPwdDialog">
                    {{ hasPayPwd ? '修改' : '设置' }}
                  </el-button>
                </span>
              </div>
            </div>

            <div class="info-actions">
              <template v-if="!editMode">
                <el-button type="primary" size="small" @click="startEdit">编辑</el-button>
              </template>
              <template v-else>
                <el-button type="primary" size="small" :loading="saving" @click="saveProfile">保存</el-button>
                <el-button size="small" @click="cancelEdit">取消</el-button>
              </template>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 我的收藏 -->
        <el-tab-pane label="我的收藏" name="favorites">
          <div v-if="favLoading" class="loading-grid">
            <div v-for="i in 6" :key="i" class="skeleton-card">
              <div class="skeleton-img"></div>
              <div class="skeleton-body">
                <div class="sk-line w-70 h-18"></div>
                <div class="sk-line w-50"></div>
                <div class="sk-line w-60"></div>
              </div>
            </div>
          </div>

          <div v-else-if="favorites.length > 0" class="fav-grid">
            <article
              v-for="item in favorites" :key="item.id"
              class="fav-card fade-in-up" @click="goDetail(item.houseId)"
            >
              <div class="card-img-wrap">
                <img :src="getCoverImage(item)" :alt="item.title" @error="handleImgError" />
                <span class="card-status" :class="'s-' + item.status">{{ getStatusText(item.status) }}</span>
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ item.title }}</h3>
                <p class="card-addr">
                  <el-icon :size="14"><Location /></el-icon>
                  {{ item.city }}{{ item.district }} · {{ item.address }}
                </p>
                <div class="card-tags">
                  <span class="ctag">{{ item.area }}m²</span>
                  <span class="ctag">{{ item.rooms }}室{{ item.halls }}厅</span>
                  <span class="ctag">{{ item.orientation || '-' }}</span>
                </div>
                <div class="card-footer">
                  <span class="card-price">
                    <em>¥</em>{{ item.rentMonthly?.toLocaleString() || '-' }}
                    <small>/月</small>
                  </span>
                  <span class="card-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <el-button
                  type="danger" plain size="small" class="unfav-btn"
                  :loading="removingIds.has(item.houseId)"
                  @click.stop="handleUnfavorite(item)"
                >取消收藏</el-button>
              </div>
            </article>
          </div>

          <el-empty v-else description="还没有收藏房源">
            <el-button type="primary" @click="$router.push('/house')">去浏览房源</el-button>
          </el-empty>

          <div v-if="favTotal > 0" class="pagination-wrap">
            <el-pagination
              v-model:current-page="favPage" v-model:page-size="favSize" :total="favTotal"
              :page-sizes="[6, 12, 18]" layout="total, sizes, prev, pager, next, jumper"
              background @current-change="fetchFavorites" @size-change="fetchFavorites"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 支付密码对话框 -->
    <el-dialog v-model="payPwdDialogVisible" :title="hasPayPwd ? '修改支付密码' : '设置支付密码'" width="380px" :close-on-click-modal="false">
      <el-form @submit.prevent="savePayPwd">
        <el-form-item v-if="hasPayPwd" label="原密码">
          <el-input v-model="payPwdForm.oldPwd" type="password" show-password maxlength="6" placeholder="请输入原支付密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="payPwdForm.newPwd" type="password" show-password maxlength="6" placeholder="请输入6位数字密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="payPwdForm.confirmPwd" type="password" show-password maxlength="6" placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPayPwd" @click="savePayPwd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'
import type { UserProfile } from '@/api/user'
import { getMyFavorites, removeFavorite } from '@/api/favorite'
import type { FavoriteItem } from '@/api/favorite'
import { hasPaymentPassword, setPaymentPassword } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')
const profile = ref<UserProfile | null>(null)
const profileLoading = ref(false)
const editMode = ref(false)
const saving = ref(false)
const hasPayPwd = ref(false)
const payPwdDialogVisible = ref(false)
const savingPayPwd = ref(false)
const payPwdForm = reactive({ oldPwd: '', newPwd: '', confirmPwd: '' })
const editForm = reactive({ nickname: '', gender: 0, avatarUrl: '' })

async function loadProfile() {
  profileLoading.value = true
  try {
    profile.value = await getUserInfo() as unknown as UserProfile
    try { hasPayPwd.value = await hasPaymentPassword() as unknown as boolean } catch { /* silent */ }
  } catch { /* handled */ }
  finally { profileLoading.value = false }
}

function startEdit() {
  if (!profile.value) return
  editForm.nickname = profile.value.nickname || ''
  editForm.gender = profile.value.gender ?? 0
  editForm.avatarUrl = profile.value.avatarUrl || ''
  editMode.value = true
}

function cancelEdit() { editMode.value = false }

async function saveProfile() {
  saving.value = true
  try {
    await updateUserInfo({
      nickname: editForm.nickname || undefined,
      gender: editForm.gender,
      avatarUrl: editForm.avatarUrl || undefined,
    })
    editMode.value = false
    ElMessage.success('保存成功')
    await loadProfile()
    await userStore.fetchUserInfo()
  } catch { /* handled */ }
  finally { saving.value = false }
}

function genderLabel(g?: number): string {
  if (g === 1) return '男'; if (g === 2) return '女'; return '未知'
}

function maskPhone(phone?: string): string {
  if (!phone || phone.length !== 11) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

function openPayPwdDialog() {
  payPwdForm.oldPwd = ''; payPwdForm.newPwd = ''; payPwdForm.confirmPwd = ''
  payPwdDialogVisible.value = true
}

async function savePayPwd() {
  if (!payPwdForm.newPwd || payPwdForm.newPwd.length !== 6 || !/^\d{6}$/.test(payPwdForm.newPwd)) { ElMessage.warning('新密码必须为6位数字'); return }
  if (payPwdForm.newPwd !== payPwdForm.confirmPwd) { ElMessage.warning('两次输入的密码不一致'); return }
  if (hasPayPwd.value && !payPwdForm.oldPwd) { ElMessage.warning('请输入原支付密码'); return }
  savingPayPwd.value = true
  try {
    await setPaymentPassword(hasPayPwd.value ? payPwdForm.oldPwd : '', payPwdForm.newPwd)
    hasPayPwd.value = true
    payPwdDialogVisible.value = false
    ElMessage.success(hasPayPwd.value ? '支付密码已修改' : '支付密码已设置')
  } catch { /* handled */ }
  finally { savingPayPwd.value = false }
}

// ==================== 收藏 ====================
const favorites = ref<FavoriteItem[]>([])
const favTotal = ref(0)
const favPage = ref(1)
const favSize = ref(12)
const favLoading = ref(false)
const removingIds = ref<Set<string>>(new Set())
const favLoaded = ref(false)

async function fetchFavorites() {
  favLoading.value = true
  try {
    const res = await getMyFavorites(favPage.value, favSize.value)
    favorites.value = res.records || []
    favTotal.value = res.total || 0
    favLoaded.value = true
  } catch { favorites.value = []; favTotal.value = 0 }
  finally { favLoading.value = false }
}

async function handleUnfavorite(item: FavoriteItem) {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${item.title}」？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消',
    })
  } catch { return }

  removingIds.value.add(item.houseId)
  try {
    await removeFavorite(item.houseId)
    ElMessage.success('已取消收藏')
    favorites.value = favorites.value.filter(f => f.id !== item.id)
    favTotal.value = Math.max(0, favTotal.value - 1)
    if (favorites.value.length === 0 && favPage.value > 1) { favPage.value--; fetchFavorites() }
  } catch { /* handled */ }
  finally { removingIds.value.delete(item.houseId) }
}

function goDetail(houseId: string) { router.push(`/house/${houseId}`) }

function getCoverImage(item: FavoriteItem): string {
  if (item.coverImage && item.coverImage.trim().length > 0 && !item.coverImage.startsWith('null')) return item.coverImage
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b', '#ef4444']
  const idx = (item.houseId?.charCodeAt(0) || 0) % colors.length
  return genPlaceholder(colors[idx], (item.title || '🏠').substring(0, 6), `${item.rooms || '-'}室${item.halls || '-'}厅 · ${item.area || '?'}m²`)
}

function handleImgError(e: Event) {
  const img = e.target as HTMLImageElement
  if (img.dataset.fallbacked) return
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b']
  img.src = genPlaceholder(colors[Math.floor(Math.random() * colors.length)], '🏠', '')
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
  if (days === 0) return '今天收藏'
  if (days === 1) return '昨天收藏'
  if (days < 7) return `${days}天前`
  return time.substring(0, 10)
}

watch(activeTab, (tab) => { if (tab === 'favorites' && !favLoaded.value) fetchFavorites() })
onMounted(() => { loadProfile() })
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: calc(100vh - 60px);
  background: var(--color-bg);
  padding: 32px 0;
}

.page-title {
  font-size: 24px; font-weight: 800; margin: 0 0 20px;
  letter-spacing: -0.02em;
}

.profile-tabs :deep(.el-tabs__header) { margin-bottom: 24px; }

// 个人信息卡片
.profile-card {
  background: #fff; border-radius: var(--radius-lg);
  padding: 32px; border: 1px solid var(--color-border-light);
}

.profile-top {
  display: flex; align-items: center; gap: 20px; margin-bottom: 28px;
  .p-avatar { font-weight: 600; }
  .p-name { font-size: 20px; font-weight: 700; margin: 0 0 6px; }
  .p-badges { display: flex; gap: 6px; }
}

.info-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px;
}

.info-item {
  display: flex; flex-direction: column; gap: 4px;

  .info-label { font-size: 12px; color: var(--color-text-muted); font-weight: 500; }
  .info-value { font-size: 15px; font-weight: 500; color: var(--color-text); }
  .url-value { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }
  .edit-input { max-width: 260px; }
}

.info-actions {
  margin-top: 28px; padding-top: 20px; border-top: 1px solid var(--color-border-light);
}

// 收藏
.loading-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px;
}

.skeleton-card {
  background: #fff; border-radius: var(--radius-lg); overflow: hidden;
  border: 1px solid var(--color-border-light);

  .skeleton-img {
    height: 200px;
    background: linear-gradient(90deg, var(--color-bg) 25%, var(--color-border-light) 50%, var(--color-bg) 75%);
    background-size: 200% 100%; animation: shimmer 1.5s infinite;
  }
  .skeleton-body { padding: 20px; }
  .sk-line {
    height: 14px; border-radius: 4px; margin-bottom: 10px;
    background: linear-gradient(90deg, var(--color-bg) 25%, var(--color-border-light) 50%, var(--color-bg) 75%);
    background-size: 200% 100%; animation: shimmer 1.5s infinite;

    &.w-70 { width: 70%; }
    &.w-50 { width: 50%; }
    &.w-60 { width: 60%; margin-bottom: 0; }
    &.h-18 { height: 18px; }
  }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.fav-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px;
}

.fav-card {
  background: #fff; border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; transition: all var(--transition-normal); opacity: 0;

  &:hover {
    transform: translateY(-4px); box-shadow: var(--shadow-lg); border-color: transparent;
    .card-img-wrap img { transform: scale(1.04); }
  }

  .card-img-wrap {
    position: relative; height: 200px; overflow: hidden;
    background: linear-gradient(135deg, #f5f5f4, #e7e5e4);

    img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }

    .card-status {
      position: absolute; top: 12px; left: 12px; padding: 3px 10px;
      border-radius: var(--radius-full); font-size: 12px; font-weight: 600; color: #fff;

      &.s-0 { background: #f59e0b; }
      &.s-1 { background: #10b981; }
      &.s-2 { background: #3b82f6; }
      &.s-3 { background: #a8a29e; }
      &.s-4 { background: #ef4444; }
    }
  }

  .card-content { padding: 18px 20px; }

  .card-title { font-size: 16px; font-weight: 700; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .card-addr { display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--color-text-secondary); margin: 0 0 12px; }

  .card-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px;
    .ctag { padding: 3px 10px; background: var(--color-bg); border-radius: var(--radius-xs); font-size: 12px; color: var(--color-text-secondary); font-weight: 500; }
  }

  .card-footer {
    display: flex; justify-content: space-between; align-items: center;
    padding-top: 14px; border-top: 1px solid var(--color-border-light); margin-bottom: 10px;
  }

  .card-price {
    font-size: 22px; font-weight: 800; color: var(--color-primary);
    em { font-style: normal; font-size: 15px; font-weight: 500; }
    small { font-size: 13px; font-weight: 400; color: var(--color-text-muted); }
  }

  .card-time { font-size: 12px; color: var(--color-text-muted); }
  .unfav-btn { width: 100%; }
}

.pagination-wrap { display: flex; justify-content: center; padding: 32px 0; }
</style>
