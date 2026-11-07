import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// The app is served by Spring Boot under /aurora and built into the WAR (see pom.xml).
// `npm run dev` serves it on :5173 and sends data requests to the demo app on :8081
// (mvnw spring-boot:test-run, see README).
export default defineConfig({
  base: '/aurora/',
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  build: {
    outDir: '../target/classes/static',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/aurora': {
        target: 'http://localhost:8081',
        // Page loads and Vite's own files (its client, the source modules) stay with Vite;
        // data, login and saves go to Spring.
        bypass: (req) =>
          req.headers.accept?.includes('text/html') || /^\/aurora\/(@|src\/|node_modules\/)/.test(req.url ?? '')
            ? req.url
            : undefined,
      },
    },
  },
})
