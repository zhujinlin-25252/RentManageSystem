<template>
  <div class="publish-page" v-loading="pageLoading" element-loading-text="正在加载房源信息...">
    <div class="page-container">
      <div class="page-header">
        <h1>
          <el-icon :size="22"><EditPen /></el-icon>
          {{ isEdit ? '编辑房源' : '发布新房源' }}
        </h1>
        <p>填写详细信息，让租客快速了解您的优质房源</p>
      </div>

      <el-form
        ref="formRef" :model="form" :rules="rules"
        label-width="100px" size="large" class="publish-form"
      >
        <!-- 基本信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-head">
              <el-icon :size="18"><HomeFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <el-row :gutter="24">
            <el-col :span="16">
              <el-form-item label="房源标题" prop="title">
                <el-input v-model="form.title" placeholder="如：精装修一室一厅，地铁口步行5分钟" maxlength="50" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="房源类型" prop="houseType">
                <el-radio-group v-model="form.houseType">
                  <el-radio-button :value="1">整租</el-radio-button>
                  <el-radio-button :value="2">合租</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="所在地区" prop="region">
                <el-cascader v-model="form.region" :options="regionOptions" placeholder="请选择省/市/区" style="width:100%" clearable />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="详细地址" prop="address">
                <el-input v-model="form.address" placeholder="具体街道/小区/楼号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="房屋描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细介绍房源特点、周边环境、交通情况等..." maxlength="500" show-word-limit />
          </el-form-item>
        </el-card>

        <!-- 户型与价格 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-head">
              <el-icon :size="18"><Coin /></el-icon>
              <span>户型与价格</span>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="室" prop="rooms">
                <el-input-number v-model="form.rooms" :min="0" :max="10" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="厅" prop="halls">
                <el-input-number v-model="form.halls" :min="0" :max="5" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="卫" prop="bathrooms">
                <el-input-number v-model="form.bathrooms" :min="0" :max="5" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="面积(㎡)" prop="area">
                <el-input-number v-model="form.area" :min="5" :max="999" :precision="1" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="月租金(元)" prop="rentMonthly">
                <el-input-number v-model="form.rentMonthly" :min="0" :max="99999" :step="100" controls-position="right" style="width:100%">
                  <template #prefix>￥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="押金(元)" prop="deposit">
                <el-input-number v-model="form.deposit" :min="0" :max="99999" :step="500" controls-position="right" style="width:100%">
                  <template #prefix>￥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="楼层" prop="floor">
                <el-input v-model="form.floor" placeholder="如：6层/18层" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="朝向" prop="orientation">
                <el-select v-model="form.orientation" placeholder="选择朝向" style="width:100%">
                  <el-option v-for="o in orientations" :key="o" :value="o" :label="o" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="支付方式" prop="paymentType">
                <el-select v-model="form.paymentType" placeholder="选择" style="width:100%">
                  <el-option :value="0" label="月付" />
                  <el-option :value="1" label="季付" />
                  <el-option :value="2" label="半年付" />
                  <el-option :value="3" label="年付" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 配套设施 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-head">
              <el-icon :size="18"><Box /></el-icon>
              <span>配套设施</span>
            </div>
          </template>
          <el-form-item label="">
            <div class="facilities-grid">
              <button
                v-for="item in facilityOptions" :key="item.value"
                type="button"
                class="facility-btn"
                :class="{ active: form.facilities.includes(item.value) }"
                @click="toggleFacility(item.value)"
              >
                <span class="facility-emoji">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
              </button>
            </div>
          </el-form-item>
        </el-card>

        <!-- 房源图片 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-head">
              <el-icon :size="18"><PictureFilled /></el-icon>
              <span>房源图片</span>
            </div>
          </template>

          <el-upload
            ref="uploadRef" :action="uploadAction" :headers="uploadHeaders"
            list-type="picture-card" :auto-upload="true" :limit="9"
            :on-success="handleUploadSuccess" :on-error="handleUploadError"
            :on-remove="handleImageRemove" :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            accept="image/jpeg,image/png,image/webp"
          >
            <div class="upload-placeholder">
              <el-icon :size="24"><Plus /></el-icon>
              <p>上传图片</p>
            </div>
            <template #tip>
              <div class="upload-tip">最多上传 9 张图片，第一张自动设为封面。支持 JPG/PNG/WebP，单张不超过 10MB</div>
            </template>
          </el-upload>
        </el-card>

        <!-- 提交 -->
        <div class="form-actions">
          <el-button size="large" @click="$router.back()">取消返回</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { EditPen, HomeFilled, Coin, Box, PictureFilled, Plus, Promotion } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile, UploadInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { pcaTextArr } from 'element-china-area-data'
