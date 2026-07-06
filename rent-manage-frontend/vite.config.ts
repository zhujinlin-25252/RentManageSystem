import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',       // 监听所有网卡，允许外网访问
    allowedHosts: true,  // 允许任何域名访问（Vite 6.0+ 推荐）\ 
    port: 5173,
    proxy: {
      // API 接口代理（后端所有接口都带 /api 前缀）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 上传资源代理（兼容旧数据中不带 /api 前缀的图片URL）
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 重写路径：加上 /api 前缀让后端能正确匹配 context-path
        rewrite: (path) => path.replace(/^\/uploads/, '/api/uploads'),
      },
    },
  },
})
