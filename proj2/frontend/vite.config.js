import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Vite proxy forwards all headers by default, but we can explicitly configure it
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            // Log to verify headers are being forwarded
            const authHeader = req.headers.authorization || req.headers.Authorization;
            if (authHeader) {
              proxyReq.setHeader('Authorization', authHeader);
              console.log('[PROXY] Forwarding Authorization header to backend');
            } else {
              console.warn('[PROXY] No Authorization header in request');
            }
          });
        },
      },
    },
  },
})