import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// During development the frontend runs on http://localhost:5173.
// The proxy below forwards any request starting with /api to the Spring Boot
// backend on :8080, so you can call fetch('/api/tasks') with no CORS hassle.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
