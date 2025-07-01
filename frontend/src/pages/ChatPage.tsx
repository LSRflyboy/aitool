import { useState } from "react";
import { Card, List, Input, Button, message as antMessage } from "antd";
import axios from "axios";

const { TextArea } = Input;

interface ChatMsg {
  role: "user" | "assistant";
  content: string;
}

export default function ChatPage() {
  const [messages, setMessages] = useState<ChatMsg[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const send = async () => {
    const text = input.trim();
    if (!text) return;
    const newMsgs: ChatMsg[] = [...messages, { role: "user", content: text }];
    setMessages(newMsgs);
    setInput("");
    setLoading(true);

    try {
      const res = await axios.post("/api/ai/chat", { messages: newMsgs });
      setMessages([
        ...newMsgs,
        { role: "assistant", content: res.data.content || "（暂无回复）" },
      ] as ChatMsg[]);
    } catch (e) {
      antMessage.error("AI 服务暂不可用，返回示例回复");
      setMessages([
        ...newMsgs,
        { role: "assistant", content: "抱歉，AI 服务暂未就绪。" },
      ] as ChatMsg[]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: "0 auto", padding: 16 }}>
      <Card title="AI 对话" bordered={false} bodyStyle={{ padding: 0 }}>
        <div style={{ maxHeight: "60vh", overflowY: "auto", padding: 16 }}>
          <List
            dataSource={messages}
            renderItem={(m) => (
              <List.Item
                style={{
                  display: "flex",
                  justifyContent: m.role === "user" ? "flex-end" : "flex-start",
                  border: "none",
                  padding: 4,
                }}
              >
                <div
                  style={{
                    background: m.role === "user" ? "#1677ff" : "#f5f5f5",
                    color: m.role === "user" ? "#fff" : "inherit",
                    padding: "8px 12px",
                    borderRadius: 8,
                    maxWidth: "80%",
                    whiteSpace: "pre-wrap",
                  }}
                >
                  {m.content}
                </div>
              </List.Item>
            )}
          />
        </div>
        <div
          style={{
            display: "flex",
            padding: 16,
            borderTop: "1px solid #f0f0f0",
          }}
        >
          <TextArea
            autoSize={{ minRows: 1, maxRows: 4 }}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onPressEnter={(e) => {
              if (!e.shiftKey) {
                e.preventDefault();
                send();
              }
            }}
            disabled={loading}
          />
          <Button
            type="primary"
            onClick={send}
            loading={loading}
            style={{ marginLeft: 8 }}
          >
            发送
          </Button>
        </div>
      </Card>
    </div>
  );
}