import * as houseApi from '@/api/house'
import { getHouseImages } from '@/api/upload'
import request from '@/utils/request'

const regionOptions = pcaTextArr
const orientations = ['东', '南', '西', '北', '南北', '东南', '西南', '其他']

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const pageLoading = ref(false)

const isEdit = computed(() => !!route.query.id)

const uploadAction = '/api/upload/image'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }

const uploadedUrls = ref<string[]>([])
let originalSnapshot: Record<string, any> | null = null

const form = reactive({
  title: '', region: [] as string[], address: '', houseType: 1,
  rooms: 1, halls: 0, bathrooms: 1,
  area: undefined as number | undefined,
  rentMonthly: undefined as number | undefined,
  deposit: undefined as number | undefined,
  floor: '', orientation: '', paymentType: 0,
  description: '', facilities: [] as string[], coverImage: '',
})

const facilityOptions = [
  { value: 'wifi', icon: '📶', label: 'WiFi' },
  { value: 'ac', icon: '❄️', label: '空调' },
  { value: 'washer', icon: '🧺', label: '洗衣机' },
  { value: 'fridge', icon: '🧊', label: '冰箱' },
  { value: 'heater', icon: '🚿', label: '热水器' },
  { value: 'tv', icon: '📺', label: '电视' },
  { value: 'bed', icon: '🛏️', label: '床' },
  { value: 'wardrobe', icon: '🗄️', label: '衣柜' },
  { value: 'elevator', icon: '🛗', label: '电梯' },
  { value: 'parking', icon: '🅿️', label: '停车位' },
  { value: 'balcony', icon: '🌿', label: '阳台' },
  { value: 'kitchen', icon: '🍳', label: '独立厨房' },
]

function toggleFacility(value: string) {
  const idx = form.facilities.indexOf(value)
  if (idx >= 0) form.facilities.splice(idx, 1)
  else form.facilities.push(value)
}

const rules: FormRules = {
  title: [
    { required: true, message: '请输入房源标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度为5-50个字符', trigger: 'blur' },
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  rentMonthly: [{ required: true, message: '请输入月租金', trigger: 'blur' }],
}

async function fetchExistingHouse() {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const house = await houseApi.getMyHouseDetail(route.query.id as string)
    form.title = house.title || ''
    if (house.province || house.city || house.district) {
      form.region = [house.province || '', house.city || '', house.district || '']
    }
    form.address = house.address || ''
    form.houseType = house.houseType ?? 1
    form.rooms = house.rooms ?? 1
    form.halls = house.halls ?? 0
    form.bathrooms = house.bathrooms ?? 1
    form.area = house.area ? Number(house.area) : undefined
    form.rentMonthly = house.rentMonthly ? Number(house.rentMonthly) : undefined
    form.deposit = house.deposit ? Number(house.deposit) : undefined
    form.floor = house.floor || ''
    form.orientation = house.orientation || ''
    form.paymentType = house.paymentType ?? 0
    form.description = house.description || ''
    form.coverImage = house.coverImage || ''

    try {
      const parsed = typeof house.facilities === 'string' && house.facilities ? JSON.parse(house.facilities) : []
      form.facilities = Array.isArray(parsed) ? parsed : []
    } catch { form.facilities = [] }

    originalSnapshot = { ...form, facilities: [...form.facilities], coverImage: form.coverImage || '' }
    await loadExistingImages(route.query.id as string)
  } catch { ElMessage.error('加载房源信息失败，请返回重试') }
  finally { pageLoading.value = false }
}

async function loadExistingImages(houseId: string) {
  try {
    const res: any = await getHouseImages(houseId)
    const list: string[] = res?.list || []
    if (list.length > 0) {
      uploadedUrls.value = list
      if (!form.coverImage && list[0]) {
        form.coverImage = list[0]
        if (originalSnapshot) originalSnapshot.coverImage = list[0]
      }
    }
  } catch { /* silent */ }
}

function beforeUpload(rawFile: UploadFile['raw']) {
  if (!rawFile) return false
  const allowed = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowed.includes(rawFile.type)) { ElMessage.error('仅支持 JPG/PNG/WebP 格式'); return false }
  if (rawFile.size / 1024 / 1024 >= 10) { ElMessage.error('图片大小不能超过 10MB'); return false }
  return true
}

