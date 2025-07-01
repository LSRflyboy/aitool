import { Table, Button, Space, Tag, message } from "antd";
import axios from "axios";
import { useEffect, useState } from "react";

interface TaskRow {
  uuid: string;
  filename: string;
  status: string;
  createdAt: string;
}

export default function TaskList() {
  const [data, setData] = useState<TaskRow[]>([]);

  const load = async () => {
    try {
      const res = await axios.get("/api/files");
      setData(res.data);
    } catch (e) {
      message.error("无法加载任务列表");
    }
  };

  useEffect(() => {
    load();
  }, []);

  const columns = [
    { title: "ID", dataIndex: "uuid", key: "uuid" },
    { title: "文件名", dataIndex: "filename", key: "filename" },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (v: string) => (
        <Tag color={v === "PARSED" ? "green" : v === "FAILED" ? "red" : "blue"}>
          {v}
        </Tag>
      ),
    },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt" },
  ];

  return (
    <Space direction="vertical" style={{ width: "100%" }}>
      <Button onClick={load}>刷新</Button>
      <Table
        rowKey="uuid"
        columns={columns}
        dataSource={data}
        pagination={{ pageSize: 10 }}
      />
    </Space>
  );
}
