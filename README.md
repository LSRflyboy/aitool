# AI Troubleshooting Tool – 快速启动指南

## 前提条件

- JDK 17
- Node.js ≥ 18
- 端口 8080（后端）、5173（前端）可用

## 启动步骤

### 1. 后端

```bash
cd backend
sh run.sh
```

### 2. 前端

```bash
cd frontend
npm install    # 首次
npm run dev
```

## 访问地址

- 上传解析页（默认）：http://localhost:5173/
- 文件列表页： http://localhost:5173/files
- 单个日志页： http://localhost:5173/logs/<uuid>
