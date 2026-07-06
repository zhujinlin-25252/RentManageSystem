<template>
  <div class="detail-page">
    <!-- 加载中 -->
    <div v-if="pageState === 'loading'" class="page-container" style="padding-top: 40px;">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 已下架 -->
    <div v-else-if="pageState === 'off_shelf'" class="page-container not-found">
      <el-empty description="该房源已下架" :image-size="100">
        <template #image><span class="status-emoji">📦</span></template>
        <el-button type="primary" @click="$router.push('/house')">返回房源列表</el-button>
      </el-empty>
    </div>

    <!-- 不存在 -->
    <div v-else-if="pageState === 'not_found'" class="page-container not-found">
      <el-empty description="房源不存在或已被删除" :image-size="100">
        <template #image><span class="status-emoji">🔍</span></template>
        <el-button type="primary" @click="$router.push('/house')">返回房源列表</el-button>
      </el-empty>
    </div>

    <template v-else-if="pageState === 'active' && house">
      <!-- 面包屑 -->
      <nav class="breadcrumb page-container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/house' }">房源列表</el-breadcrumb-item>
          <el-breadcrumb-item>{{ house.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </nav>

      <div class="detail-layout page-container">
        <!-- 左侧主内容 -->
        <div class="detail-main">
          <!-- 图片画廊 -->
          <div class="gallery">
            <div class="gallery-main">
              <img
                v-if="currentImage"
                :src="currentImage"
                :alt="house.title"
                class="gallery-img"
                @error="handleMainImgError"
              />
              <div v-else class="gallery-placeholder">
                <span>🏠</span>
                <p>暂无房源图片</p>
              </div>
            </div>

            <div v-if="imageList.length > 1" class="gallery-thumbs">
              <button
                v-for="(img, idx) in imageList"
                :key="idx"
                class="thumb-btn"
                :class="{ active: currentIdx === idx }"
                @click="currentIdx = idx"
              >
                <img :src="img" alt="" @error="handleThumbError" />
              </button>
            </div>
          </div>

          <!-- 房源信息 -->
          <div class="info-card">
            <div class="info-head">
              <div>
                <h1 class="info-title">{{ house.title }}</h1>
                <p class="info-addr">
                  <el-icon :size="14"><Location /></el-icon>
                  {{ house.address }}
                </p>
              </div>
              <div class="info-price-tag">
                <span class="info-price">
                  <em>¥</em>{{ house.rentMonthly?.toLocaleString() || '-' }}
                  <small>/月</small>
                </span>
                <el-tag
                  :type="statusTagType(house.status)"
                  size="large"
                  round
                  effect="dark"
                >
                  {{ getStatusText(house.status) }}
                </el-tag>
              </div>
            </div>

            <div class="info-grid">
              <div class="info-cell">
                <span class="cell-label">面积</span>
                <span class="cell-value">{{ house.area }}m²</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">户型</span>
                <span class="cell-value">{{ house.rooms }}室{{ house.halls }}厅{{ house.bathrooms || 0 }}卫</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">朝向</span>
                <span class="cell-value">{{ house.orientation || '未知' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">楼层</span>
                <span class="cell-value">{{ house.floor || '-' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">城市</span>
                <span class="cell-value">{{ house.city }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">区域</span>
                <span class="cell-value">{{ house.district }}</span>
              </div>
            </div>

            <!-- 房源描述 -->
            <div v-if="house.description" class="desc-block">
              <h3>房源介绍</h3>
              <p>{{ house.description }}</p>
            </div>
          </div>
        </div>

        <!-- 右侧操作栏 -->
        <aside class="detail-sidebar">
          <!-- 管理员审核面板 -->
          <div v-if="isAdminView" class="sidebar-card admin-card">
            <div class="card-label">管理员审核</div>
            <p class="card-desc">{{ adminStatusDesc }}</p>

            <div v-if="house.status === 4 && house.auditRemark" class="rejection-box">
              <p class="rejection-head">拒绝原因</p>
              <p class="rejection-body">{{ house.auditRemark }}</p>
            </div>

            <template v-if="house.status === 0">
              <el-button
                type="primary" size="large" class="side-btn btn-approve"
                :loading="auditing" @click="handleAdminApprove"
              >
                审核通过
              </el-button>
              <el-button
                type="danger" size="large" class="side-btn btn-reject"
                :loading="auditing" @click="openAdminRejectDialog"
              >
                审核拒绝
              </el-button>
            </template>

            <el-button size="large" class="side-btn btn-outline" @click="$router.push('/admin/audit')">
              返回审核列表
            </el-button>
          </div>

          <!-- 房东管理面板 -->
          <div v-else-if="isLandlordView" class="sidebar-card landlord-card">
            <div class="card-label">房源管理</div>
            <p class="card-desc">{{ landlordStatusDesc }}</p>

            <div v-if="house.status === 4 && house.auditRemark" class="rejection-box">
              <p class="rejection-head">拒绝原因</p>
              <p class="rejection-body">{{ house.auditRemark }}</p>
            </div>

            <el-button
              type="primary" size="large" class="side-btn"
              @click="$router.push({ path: '/landlord/publish', query: { id: house.houseId } })"
            >
              {{ house.status === 4 ? '修改后重新提交' : '编辑房源' }}
            </el-button>

            <el-button
              v-if="house.status === 1 || house.status === 2"
              size="large" class="side-btn btn-outline" type="warning"
              :loading="togglingStatus" @click="handleOffShelf"
            >
              下架房源
            </el-button>

            <el-button
              v-if="house.status === 3"
              size="large" class="side-btn btn-outline" type="success"
              :loading="togglingStatus" @click="handleOnShelf"
            >
              重新上架
            </el-button>

            <el-button size="large" class="side-btn btn-outline" @click="$router.push('/landlord/houses')">
              返回房源管理
            </el-button>
          </div>

          <!-- 租客操作面板 -->
          <div v-else class="sidebar-card">
            <div class="card-label">{{ house.status === 2 ? '房源状态' : '立即租房' }}</div>
            <p class="card-desc">
              {{ house.status === 2 ? '该房源已被出租，您可以浏览其他房源' : '看中这套房？提交租房申请，房东确认后即可签约入住！' }}
            </p>

            <el-button
              v-if="house.status !== 2"
              type="primary" size="large" class="side-btn btn-rent"
              @click="handleRentClick"
            >
              立即租房
            </el-button>
            <el-button v-else size="large" class="side-btn btn-outline" disabled>
              该房源已被出租
            </el-button>
            <el-button size="large" class="side-btn btn-outline" @click="handleFavorite">
              <el-icon :class="{ 'is-fav': isFavorited }"><Star /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏房源' }}
            </el-button>
          </div>

          <!-- 房东信息卡片（租客视角） -->
          <div v-if="!isLandlordView" class="sidebar-card landlord-info">
            <div class="ll-avatar-row">
              <el-avatar :size="48" style="background: var(--color-primary)">房</el-avatar>
              <div>
                <strong>品质房东</strong>
                <small>信用良好 · 响应及时</small>
              </div>
            </div>
            <div class="ll-stats">
              <div class="ll-stat">
                <strong>98%</strong>
                <small>回复率</small>
              </div>
              <div class="ll-stat">
                <strong>&lt;2h</strong>
                <small>平均响应</small>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </template>

    <!-- ==================== 对话框 ==================== -->
    <!-- 租房下单 -->
    <el-dialog
      v-model="showRentDialog"
      title="提交租房申请"
      width="520px"
      :close-on-click-modal="false"
      class="rent-dialog"
    >
      <div class="rent-form">
        <div class="rent-summary">
          <p class="rent-title">{{ house?.title }}</p>
          <p class="rent-price-line">
            <span class="rent-price">¥{{ house?.rentMonthly?.toLocaleString() }}</span>/月
            · 押金 ¥{{ house?.deposit?.toLocaleString() }}
          </p>
        </div>

        <el-form :model="rentForm" label-position="top" :rules="rentRules" ref="rentFormRef">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="入住日期" prop="startDate">
                <el-date-picker
                  v-model="rentForm.startDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width:100%"
                  :disabled-date="(date: Date) => date < new Date(new Date().setHours(0,0,0,0))"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退房日期" prop="endDate">
                <el-date-picker
                  v-model="rentForm.endDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width:100%"
                  :disabled-date="disableEndDate"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="rentForm.contactName" placeholder="您的真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="rentForm.contactPhone" placeholder="手机号码" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="备注留言（选填）">
            <el-input
              v-model="rentForm.remark"
              type="textarea"
              :rows="3"
              placeholder="如：希望尽快入住、是否有家具需求等..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <div class="rent-fee" v-if="rentForm.startDate && rentForm.endDate">
          <div class="fee-row">
            <span>月租金</span>
            <span>¥{{ house?.rentMonthly?.toLocaleString() }}/月</span>
          </div>
          <div class="fee-row">
            <span>押金</span>
            <span>¥{{ house?.deposit?.toLocaleString() }}</span>
          </div>
          <div class="fee-divider"></div>
          <div class="fee-row fee-total">
            <span>预计总金额</span>
            <span class="fee-big">¥{{ estimatedTotal.toLocaleString() }}</span>
          </div>
          <p class="fee-note">* 实际金额以最终合同为准，此处为预估值</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="showRentDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitRent">
          提交租房申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 游客登录提示 -->
    <el-dialog
      v-model="showLoginPrompt"
      width="400px"
      :close-on-click-modal="false"
      :show-header="false"
      class="login-prompt-dialog"
    >
      <div class="prompt-body">
        <div class="prompt-icon">
          <el-icon :size="40" color="#f97316"><UserFilled /></el-icon>
        </div>
        <h3>请您先登录</h3>
        <p>登录后即可提交租房申请，与房东确认签约入住</p>
        <div class="prompt-actions">
          <el-button type="primary" size="large" class="prompt-btn" @click="goToLogin">登录账号</el-button>
          <el-button size="large" class="prompt-btn-outline" @click="goToRegister">注册新账号</el-button>
        </div>
        <p class="prompt-cancel">
          <el-button type="info" link @click="showLoginPrompt = false">继续浏览，暂不登录</el-button>
        </p>
      </div>
    </el-dialog>

    <!-- 管理员拒绝原因弹窗 -->
    <el-dialog
      v-model="showRejectDialog"
      title="审核拒绝"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <p class="reject-tip">请填写拒绝原因，帮助房东了解问题所在：</p>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="例如：房源图片模糊不清、地址信息不完整..."
        maxlength="200"
        show-word-limit
      />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" :loading="rejectLoading" @click="handleAdminReject">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Location, Key, Star, Check, UserFilled,
  EditPen, Top, Bottom, Select, CloseBold,
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getHouseDetail, getMyHouseDetail, approveHouse, rejectHouse,
  offShelfHouse, onShelfHouse,
} from '@/api/house'
import { getHouseImages } from '@/api/upload'
import { createOrder } from '@/api/order'
import { addFavorite, removeFavorite, checkFavorited } from '@/api/favorite'
import type { HouseInfo } from '@/api/house'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageState = ref<'loading' | 'active' | 'off_shelf' | 'not_found'>('loading')
const house = ref<HouseInfo | null>(null)
const currentIdx = ref(0)
const isLandlordView = ref(false)
const isAdminView = ref(false)
const togglingStatus = ref(false)
const auditing = ref(false)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)

const adminStatusDesc = computed(() => {
  if (!house.value) return ''
  const map: Record<number, string> = {
    0: '该房源正在等待审核，请仔细核查房源信息后决定通过或拒绝',
    1: '该房源已审核通过并公开发布',
    2: '该房源已被出租',
    3: '该房源已由房东下架',
    4: '该房源已被审核拒绝',
  }
  return map[house.value.status] || ''
})

const landlordStatusDesc = computed(() => {
  if (!house.value) return ''
  const map: Record<number, string> = {
    0: '该房源正在等待管理员审核，审核通过后将自动发布',
    1: '该房源已公开发布，租客可以浏览和申请租房',
    2: '该房源已被出租，租客正在租住中',
    3: '该房源已下架，租客端不再展示，可重新上架提交审核',
    4: '该房源未通过管理员审核，请根据拒绝原因修改后重新提交',
  }
  return map[house.value.status] || ''
})

async function handleAdminApprove() {
  if (!house.value) return
  try {
    await ElMessageBox.confirm(
      `确认通过「${house.value.title}」的审核？`,
      '审核通过',
      { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'success' }
    )
    auditing.value = true
    await approveHouse(house.value.houseId)
    ElMessage.success('房源已通过审核并发布')
    house.value.status = 1
  } catch (err: any) {
    if (err !== 'cancel') { /* handled */ }
  } finally { auditing.value = false }
}

function openAdminRejectDialog() {
  rejectReason.value = ''
  showRejectDialog.value = true
}

async function handleAdminReject() {
  if (!house.value || !rejectReason.value.trim()) return
  try {
    rejectLoading.value = true
    await rejectHouse(house.value.houseId, rejectReason.value.trim())
    ElMessage.success('已拒绝该房源并通知房东')
    showRejectDialog.value = false
    house.value.status = 4
    house.value.auditRemark = rejectReason.value.trim()
  } catch { /* handled */ }
  finally { rejectLoading.value = false }
}

async function handleOffShelf() {
  if (!house.value) return
  try {
    await ElMessageBox.confirm('下架后该房源将不再对租客展示', '确认下架', {
      confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning',
    })
    togglingStatus.value = true
    await offShelfHouse(house.value.houseId)
    ElMessage.success('房源已下架')
    house.value.status = 3
  } catch (err: any) {
    if (err !== 'cancel') { /* handled */ }
  } finally { togglingStatus.value = false }
}

async function handleOnShelf() {
  if (!house.value) return
  try {
    await ElMessageBox.confirm('上架后需等待管理员审核通过才能公开显示', '确认重新上架', {
      confirmButtonText: '提交审核', cancelButtonText: '取消', type: 'warning',
    })
    togglingStatus.value = true
    await onShelfHouse(house.value.houseId)
    ElMessage.success('已提交上架审核，请耐心等待审核结果')
    house.value.status = 0
  } catch (err: any) {
    if (err !== 'cancel') { /* handled */ }
  } finally { togglingStatus.value = false }
}

const showRentDialog = ref(false)
const showLoginPrompt = ref(false)
const submitting = ref(false)
const isFavorited = ref(false)
const rentFormRef = ref<FormInstance>()

const rentForm = ref({
  startDate: '', endDate: '', contactName: '', contactPhone: '', remark: '',
})

const rentRules: FormRules = {
  startDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

const estimatedTotal = computed(() => {
  if (!rentForm.value.startDate || !rentForm.value.endDate || !house.value) return 0
  const start = new Date(rentForm.value.startDate)
  const end = new Date(rentForm.value.endDate)
  const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())
  return (house.value.deposit || 0) + (house.value.rentMonthly || 0) * Math.max(1, months)
})

function disableEndDate(date: Date) {
  if (!rentForm.value.startDate) return date < new Date(new Date().setHours(0,0,0,0))
  return date < new Date(rentForm.value.startDate)
}

function handleRentClick() {
  if (userStore.isLoggedIn) {
    showRentDialog.value = true
  } else {
    showLoginPrompt.value = true
  }
}

function goToLogin() {
  router.push({ name: 'Login', query: { redirect: `/house/${route.params.id}` } })
}

function goToRegister() {
  router.push({ name: 'Register' })
}

async function handleSubmitRent() {
  if (!rentFormRef.value) return
  try { await rentFormRef.value.validate() } catch { return }
  if (!house.value?.houseId) return
  submitting.value = true
  try {
    await createOrder({
      houseId: house.value.houseId,
      startDate: rentForm.value.startDate,
      endDate: rentForm.value.endDate,
      contactName: rentForm.value.contactName,
      contactPhone: rentForm.value.contactPhone,
      remark: rentForm.value.remark,
    })
    ElMessage.success('租房申请提交成功！请等待房东确认')
    showRentDialog.value = false
    rentFormRef.value?.resetFields()
  } catch (err: any) {
    ElMessage.error(err?.message || '提交失败，请重试')
  } finally { submitting.value = false }
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) { showLoginPrompt.value = true; return }
  const houseId = house.value?.houseId
  if (!houseId) return
  try {
    if (isFavorited.value) {
      await removeFavorite(houseId)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(houseId)
      isFavorited.value = true
      ElMessage.success('已加入收藏')
    }
  } catch { /* handled */ }
}

const imageList = ref<string[]>([])

const currentImage = computed(() => {
  if (imageList.value.length > 0) {
    const img = imageList.value[currentIdx.value] || imageList.value[0]
    if (img && img.trim().length > 0 && !img.startsWith('null')) return img
  }
  const cover = house.value?.coverImage
  if (cover && cover.trim().length > 0 && !cover.startsWith('null')) return cover
  return ''
})

async function fetchDetail() {
  const id = route.params.id as string
  if (!id) { pageState.value = 'not_found'; return }
  pageState.value = 'loading'
  isLandlordView.value = false
  isAdminView.value = false

  try {
    house.value = await getHouseDetail(id, { _silent: true })
    pageState.value = 'active'
    if (userStore.isAdmin) {
      isAdminView.value = true
    } else if (userStore.isLoggedIn && house.value.landlordId === userStore.userInfo?.userId) {
      isLandlordView.value = true
    }
    await fetchHouseImages(id)
    if (userStore.isLoggedIn && !isLandlordView.value && !isAdminView.value) {
      try { isFavorited.value = (await checkFavorited(id)) as unknown as boolean } catch { /* silent */ }
    }
    return
  } catch (pubErr: any) {
    if (userStore.isLoggedIn) {
      try {
        house.value = await getMyHouseDetail(id, { _silent: true })
        pageState.value = 'active'
        if (userStore.isAdmin) { isAdminView.value = true } else { isLandlordView.value = true }
        await fetchHouseImages(id)
        return
      } catch { /* fall through */ }
    }
    house.value = null
    pageState.value = pubErr.code === 310 ? 'off_shelf' : 'not_found'
  }
}

async function fetchHouseImages(houseId: string) {
  try {
    const res = await getHouseImages(houseId)
    if (res?.list && res.list.length > 0) {
      imageList.value = res.list
      if (house.value?.coverImage && !res.list.includes(house.value.coverImage)) {
        imageList.value.unshift(house.value.coverImage)
      }
    } else if (house.value?.coverImage) {
      imageList.value = [house.value.coverImage]
    }
  } catch { /* silent */ }
}

function genDetailPlaceholder(): string {
  const colors = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#f59e0b']
  const idx = (house.value?.houseId?.charCodeAt(0) || 0) % colors.length
  return `data:image/svg+xml,${encodeURIComponent(`<svg xmlns='http://www.w3.org/2000/svg' width='800' height='500'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop offset='0%' stop-color='${colors[idx]}'/><stop offset='100%' stop-color='${colors[idx]}99'/></linearGradient></defs><rect fill='url(%23g)' width='800' height='500'/><text x='50%' y='48%' dominant-baseline='middle' text-anchor='middle' font-size='64' fill='white' opacity='0.9'>🏠</text><text x='50%' y='62%' dominant-baseline='middle' text-anchor='middle' font-size='28' fill='white' font-weight='bold'>${(house.value?.title || '🏠').substring(0, 8)}</text></svg>`)}`
}

function handleMainImgError(e: Event) {
  const img = e.target as HTMLImageElement
  if (img.dataset.fallbacked) return
  img.src = genDetailPlaceholder()
  img.dataset.fallbacked = '1'
}

function handleThumbError(e: Event) {
  const el = (e.target as HTMLElement).parentElement
  if (el) el.style.display = 'none'
}

function getStatusText(status: number): string {
  const map: Record<number, string> = { 0: '待审核', 1: '已发布', 2: '已出租', 3: '已下架', 4: '审核拒绝' }
  return map[status] || '未知'
}

function statusTagType(status: number): 'success' | 'warning' | 'info' | 'danger' | '' {
  const map: Record<number, 'success' | 'warning' | 'info' | 'danger' | ''> = {
    0: 'warning', 1: 'success', 2: 'info', 3: 'danger', 4: 'danger',
  }
  return map[status] || 'info'
}

onMounted(() => { fetchDetail() })
watch(() => route.params.id, () => { currentIdx.value = 0; imageList.value = []; fetchDetail() })
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: calc(100vh - 60px);
  background: var(--color-bg);
  padding-bottom: 60px;
}

.breadcrumb {
  padding: 20px 0 8px;
}

// ==================== 主布局 ====================
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
  align-items: start;
  padding-top: 12px;
}

// ==================== 左侧内容 ====================
.detail-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 图片画廊
.gallery {
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
}

.gallery-main {
  .gallery-img {
    width: 100%;
    height: 420px;
    object-fit: cover;
    display: block;
  }

  .gallery-placeholder {
    height: 420px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f5f4, #e7e5e4);
    color: var(--color-text-muted);

    span { font-size: 56px; margin-bottom: 8px; }
    p { font-size: 16px; }
  }
}

.gallery-thumbs {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
  border-top: 1px solid var(--color-border-light);

  .thumb-btn {
    width: 80px;
    height: 60px;
    border-radius: var(--radius-sm);
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    padding: 0;
    background: none;
    transition: border-color var(--transition-fast);
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &.active {
      border-color: var(--color-primary);
    }
  }
}

// 信息卡片
.info-card {
  background: #fff;
  padding: 28px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.info-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.info-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.info-addr {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.info-price-tag {
  text-align: right;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.info-price {
  font-size: 32px;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;

  em { font-style: normal; font-size: 20px; }

  small {
    font-size: 14px;
    font-weight: 400;
    color: var(--color-text-muted);
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.info-cell {
  .cell-label {
    display: block;
    font-size: 12px;
    color: var(--color-text-muted);
    margin-bottom: 2px;
  }

  .cell-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }
}

.desc-block {
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);

  h3 {
    font-size: 16px;
    font-weight: 700;
    margin: 0 0 10px;
  }

  p {
    font-size: 14px;
    line-height: 1.8;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

// ==================== 右侧边栏 ====================
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 84px;
}

.sidebar-card {
  background: #fff;
  padding: 24px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);

  .card-label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: var(--color-primary);
    margin-bottom: 8px;
  }

  .card-desc {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 20px;
    line-height: 1.6;
  }
}

.side-btn {
  width: 100%;
  margin-bottom: 10px;
  border-radius: var(--radius-sm) !important;
  font-weight: 600;
  height: 44px !important;

  &.btn-outline {
    border: 2px solid var(--color-border-light) !important;
    background: transparent !important;
    color: var(--color-text-secondary) !important;

    &:hover {
      border-color: var(--color-primary) !important;
      color: var(--color-primary) !important;
    }
  }

  &.btn-rent {
    background: linear-gradient(135deg, var(--color-primary), #fb923c) !important;
    border: none !important;
    box-shadow: 0 4px 16px rgba(249, 115, 22, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4) !important;
    }
  }

  &.btn-approve {
    background: linear-gradient(135deg, #10b981, #059669) !important;
    border: none !important;
    box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4) !important;
    }
  }

  &.btn-reject {
    background: #fff !important;
    border: 2px solid #ef4444 !important;
    color: #ef4444 !important;

    &:hover {
      background: #fef2f2 !important;
    }
  }
}

// 拒绝原因提示
.rejection-box {
  background: #fef2f2;
  border-radius: var(--radius-sm);
  border-left: 3px solid #ef4444;
  padding: 10px 14px;
  margin-bottom: 16px;

  .rejection-head {
    font-size: 12px;
    font-weight: 600;
    color: #dc2626;
    margin: 0 0 4px;
  }

  .rejection-body {
    font-size: 13px;
    color: #991b1b;
    line-height: 1.5;
    margin: 0;
  }
}

// 房东信息卡片
.landlord-info {
  .ll-avatar-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    strong { font-size: 15px; display: block; }
    small { font-size: 12px; color: var(--color-text-muted); }
  }

  .ll-stats {
    display: flex;
    gap: 24px;
    padding-top: 16px;
    border-top: 1px solid var(--color-border-light);

    .ll-stat {
      display: flex;
      flex-direction: column;
      strong { font-size: 18px; font-weight: 800; }
      small { font-size: 12px; color: var(--color-text-muted); }
    }
  }
}

// ==================== 租房对话框 ====================
.rent-summary {
  background: var(--color-primary-bg);
  border-radius: var(--radius-sm);
  padding: 16px;
  margin-bottom: 20px;
  border-left: 4px solid var(--color-primary);

  .rent-title {
    font-size: 15px;
    font-weight: 700;
    margin: 0 0 6px;
  }

  .rent-price-line {
    margin: 0;
    font-size: 14px;
    color: var(--color-text-secondary);
  }

  .rent-price {
    font-size: 22px;
    font-weight: 800;
    color: var(--color-primary);
  }
}

.rent-fee {
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  padding: 16px;
  margin-top: 16px;

  .fee-row {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    font-size: 14px;
    color: var(--color-text-secondary);

    &.fee-total {
      padding-top: 12px;
      border-top: 2px solid var(--color-border-light);
      margin-top: 4px;

      span:first-child {
        font-weight: 700;
        color: var(--color-text);
        font-size: 15px;
      }
    }
  }

  .fee-big {
    font-size: 20px !important;
    font-weight: 800 !important;
    color: var(--color-primary) !important;
  }

  .fee-divider { height: 1px; background: var(--color-border-light); margin: 4px 0; }

  .fee-note {
    font-size: 11px;
    color: var(--color-text-muted);
    margin: 10px 0 0;
  }
}

.is-fav { color: var(--color-primary) !important; }

.not-found {
  padding-top: 80px;
  text-align: center;
}

.status-emoji { font-size: 64px; }

// 游客登录提示
.login-prompt-dialog {
  :deep(.el-dialog__body) { padding: 36px 32px 24px; }
}

.prompt-body {
  text-align: center;

  .prompt-icon {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: var(--color-primary-bg);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  h3 { font-size: 20px; font-weight: 700; margin: 0 0 8px; }
  p { font-size: 14px; color: var(--color-text-secondary); margin: 0 0 24px; line-height: 1.5; }

  .prompt-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-width: 260px;
    margin: 0 auto;
  }

  .prompt-btn {
    width: 100%;
    height: 44px !important;
    font-weight: 600 !important;
    border-radius: var(--radius-sm) !important;
  }

  .prompt-btn-outline {
    width: 100%;
    height: 44px !important;
    font-weight: 600 !important;
    border-radius: var(--radius-sm) !important;
    border-color: var(--color-primary);
    color: var(--color-primary);

    &:hover { background: var(--color-primary-bg); }
  }

  .prompt-cancel { margin: 16px 0 0; }
}

.reject-tip {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 14px;
  line-height: 1.5;
}
</style>
