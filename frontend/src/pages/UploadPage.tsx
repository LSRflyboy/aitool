import { Upload, Button, message, Input, Typography } from "antd";
import { UploadOutlined, LinkOutlined } from "@ant-design/icons";
import type { UploadProps } from "antd";
import axios from "axios";
import { useState } from "react";

export default function UploadPage() {
  const [url, setUrl] = useState("");
  const [desc, setDesc] = useState("");
  const [remoteLoading, setRemoteLoading] = useState(false);

  const props: UploadProps = {
    name: "file",
    action: "/api/uploads",
    maxCount: 1,
    onChange(info) {
      if (info.file.status === "done") {
        const id = info.file.response.id;
        message.success(`上传成功 (ID: ${id})`);
      } else if (info.file.status === "error") {
        message.error("上传失败");
      }
    },
  };

  const handleRemote = async () => {
    if (!url) return;
    setRemoteLoading(true);
    const key = "remote";
    message.loading({ content: "正在远程下载...", key });
    try {
      const res = await axios.post("/api/uploads/remote", null, {
        params: { url },
      });
      message.success({
        content: "远程下载完成 (ID: " + res.data.id + ")",
        key,
        duration: 2,
      });
    } catch (e) {
      message.error({ content: "远程下载失败", key });
    } finally {
      setRemoteLoading(false);
    }
  };

  const startAnalyze = async () => {
    if (!desc.trim()) {
      message.warning("请先输入问题描述");
      return;
    }
    message.loading({ content: "AI 分析中... (功能开发中)", key: "ai" });
    try {
      // TODO: 等后端接口完成后替换
      await new Promise((r) => setTimeout(r, 1000));
      message.success({ content: "AI 分析完成，敬请期待", key: "ai" });
    } catch (e) {
      message.error({ content: "AI 分析失败", key: "ai" });
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: "0 auto", marginTop: 32 }}>
      <Upload.Dragger {...props}>
        <p className="ant-upload-drag-icon">
          <UploadOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽上传日志压缩包</p>
      </Upload.Dragger>
      <div style={{ marginTop: 24, display: "flex", gap: 8 }}>
        <Input
          placeholder="日志文件下载链接"
          prefix={<LinkOutlined />}
          value={url}
          onChange={(e) => setUrl(e.target.value)}
        />
        <Button type="primary" onClick={handleRemote} loading={remoteLoading}>
          远程下载
        </Button>
      </div>

      <Typography.Paragraph type="danger" style={{ marginTop: 32 }}>
        <strong>！！输入问题描述，以启用 AI 智能分析功能！！</strong>
      </Typography.Paragraph>
      <Input.TextArea
        rows={4}
        placeholder="请简要描述您遇到的故障现象、期望结果或其它上下文信息..."
        value={desc}
        onChange={(e) => setDesc(e.target.value)}
      />
      <Button
        type="primary"
        block
        style={{ marginTop: 16 }}
        onClick={startAnalyze}
      >
        开始分析
      </Button>
    </div>
  );
}
