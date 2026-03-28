import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@shared': path.resolve(__dirname, '../campus-web-shared'),
      'dayjs': path.resolve(__dirname, 'node_modules/dayjs'),
      'axios': path.resolve(__dirname, 'node_modules/axios')
    },
    dedupe: ['vue', 'axios', 'pinia', 'vue-router', 'dayjs']
  },
  optimizeDeps: {
    include: ['axios', 'dayjs', 'element-plus', 'pinia', 'vue-router']
  },
  server: {
    port: 5175,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `
          @use "@shared/styles/variables" as *;
          @use "@shared/styles/mixins" as *;
        `
      }
    }
  }
})
