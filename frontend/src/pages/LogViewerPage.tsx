import {
  Table,
  Tag,
  message,
  Spin,
  Select,
  Input,
  DatePicker,
  Button,
  Space,
} from "antd";
import axios from "axios";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import dayjs, { Dayjs } from "dayjs";

interface LogRow {
  timestamp: string;
  level: string;
  tag: string;
  message: string;
}

export default function LogViewerPage() {
  const { uuid } = useParams();
  const [data, setData] = useState<LogRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [level, setLevel] = useState<string>();
  const [tag, setTag] = useState<string>();
  const [range, setRange] = useState<[Dayjs, Dayjs] | null>(null);

  const fetchData = async () => {
    if (!uuid) return;
    setLoading(true);
    try {
      const params: any = { page: 0, size: 200 };
      if (level) params.level = level;
      if (tag) params.tag = tag;
      if (range) {
        params.from = range[0].toISOString();
        params.to = range[1].toISOString();
      }
      const res = await axios.get(`/api/files/${uuid}/logs`, {
        params,
      });
      setData(res.data.data);
    } catch (e) {
      message.error("加载日志失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [uuid]);

  const levelOptions = ["Error", "Warn", "Info", "Debug", "E", "W", "I", "D"];

  const handleSearch = () => {
    fetchData();
  };

  const columns = [
    {
      title: "时间",
      dataIndex: "timestamp",
      key: "timestamp",
      width: 200,
    },
    {
      title: "级别",
      dataIndex: "level",
      key: "level",
      width: 100,
      render: (level: string) => (
        <Tag color={level === "E" || level === "Error" ? "red" : "blue"}>
          {level}
        </Tag>
      ),
    },
    { title: "Tag", dataIndex: "tag", key: "tag", width: 150 },
    { title: "内容", dataIndex: "message", key: "message" },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          allowClear
          placeholder="级别"
          style={{ width: 120 }}
          value={level}
          onChange={setLevel}
          options={levelOptions.map((lv) => ({ value: lv, label: lv }))}
        />
        <Input
          placeholder="Tag"
          value={tag}
          onChange={(e) => setTag(e.target.value)}
          style={{ width: 150 }}
        />
        <DatePicker.RangePicker
          showTime
          value={range as any}
          onChange={(vals) => setRange(vals as any)}
        />
        <Button type="primary" onClick={handleSearch}>
          查询
        </Button>
      </Space>
      <Spin spinning={loading} tip="加载中...">
        <Table
          rowKey={(r) => r.timestamp + r.message}
          dataSource={data}
          columns={columns}
          pagination={false}
          scroll={{ y: 600 }}
        />
      </Spin>
    </div>
  );
}
