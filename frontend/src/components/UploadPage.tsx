import { Upload, Button, Input, Space, message, Progress } from "antd";
import {
  UploadOutlined,
  CloudDownloadOutlined,
  BugOutlined,
} from "@ant-design/icons";
import type { UploadProps } from "antd";
import axios from "axios";
import { useState } from "react";

export default function UploadPage() {
  const [url, setUrl] = useState("");
  const [percent, setPercent] = useState(0);
  const [loading, setLoading] = useState(false);

  const props: UploadProps = {
    name: "file",
    multiple: false,
    accept: ".log,.zip,.gz,.txt",
    showUploadList: false,
    customRequest: async ({ file, onSuccess, onError }) => {
      const form = new FormData();
      form.append("file", file as any);
      setLoading(true);

      const fileName =
        typeof file === "string" ? file : (file as any).name || "unknown";
      const fileSize =
        typeof file === "string"
          ? "unknown"
          : Math.round((file as any).size / 1024);
      console.log("开始上传文件:", fileName, "大小:", fileSize, "KB");

      try {
        const res = await axios.post("/api/uploads", form, {
          headers: { "Content-Type": "multipart/form-data" },
          timeout: 300000, // 5分钟超时
          maxContentLength: 500 * 1024 * 1024, // 500MB
          maxBodyLength: 500 * 1024 * 1024,
          onUploadProgress: (ev) => {
            if (ev.total) {
              setPercent(Math.round((ev.loaded * 100) / ev.total));
            }
          },
        });
        message.success(
          "上传成功，请前往文件列表页点击「解析」按钮进行解析。ID: " +
            res.data.id
        );
        onSuccess?.(res.data, undefined as any);
        setTimeout(() => setPercent(0), 800);
        setLoading(false);
      } catch (e: any) {
        console.error("上传失败:", e);
        console.log("上传错误详情:", {
          status: e.response?.status,
          statusText: e.response?.statusText,
          data: e.response?.data,
          message: e.message,
          code: e.code,
        });
        const errorMsg =
          e.response?.data?.message ||
          (e.code === "ECONNABORTED"
            ? "上传超时，请检查网络或减小文件大小"
            : "上传失败: " + (e.message || "未知错误"));
        message.error(errorMsg);
        onError?.(e);
        setPercent(0);
        setLoading(false);
      }
    },
  };

  const handleRemote = async () => {
    if (!url) return;
    try {
      const res = await axios.post("/api/uploads/remote", null, {
        params: { url },
        timeout: 300000, // 5分钟超时
      });
      message.success(
        "远程下载成功，请前往文件列表页点击「解析」按钮进行解析。ID: " +
          res.data.id
      );
      setUrl("");
    } catch (e: any) {
      console.error("远程下载失败:", e);
      const errorMsg =
        e.response?.data?.message ||
        (e.code === "ECONNABORTED"
          ? "下载超时，请检查网络或使用更小的文件"
          : "下载失败: " + (e.message || "未知错误"));
      message.error(errorMsg);
    }
  };

  // 测试小文件上传
  const testUpload = async () => {
    try {
      setLoading(true);

      // 创建一个小的测试文件内容
      const testContent =
        "这是一个测试日志文件\nINFO: 测试日志内容\nERROR: 测试错误内容";
      const testFile = new File([testContent], "test.log", {
        type: "text/plain",
      });

      const form = new FormData();
      form.append("file", testFile);

      console.log(
        "开始上传测试文件:",
        testFile.name,
        "大小:",
        Math.round(testFile.size / 1024),
        "KB"
      );

      // 检查后端服务是否正常运行
      try {
        const healthCheck = await axios.get("/api/uploads/test", {
          timeout: 5000,
        });
        console.log("后端服务状态:", healthCheck.data);
      } catch (error: any) {
        console.error("后端服务检查失败:", error);
        const errorDetail = error.response
          ? `状态码: ${error.response.status}, 信息: ${JSON.stringify(
              error.response.data
            )}`
          : `错误类型: ${error.name}, 信息: ${error.message}`;
        console.log("详细错误信息:", errorDetail);
        message.error("后端服务不可用，请确认后端是否已启动");
        setLoading(false);
        return;
      }

      const res = await axios.post("/api/uploads", form, {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
        maxContentLength: 500 * 1024 * 1024,
        maxBodyLength: 500 * 1024 * 1024,
      });

      message.success("测试文件上传成功! ID: " + res.data.id);
      setLoading(false);
    } catch (e: any) {
      console.error("测试上传失败:", e);
      console.log("测试上传错误详情:", {
        status: e.response?.status,
        statusText: e.response?.statusText,
        data: e.response?.data,
        message: e.message,
        code: e.code,
      });

      const errorMsg =
        e.response?.data?.message ||
        (e.code === "ECONNABORTED"
          ? "测试上传超时"
          : "测试上传失败: " + (e.message || "未知错误"));
      message.error(errorMsg);
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      <Space>
        <Upload {...props}>
          <Button icon={<UploadOutlined />} loading={loading}>
            选择日志文件上传
          </Button>
        </Upload>
        <Button icon={<BugOutlined />} onClick={testUpload} loading={loading}>
          测试小文件上传
        </Button>
      </Space>
      {percent > 0 && (
        <Progress
          percent={percent}
          size="small"
          status={percent === 100 ? "success" : "active"}
          style={{ width: 200 }}
        />
      )}
      <Space>
        <Input
          placeholder="输入日志下载链接"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          style={{ width: 400 }}
        />
        <Button
          type="primary"
          icon={<CloudDownloadOutlined />}
          onClick={handleRemote}
        >
          远程下载
        </Button>
      </Space>
    </Space>
  );
}
