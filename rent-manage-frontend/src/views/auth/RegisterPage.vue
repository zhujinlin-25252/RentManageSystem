<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-blob bg-blob-1"></div>
      <div class="bg-blob bg-blob-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="register-container scale-in">
      <!-- 左侧品牌区 -->
      <div class="register-brand">
        <div class="brand-glow"></div>
        <span class="brand-emoji">✨</span>
        <h2>加入我们</h2>
        <p>开启品质租房新体验</p>
      </div>

      <!-- 注册表单 -->
      <div class="register-form-wrap">
        <h3>创建账号</h3>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          label-position="top"
          @submit.prevent="handleRegister"
        >
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="nickname" label="昵称">
                <el-input v-model="form.nickname" placeholder="您的昵称" :prefix-icon="User" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="realName" label="真实姓名">
                <el-input v-model="form.realName" placeholder="真实姓名（选填）" :prefix-icon="User" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item prop="email" label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱（选填）" :prefix-icon="Message" />
          </el-form-item>

          <el-form-item prop="gender" label="性别">
            <el-radio-group v-model="form.gender" size="large">
              <el-radio-button :value="0">未知</el-radio-button>
              <el-radio-button :value="1">男</el-radio-button>
              <el-radio-button :value="2">女</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="phone" label="手机号">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              maxlength="11"
              @blur="checkPhone"
              @input="checkPhone"
            />
            <transition name="el-fade-in">
              <span v-if="phoneStatus === 'checking'" class="phone-hint hint-checking">
                <el-icon class="is-loading"><Loading /></el-icon> 正在检查...
              </span>
              <span v-else-if="phoneStatus === 'available'" class="phone-hint hint-ok">
                <el-icon><CircleCheckFilled /></el-icon> 手机号可用
              </span>
              <span v-else-if="phoneStatus === 'exists'" class="phone-hint hint-bad">
                <el-icon><WarningFilled /></el-icon> 该手机号已注册，
                <router-link to="/login" class="link-login">去登录</router-link>
              </span>
            </transition>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="password" label="密码">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="8-20位，包含字母和数字"
                  :prefix-icon="Lock"
                  show-password
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="confirmPassword" label="确认密码">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="再次输入密码"
                  :prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleRegister"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item prop="role" label="注册身份">
            <el-radio-group v-model="form.role" size="large">
              <el-radio-button :value="0">我是租客 🧑‍💼</el-radio-button>
              <el-radio-button :value="1">我是房东 👨‍💼</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="link">
            <el-icon :size="14"><ArrowLeft /></el-icon>
            返回登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User, Iphone, Lock, Loading, CircleCheckFilled,
  WarningFilled, Message, ArrowLeft,
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const phoneStatus = ref('')
let checkTimer: ReturnType<typeof setTimeout> | null = null

const form = reactive({
  nickname: '',
  realName: '',
  email: '',
  gender: 0,
  phone: '',
  password: '',
  confirmPassword: '',
  role: 0,
})

async function checkPhone() {
  const phone = form.phone.trim()
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    phoneStatus.value = ''
    if (checkTimer) clearTimeout(checkTimer)
    return
  }
  if (checkTimer) clearTimeout(checkTimer)
  phoneStatus.value = 'checking'
  checkTimer = setTimeout(async () => {
    try {
      const exists = await request.get('/auth/check-phone', { params: { phone } })
      phoneStatus.value = exists ? 'exists' : 'available'
    } catch {
      phoneStatus.value = ''
    }
  }, 500)
}

const confirmPassValidator = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const phoneExistsValidator = (_rule: any, _value: string, callback: any) => {
  if (phoneStatus.value === 'exists') {
    callback(new Error('该手机号已注册'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称长度为2-50位', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
    { validator: phoneExistsValidator, trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: confirmPassValidator, trigger: 'blur' },
  ],
}

async function handleRegister() {
  if (!formRef.value) return
  if (phoneStatus.value === 'checking') {
    ElMessage.warning('正在检查手机号，请稍候再试')
    return
  }
  if (phoneStatus.value === 'exists') {
    ElMessage.warning('该手机号已注册，请更换手机号')
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.register({
        nickname: form.nickname,
        realName: form.realName || undefined,
        email: form.email || undefined,
        gender: form.gender,
        phone: form.phone,
        password: form.password,
        role: form.role,
      })
      ElMessage.success('注册成功！欢迎加入青年品质租房')
      router.push({ name: 'Login', query: { phone: form.phone } })
    } catch (err) {
      // handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
  background: #0f172a;
}

// 背景装饰
.bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;

  &.bg-blob-1 {
    width: 500px;
    height: 500px;
    top: -200px;
    left: -100px;
    background: #3b82f6;
    animation: float 12s ease-in-out infinite;
  }

  &.bg-blob-2 {
    width: 350px;
    height: 350px;
    bottom: -120px;
    right: -80px;
    background: #10b981;
    animation: float 10s ease-in-out infinite reverse;
  }
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-24px) rotate(3deg); }
}

// 注册卡片
.register-container {
  display: flex;
  width: 800px;
  max-width: 95vw;
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--radius-xl);
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

// 左侧品牌区
.register-brand {
  flex: 0 0 260px;
  background: linear-gradient(160deg, #3b82f6 0%, #2563eb 40%, #1d4ed8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
  overflow: hidden;

  .brand-glow {
    position: absolute;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.06);
    filter: blur(40px);
    top: -40px;
    right: -40px;
  }

  .brand-emoji {
    font-size: 48px;
    margin-bottom: 16px;
    position: relative;
    z-index: 1;
  }

  h2 {
    font-size: 24px;
    font-weight: 800;
    margin-bottom: 6px;
    position: relative;
    z-index: 1;
  }

  p {
    font-size: 13px;
    opacity: 0.85;
    position: relative;
    z-index: 1;
    text-align: center;
    padding: 0 20px;
  }
}

// 表单区
.register-form-wrap {
  flex: 1;
  padding: 36px 40px;
  overflow-y: auto;
  max-height: 80vh;

  h3 {
    font-size: 22px;
    font-weight: 800;
    margin: 0 0 20px;
    letter-spacing: -0.02em;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;

    .el-form-item__label {
      font-size: 13px;
      font-weight: 600;
      color: var(--color-text-secondary);
      padding-bottom: 4px;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 46px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 0.3em;
  border-radius: var(--radius-sm) !important;
  background: linear-gradient(135deg, #3b82f6, #60a5fa) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  margin-top: 4px;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4) !important;
  }
}

// 手机号状态提示
.phone-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 4px;

  .el-icon { font-size: 14px; }

  &.hint-checking { color: #909399; }
  &.hint-ok { color: #10b981; }
  &.hint-bad { color: #ef4444; }

  .link-login {
    color: var(--color-primary);
    font-weight: 600;
    text-decoration: underline;
  }
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);

  .link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: #3b82f6;
    font-weight: 600;
    margin-left: 4px;

    &:hover { text-decoration: underline; }
  }
}
</style>
