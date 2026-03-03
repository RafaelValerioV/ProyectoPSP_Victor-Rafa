import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import basicSsl from '@vitejs/plugin-basic-ssl'

export default defineConfig({
    plugins: [react(), tailwindcss(), basicSsl()],
    server: {
        host: '0.0.0.0',
        https: true,
        proxy: {
            '/api': {
                target: 'https://localhost:8443',
                changeOrigin: true,
                secure: false,
            },
        },
    },
})
