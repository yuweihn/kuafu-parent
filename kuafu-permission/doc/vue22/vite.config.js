import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue2'
import vueJsx from '@vitejs/plugin-vue2-jsx'
import path from 'path'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      vueJsx(),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), 'src/components/svg/icon')],
        symbolId: 'icon-[name]',
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
        '~': path.resolve(__dirname, 'node_modules')
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
          additionalData: `@use "${path.resolve(__dirname, 'src/components/css/vars.scss')}" as *;`
        }
      }
    },
    server: {
      host: '0.0.0.0',
      port: 3000,
      open: false,
      cors: true,
      proxy: {
        // 如果需要代理，可以在这里配置
        // '/api': {
        //   target: 'http://localhost:8080',
        //   changeOrigin: true,
        //   rewrite: (path) => path.replace(/^\/api/, '')
        // }
      }
    },
    build: {
      outDir: mode === 'prd' ? '../../src/main/resources' : 'dist',
      assetsDir: 'assets',
      sourcemap: mode !== 'prd',
      minify: 'terser',
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: ['vue', 'vue-router', 'vuex', 'element-ui'],
            echarts: ['echarts']
          }
        }
      }
    },
    base: './'
  }
})
