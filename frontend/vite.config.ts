import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The dev server runs on :5173 — that origin is already whitelisted in the
// backend's CORS config (app.cors.allowed-origins).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
})
