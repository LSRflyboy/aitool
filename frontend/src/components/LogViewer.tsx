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
  Empty,
} from "antd";
import axios from "axios";
import { useEffect, useState } from "react";
import { Dayjs } from "dayjs";
import React from "react";

interface Props {
  uuids: string[];
  height?: number;
}
interface LogRow {
  timestamp: string;
  level: string;
  tag: string;
  message: string;
  rawLine?: string;
}

export default function LogViewer({ uuids, height = 600 }: Props) {
  const [data, setData] = useState<LogRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [level, setLevel] = useState<string>();
  const [tag, setTag] = useState<string>();
  const [range, setRange] = useState<[Dayjs, Dayjs] | null>(null);
  const PAGE_SIZE = 1000;
  const [pageMap, setPageMap] = useState<Record<string, number>>({});
  const [totalMap, setTotalMap] = useState<Record<string, number>>({});
  const [hasMore, setHasMore] = useState(false);

  const tableBodyRef = React.useRef<HTMLDivElement | null>(null);

  const fetchInitial = async () => {
    if (!uuids.length) return;
    setLoading(true);
    try {
      // 首先检查每个文件的状态，只有PARSED状态才获取日志
      const filesWithStatus = await Promise.all(
        uuids.map(async (id) => {
          try {
            const fileRes = await axios.get(`/api/files/${id}`);
            return {
              id,
              status: fileRes.data.status,
              filename: fileRes.data.filename,
            };
          } catch (e) {
            console.error(`获取文件${id}状态失败`, e);
            return { id, status: "UNKNOWN", filename: id };
          }
        })
      );

      // 过滤掉未解析的文件
      const parsedFiles = filesWithStatus.filter((f) => f.status === "PARSED");
      const unparsedFiles = filesWithStatus.filter(
        (f) => f.status !== "PARSED"
      );

      if (unparsedFiles.length > 0) {
        const unparsedNames = unparsedFiles.map((f) => f.filename).join(", ");
        message.info(`跳过未解析文件: ${unparsedNames}，请先点击「解析」按钮`);
      }

      if (parsedFiles.length === 0) {
        setData([]);
        setLoading(false);
        return;
      }

      // 只获取已解析文件的日志
      const parsedUuids = parsedFiles.map((f) => f.id);

      const baseParams: any = {};
      if (level) {
        // 直接使用选择的值，不进行转换
        baseParams.level = level;
      }
      if (tag) baseParams.tag = tag;
      if (range) {
        baseParams.from = range[0].toISOString();
        baseParams.to = range[1].toISOString();
      }

      const combined: LogRow[] = [];
      const newPageMap: Record<string, number> = {};
      const newTotalMap: Record<string, number> = {};

      // 拉第一页
      await Promise.all(
        parsedUuids.map(async (id) => {
          const res = await axios.get(`/api/files/${id}/logs`, {
            params: { ...baseParams, page: 0, size: PAGE_SIZE },
          });
          combined.push(...res.data.data);
          newPageMap[id] = 0;
          newTotalMap[id] = res.data.pages;
        })
      );

      setPageMap(newPageMap);
      setTotalMap(newTotalMap);
      setHasMore(Object.keys(newTotalMap).some((id) => newTotalMap[id] > 1));

      // 按时间排序
      combined.sort((a, b) => (a.timestamp > b.timestamp ? 1 : -1));
      setData(combined);

      if (combined.length) {
        message.success({
          content: `已加载 ${combined.length} 条日志`,
          duration: 2,
        });
      }
    } catch (e) {
      console.error("加载日志失败", e);
      message.error("加载日志失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInitial();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [uuids]); // 不把 level/tag/range 放进依赖，避免频繁触发卡顿

  // 加载更多
  const loadMore = async () => {
    if (!hasMore || loading) return;
    setLoading(true);
    try {
      const baseParams: any = {};
      if (level) baseParams.level = level;
      if (tag) baseParams.tag = tag;
      if (range) {
        baseParams.from = range[0].toISOString();
        baseParams.to = range[1].toISOString();
      }

      const more: LogRow[] = [];
      const newPageMap = { ...pageMap };
      const newHasMoreFlags: boolean[] = [];

      await Promise.all(
        Object.keys(pageMap).map(async (id) => {
          const nextPage = pageMap[id] + 1;
          if (nextPage >= totalMap[id]) return;
          const res = await axios.get(`/api/files/${id}/logs`, {
            params: { ...baseParams, page: nextPage, size: PAGE_SIZE },
          });
          more.push(...res.data.data);
          newPageMap[id] = nextPage;
          newHasMoreFlags.push(nextPage + 1 < totalMap[id]);
        })
      );

      if (more.length) {
        const merged = [...data, ...more].sort((a, b) =>
          a.timestamp > b.timestamp ? 1 : -1
        );
        setData(merged);
      }

      setPageMap(newPageMap);
      setHasMore(newHasMoreFlags.some(Boolean));
    } catch (e) {
      console.error("加载更多日志失败", e);
    } finally {
      setLoading(false);
    }
  };

  // 监听滚动
  useEffect(() => {
    const div = document.querySelector(".ant-table-body") as HTMLDivElement;
    if (!div) return;
    tableBodyRef.current = div;
    const handler = () => {
      if (!hasMore) return;
      const { scrollTop, scrollHeight, clientHeight } = div;
      if (scrollHeight - scrollTop - clientHeight < 300) {
        loadMore();
      }
    };
    div.addEventListener("scroll", handler);
    return () => div.removeEventListener("scroll", handler);
  }, [hasMore, pageMap, totalMap, level, tag, range]);

  // 统一日志级别选项，后端处理时自动匹配不同格式
  const levelOptions = [
    { value: "Error", label: "Error (E)" },
    { value: "Warn", label: "Warn (W)" },
    { value: "Info", label: "Info (I)" },
    { value: "Debug", label: "Debug (D)" },
  ];

  const columns = [
    { title: "时间", dataIndex: "timestamp", width: 200 },
    {
      title: "级别",
      dataIndex: "level",
      width: 100,
      render: (level: string) => (
        <Tag color={level === "E" || level === "Error" ? "red" : "blue"}>
          {level}
        </Tag>
      ),
    },
    { title: "Tag", dataIndex: "tag", width: 150 },
    {
      title: "内容",
      dataIndex: "rawLine",
      render: (_: any, row: LogRow) => (
        <div
          style={{
            whiteSpace: "pre-wrap",
            wordBreak: "break-word",
          }}
        >
          {row.rawLine || row.message}
        </div>
      ),
    },
  ];

  if (!uuids.length) return <Empty description="请在左侧选择文件" />;

  return (
    <div>
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          allowClear
          placeholder="级别"
          style={{ width: 150 }}
          value={level}
          onChange={setLevel}
          options={levelOptions}
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
        <Button type="primary" onClick={fetchInitial} disabled={!uuids.length}>
          查询
        </Button>
      </Space>
      <Spin spinning={loading} tip="加载中...">
        <Table
          virtual
          rowKey={(r: any) => r.timestamp + (r.rawLine || r.message)}
          dataSource={data}
          columns={columns}
          pagination={false}
          scroll={{ y: height }}
        />
      </Spin>
    </div>
  );
}
