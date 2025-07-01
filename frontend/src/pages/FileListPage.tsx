import { useEffect, useState } from "react";
import { Table, Layout, Space, Button, Popconfirm, message } from "antd";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";
import LogViewer from "../components/LogViewer";

interface FileRec {
  uuid: string;
  filename: string;
  status: string;
  createdAt: string;
}

const { Sider, Content } = Layout;

export default function FileListPage() {
  const [data, setData] = useState<FileRec[]>([]);
  const [loading, setLoading] = useState(false);
  const { uuid } = useParams();
  const navigate = useNavigate();
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);

  const selectedUuids = selectedKeys as string[];

  const handleDelete = async () => {
    if (!selectedUuids.length) return;
    try {
      await Promise.all(
        selectedUuids.map((id) => axios.delete(`/api/files/${id}`))
      );
      message.success("删除成功");
      setSelectedKeys([]);
      // refresh list
      const res = await axios.get<FileRec[]>("/api/files");
      setData(res.data);
    } catch {
      message.error("删除失败");
    }
  };

  const handleParse = async () => {
    if (!selectedUuids.length) return;
    try {
      console.log("开始触发解析，选中文件IDs:", selectedUuids);
      const results = await Promise.allSettled(
        selectedUuids.map((id) => {
          console.log(`请求解析文件 ${id}`);
          return axios
            .post(`/api/files/${id}/parse`)
            .then((res) => {
              console.log(`文件 ${id} 解析请求成功:`, res.data);
              return res;
            })
            .catch((err) => {
              console.error(
                `文件 ${id} 解析请求失败:`,
                err.response?.data || err.message
              );
              throw err;
            });
        })
      );
      console.log("所有解析请求结果:", results);
      const success = results.filter((r) => r.status === "fulfilled").length;
      const fails = results.filter((r) => r.status === "rejected").length;
      if (success) message.success(`已触发 ${success} 个文件解析`);
      if (fails) message.warning(`${fails} 个文件触发解析失败或已在解析中`);
      // refresh list to reflect status change (可能仍为 STORED 但后台开始解析)
      const res = await axios.get<FileRec[]>("/api/files");
      setData(res.data);
    } catch (e) {
      console.error("解析过程异常:", e);
      message.error("触发解析过程出现异常");
    }
  };

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const res = await axios.get<FileRec[]>("/api/files");
        setData(res.data);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  return (
    <Layout style={{ background: "#fff" }}>
      <Sider
        width={350}
        theme="light"
        style={{ paddingRight: 8, overflow: "auto" }}
      >
        <Space style={{ marginBottom: 8 }}>
          <Popconfirm
            title="确定删除所选文件?"
            onConfirm={handleDelete}
            disabled={!selectedUuids.length}
          >
            <Button danger disabled={!selectedUuids.length}>
              删除
            </Button>
          </Popconfirm>
          <Button
            type="primary"
            onClick={handleParse}
            disabled={!selectedUuids.length}
          >
            解析
          </Button>
        </Space>
        <Table
          rowKey="uuid"
          size="small"
          loading={loading}
          dataSource={data}
          pagination={{ pageSize: 10 }}
          rowSelection={{
            selectedRowKeys: selectedKeys,
            onChange: setSelectedKeys,
          }}
          onRow={(rec) => ({
            onClick: () => navigate(`/files/${rec.uuid}`),
          })}
          rowClassName={(rec) =>
            rec.uuid === uuid ? "ant-table-row-selected" : ""
          }
          columns={[
            { title: "文件名", dataIndex: "filename" },
            { title: "状态", dataIndex: "status", width: 80 },
            { title: "上传时间", dataIndex: "createdAt", width: 120 },
          ]}
        />
      </Sider>
      <Content style={{ paddingLeft: 16 }}>
        <LogViewer
          uuids={selectedUuids.length ? selectedUuids : uuid ? [uuid] : []}
        />
      </Content>
    </Layout>
  );
}
