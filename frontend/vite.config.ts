import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        configure: (proxy, _options) => {
          proxy.on("error", (err, _req, _res) => {
            console.log("代理错误", err);
          });
          proxy.on("proxyReq", (proxyReq, req, _res) => {
            console.log("发送请求:", req.method, req.url);
          });
          proxy.on("proxyRes", (proxyRes, req, _res) => {
            console.log("收到响应:", proxyRes.statusCode, req.url);
          });
        },
        timeout: 300000,
      },
    },
  },
});
