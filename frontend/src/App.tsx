import { Layout, Menu } from "antd";
import {
  UploadOutlined,
  FileSearchOutlined,
  UnorderedListOutlined,
} from "@ant-design/icons";
import { Route, Routes, useNavigate } from "react-router-dom";
import UploadPage from "./pages/UploadPage";
import LogViewerPage from "./pages/LogViewerPage";
import FileListPage from "./pages/FileListPage";

const { Header, Content, Sider } = Layout;

export default function App() {
  const navigate = useNavigate();

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider collapsible>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={["upload"]}
          onClick={({ key }) => navigate(key)}
          items={[
            { key: "upload", icon: <UploadOutlined />, label: "上传解析" },
            { key: "logs", icon: <FileSearchOutlined />, label: "日志查看" },
            {
              key: "files",
              icon: <UnorderedListOutlined />,
              label: "文件视图",
            },
          ]}
        />
      </Sider>
      <Layout>
        <Header style={{ color: "#fff" }}>AI Troubleshooting Tool</Header>
        <Content style={{ margin: 16 }}>
          <Routes>
            <Route path="/" element={<UploadPage />} />
            <Route path="upload" element={<UploadPage />} />
            <Route path="logs/:uuid" element={<LogViewerPage />} />
            <Route path="files/:uuid?" element={<FileListPage />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}