function handleUploadSuccess(res: any, file: UploadFile) {
  if (res.code === 200 && res.data?.url) {
    if (uploadedUrls.value.length === 0) form.coverImage = res.data.url
    uploadedUrls.value.push(res.data.url)
    ElMessage.success(`图片上传成功`)
  } else {
    ElMessage.error(res?.message || '上传失败')
    if (uploadRef.value && file.uid) uploadRef.value.handleRemove(file as any)
  }
}

function handleUploadError() { ElMessage.error('图片上传失败，请检查网络后重试') }

function handleImageRemove(file: UploadFile) {
  const urlToFind = file.response?.data?.url || file.url
  if (urlToFind) {
    const idx = uploadedUrls.value.indexOf(urlToFind)
    if (idx >= 0) uploadedUrls.value.splice(idx, 1)
  }
  if (form.coverImage === urlToFind) form.coverImage = uploadedUrls.value[0] || ''
}

function handleExceed() { ElMessage.warning('最多只能上传9张图片') }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const { region, ...rest } = form
      const payload: Record<string, any> = {
        ...rest, province: region[0] || '', city: region[1] || '', district: region[2] || '',
        facilities: JSON.stringify(form.facilities), coverImage: form.coverImage || '',
      }

      let hasChanged = false
      if (isEdit.value && originalSnapshot) {
        hasChanged = detectChanges(originalSnapshot, payload)
        if (hasChanged) payload.needReaudit = true
      }

      if (isEdit.value) {
        await houseApi.updateHouse(route.query.id as string, payload)
        ElMessage.success(hasChanged ? '已保存，需重新审核通过后生效' : '房源信息未发生变更')
        if (uploadedUrls.value.length > 0) await batchLinkImages(route.query.id as string)
      } else {
        const result = await houseApi.publishHouse(payload)
        const newHouseId = result?.houseId || result?.data?.houseId
        if (newHouseId && uploadedUrls.value.length > 0) await batchLinkImages(newHouseId)
        ElMessage.success('发布成功！等待审核中')
      }
      router.push('/landlord/houses')
    } catch { /* handled */ }
    finally { submitting.value = false }
  })
}

function detectChanges(original: Record<string, any>, current: Record<string, any>): boolean {
  const keys = ['title', 'region', 'address', 'houseType', 'rooms', 'halls', 'bathrooms', 'area', 'rentMonthly', 'deposit', 'floor', 'orientation', 'paymentType', 'description', 'facilities', 'coverImage']
  for (const key of keys) {
    if (String(original[key] ?? '') !== String(current[key] ?? '')) return true
  }
  return false
}

async function batchLinkImages(houseId: string) {
  for (let i = 0; i < uploadedUrls.value.length; i++) {
    try {
      await request.post('/upload/link-image', null, {
        params: { imageUrl: uploadedUrls.value[i], houseId, isCover: i === 0 ? 'true' : 'false' },
      })
    } catch { /* silent */ }
  }
}

onMounted(() => { fetchExistingHouse() })
</script>

<style lang="scss" scoped>
.publish-page {
  background: var(--color-bg);
  min-height: calc(100vh - 60px - 72px);
  padding-bottom: 60px;
}

.page-header {
  padding: 32px 0 8px;

  h1 {
    font-size: 24px;
    font-weight: 800;
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 0 6px;
    .el-icon { color: var(--color-primary); }
  }

  p {
    color: var(--color-text-secondary);
    font-size: 14px;
    margin: 0;
  }
}

.publish-form {
  max-width: 880px;
  padding-top: 20px;
}

.section-card {
  margin-bottom: 20px;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--color-border-light) !important;

  :deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid var(--color-border-light);
    background: var(--color-bg);
  }
}

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  .el-icon { color: var(--color-primary); }
}

.facilities-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.facility-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: 2px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-family: inherit;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);

  &:hover {
    border-color: var(--color-primary-light);
    background: var(--color-primary-bg);
  }

  &.active {
    border-color: var(--color-primary);
    background: var(--color-primary-bg);
    color: var(--color-primary);
    font-weight: 600;
    .facility-emoji { transform: scale(1.15); }
  }

  .facility-emoji { font-size: 18px; transition: transform 0.2s; }
}

.upload-placeholder {
  text-align: center;
  color: var(--color-text-muted);
  p { margin-top: 4px; font-size: 13px; }
}

.upload-tip { margin-top: 8px; color: var(--color-text-muted); font-size: 13px; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 0;

  .el-button {
    min-width: 120px;
    height: 46px !important;
    font-size: 15px !important;
    font-weight: 600 !important;
    border-radius: var(--radius-sm) !important;
  }

  .el-button--primary {
    background: linear-gradient(135deg, var(--color-primary), #fb923c) !important;
    border: none !important;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: var(--shadow-primary);
    }
  }
}
</style>
