# AI Mobile Log Troubleshooting Tool – 启动指南

> 本指南适用于 **macOS / Linux / Windows**，假设已安装 JDK 17+、Node.js 18+、npm 9+。
>
> 前后端均可独立运行，也可整体启动进行联调。

---

## 1. 项目结构

```
AiTool_java01/
├─ backend/        # Spring Boot 3 + Maven
└─ frontend/       # React 18 + Vite + Ant Design 5
```

---

## 2. 后端 (Spring Boot)

### 2.1 编译

```bash
cd backend
# 使用项目自带的 Maven Wrapper，不要求本地安装 Maven
./mvnw clean package -DskipTests
```

生成的可执行包位于 `backend/target/ai-tool-backend-0.1.0-SNAPSHOT.jar`。

### 2.2 运行

```bash
# 运行在 8080 端口，可通过 --server.port 参数修改
java -jar target/ai-tool-backend-0.1.0-SNAPSHOT.jar
```

启动日志尾部看到

```
Tomcat started on port 8080 (http) with context path ''
```

即表示后端启动成功。

> ⚠️ 若端口被占用会报错 _"Port 8080 was already in use"_，请释放端口或修改 `--server.port`。

### 2.3 关键端点

| 功能         | 方法 & 路径                     |
| ------------ | ------------------------------- |
| 列出文件     | GET `/api/files`                |
| 上传文件     | POST `/api/uploads` (multipart) |
| 远程下载     | POST `/api/uploads/remote?url=` |
| 查询文件状态 | GET `/api/files/{uuid}`         |
| 触发解析     | POST `/api/files/{uuid}/parse`  |
| 分页查询日志 | GET `/api/files/{uuid}/logs`    |

---

## 3. 前端 (Vite + React)

### 3.1 安装依赖

```bash
cd ../frontend
npm install
```

### 3.2 开发模式

```bash
npm run dev -- --host   # 默认 5173 端口，可通过 --port 指定
```

启动成功后终端会输出：

```
Local:   http://localhost:5173/
Network: http://<your-ip>:5173/
```

浏览器访问即可。

### 3.3 生产构建 & 预览

```bash
npm run build      # 生成静态文件到 dist/
npm run preview    # 本地以 4173 端口预览
```

### 3.4 代理说明

前端 `vite.config.ts` 已配置：

```ts
proxy: {
  "/api": {
    target: "http://localhost:8080",
    changeOrigin: true,
  },
}
```

确保前端请求 `/api/**` 时会自动转发到后端。

---

## 4. 常见问题

| 现象                           | 解决方案                                                                            |
| ------------------------------ | ----------------------------------------------------------------------------------- |
| `Port 8080 was already in use` | 终止占用 8080 的进程，或 `java -jar ... --server.port=9090`                         |
| 前端白屏 / 网络 404            | 确认后端已启动，且前端请求的接口地址正确                                            |
| 上传后自动解析                 | 现已改为**手动**：上传成功 -> 文件状态 `STORED` -> 在文件列表里选中后点击"解析"按钮 |
| 日志级别筛选无结果             | 目前选项仅有 `Error/Warn/Info/Debug` 四种（兼容简写 E/W/I/D），请选择正确级别       |

---

## 5. 一键启动脚本（可选）

在项目根目录创建脚本 `run.sh`：

```bash
#!/usr/bin/env bash

# 后端
cd backend || exit 1
./mvnw clean package -DskipTests
java -jar target/ai-tool-backend-0.1.0-SNAPSHOT.jar &
BACK_PID=$!

# 前端
cd ../frontend || exit 1
npm install
npm run dev -- --host &
FRONT_PID=$!

echo "Backend PID: $BACK_PID  |  Frontend PID: $FRONT_PID"
wait
```

赋予执行权限： `chmod +x run.sh` ，直接 `./run.sh` 即可同时启动前后端。

---

祝使用愉快，如有问题请在 README 中开 issue 反馈。
