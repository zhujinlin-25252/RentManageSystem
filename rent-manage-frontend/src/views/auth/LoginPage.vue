<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-blob bg-blob-1"></div>
      <div class="bg-blob bg-blob-2"></div>
      <div class="bg-blob bg-blob-3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-container scale-in">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-glow"></div>
        <div class="brand-content">
          <div class="brand-icon-wrap">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="14" fill="rgba(255,255,255,0.2)"/>
              <path d="M24 12L12 22v16h10v-10h4v10h10V22L24 12z" fill="white"/>
            </svg>
          </div>
          <h1 class="brand-title">青年品质租房</h1>
          <p class="brand-desc">为年轻人打造的高品质居住体验</p>
          <div class="brand-points">
            <div class="brand-point">
              <el-icon :size="16"><CircleCheckFilled /></el-icon>
              <span>真实房源，人工审核</span>
            </div>
            <div class="brand-point">
              <el-icon :size="16"><CircleCheckFilled /></el-icon>
              <span>品质保障，拎包入住</span>
            </div>
            <div class="brand-point">
              <el-icon :size="16"><CircleCheckFilled /></el-icon>
              <span>便捷管理，一站式服务</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="login-form-wrap">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">登录您的账号，开启品质生活</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              maxlength="11"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">
            立即注册
            <el-icon :size="14"><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="guest-entry">
          <el-divider>
            <span class="divider-label">或者</span>
          </el-divider>
          <router-link to="/home" class="guest-link">
            <el-icon :size="16"><View /></el-icon>
            以游客身份浏览房源
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Iphone, Lock, CircleCheckFilled, View, ArrowRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form.phone, form.password)
      ElMessage.success('登录成功！欢迎回来')
      const redirect = (route.query.redirect as string) || '/home'
      router.push(redirect)
    } catch (err: any) {
      // handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
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
    right: -100px;
    background: #f97316;
    animation: float 12s ease-in-out infinite;
  }

  &.bg-blob-2 {
    width: 350px;
    height: 350px;
    bottom: -120px;
    left: -80px;
    background: #3b82f6;
    animation: float 10s ease-in-out infinite reverse;
  }

  &.bg-blob-3 {
    width: 200px;
    height: 200px;
    top: 40%;
    left: 30%;
    background: #10b981;
    animation: float 8s ease-in-out infinite 2s;
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

// 登录卡片
.login-container {
  display: flex;
  width: 880px;
  max-width: 95vw;
  min-height: 520px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--radius-xl);
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

// 左侧品牌区
.login-brand {
  flex: 0 0 380px;
  background: linear-gradient(160deg, #f97316 0%, #ea580c 40%, #c2410c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;

  .brand-glow {
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.06);
    filter: blur(60px);
    top: -60px;
    right: -60px;
  }

  .brand-content {
    text-align: center;
    color: #fff;
    position: relative;
    z-index: 1;
  }

  .brand-icon-wrap {
    margin-bottom: 20px;
    display: inline-block;
  }

  .brand-title {
    font-size: 28px;
    font-weight: 800;
    margin-bottom: 8px;
    letter-spacing: 0.02em;
  }

  .brand-desc {
    font-size: 14px;
    opacity: 0.85;
    margin-bottom: 40px;
  }

  .brand-points {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .brand-point {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    font-weight: 500;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.12);
    border-radius: var(--radius-sm);
    backdrop-filter: blur(4px);

    .el-icon {
      flex-shrink: 0;
    }
  }
}

// 右侧表单区
.login-form-wrap {
  flex: 1;
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .form-title {
    font-size: 26px;
    font-weight: 800;
    color: var(--color-text);
    margin: 0 0 6px;
    letter-spacing: -0.02em;
  }

  .form-subtitle {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 32px;
  }
}

.login-form {
  .custom-input {
    :deep(.el-input__wrapper) {
      height: 48px;
      border-radius: var(--radius-sm) !important;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 0.3em;
  border-radius: var(--radius-sm) !important;
  background: linear-gradient(135deg, var(--color-primary), #fb923c) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(249, 115, 22, 0.3);
  margin-top: 4px;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4) !important;
  }
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--color-text-secondary);

  .link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: var(--color-primary);
    font-weight: 600;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.guest-entry {
  text-align: center;
  margin-top: 12px;

  .divider-label {
    color: var(--color-text-muted);
    font-size: 13px;
  }

  .guest-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    margin-top: 8px;
    color: var(--color-text-secondary);
    font-size: 14px;
    border: 1px dashed var(--color-border);
    border-radius: var(--radius-sm);
    transition: all var(--transition-fast);

    &:hover {
      color: var(--color-primary);
      border-color: var(--color-primary);
      background: var(--color-primary-bg);
    }
  }
}
</style>
