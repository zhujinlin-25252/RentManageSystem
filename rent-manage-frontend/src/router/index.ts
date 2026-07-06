import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomePage.vue'),
        meta: { title: '首页', public: true },
      },
      {
        path: 'house',
        name: 'HouseList',
        component: () => import('@/views/house/HouseList.vue'),
        meta: { title: '房源列表', public: true },
      },
      {
        path: 'house/:id',
        name: 'HouseDetail',
        component: () => import('@/views/house/HouseDetail.vue'),
        meta: { title: '房源详情', public: true },
        props: true,
      },
      // ==================== 房东管理（需登录+房东角色） ====================
      {
        path: 'landlord/publish',
        name: 'HousePublish',
        component: () => import('@/views/landlord/HousePublish.vue'),
        meta: { title: '发布房源' },
      },
      {
        path: 'landlord/houses',
        name: 'LandlordHouses',
        component: () => import('@/views/landlord/LandlordPage.vue'),
        meta: { title: '我的房源' },
      },
      {
        path: 'landlord/orders',
        name: 'LandlordOrders',
        component: () => import('@/views/landlord/LandlordOrdersPage.vue'),
        meta: { title: '订单管理' },
      },
      // ==================== 管理员管理（需登录+管理员角色） ====================
      {
        path: 'admin/audit',
        name: 'AuditManage',
        component: () => import('@/views/admin/AuditPage.vue'),
        meta: { title: '房源审核' },
      },
      // ==================== 租客订单（需登录+租客角色） ====================
      {
        path: 'tenant/orders',
        name: 'MyOrders',
        component: () => import('@/views/tenant/OrderPage.vue'),
        meta: { title: '我的订单' },
      },
      // ==================== 个人中心（需登录） ====================
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/ProfilePage.vue'),
        meta: { title: '个人信息' },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterPage.vue'),
    meta: { title: '注册', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {

    //方案2：有保存位置就用，没有就回顶部
        if (savedPosition) {
            return savedPosition;
        } else {
            return { top: 0 };
        }
  },
})

// 路由守卫 - 需要登录的页面
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  const title = to.meta.title as string
  document.title = title ? `${title} - 青年品质租房` : '青年品质租房'

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
