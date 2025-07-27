import react from '@vitejs/plugin-react'
import path from 'path'
import { defineConfig } from 'vite'

export default defineConfig({
  server: {
    hmr: true,
    proxy: {
      "/api": {
        // target: "https://10.10.18.71:2443",
        target: "http://localhost:8080",
        secure: false,
        changeOrigin: true
      },
      "/share": {
        // target: "https://10.10.18.71:2443",
        target: "http://localhost:8080",
        secure: false,
        changeOrigin: true
      },
      "/static-file": {
        target: "http://localhost:8080",  // 代理到Java服务
        secure: false,
        changeOrigin: true
      },
    },
  },
  plugins: [
    react(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
})
