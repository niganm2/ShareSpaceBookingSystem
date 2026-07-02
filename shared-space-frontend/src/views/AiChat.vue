<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="header-left">
        <div class="logo-icon">
          <i class="el-icon-robot"></i>
        </div>
        <span class="title">AI 智能预约助手</span>
      </div>
      <div class="header-right">
        <el-button size="mini" @click="clearHistory">清空记录</el-button>
        <span class="badge">DeepSeek</span>
      </div>
    </div>

    <div class="chat-messages" ref="messageContainer">
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
        <div class="avatar">
          <i v-if="msg.role === 'assistant'" class="el-icon-robot"></i>
          <i v-else class="el-icon-user"></i>
        </div>
        <div class="message-bubble">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>
      <div v-if="loading" class="message assistant">
        <div class="avatar">
          <i class="el-icon-robot"></i>
        </div>
        <div class="message-bubble">
          <div class="typing-indicator">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <div class="quick-actions">
        <template v-if="currentRole === 'STUDENT'">
          <el-button size="small" type="primary" plain @click="quickAction('query_my_bookings')">查询我的预约</el-button>
          <el-button size="small" type="primary" plain @click="quickAction('query_available_seats')">查询可用座位</el-button>
          <el-button size="small" type="primary" plain @click="quickAction('create_booking')">创建预约</el-button>
        </template>
        <template v-else-if="currentRole === 'TEACHER'">
          <el-button size="small" type="primary" plain @click="quickAction('query_space_bookings')">查询管辖自习室预约</el-button>
          <el-button size="small" type="primary" plain @click="quickAction('query_available_seats')">查询可用座位</el-button>
        </template>
        <template v-else-if="currentRole === 'ADMIN'">
          <el-button size="small" type="primary" plain @click="quickAction('query_my_bookings')">查询所有预约</el-button>
          <el-button size="small" type="primary" plain @click="quickAction('query_available_seats')">查询可用座位</el-button>
        </template>
      </div>
      <div class="input-wrapper">
        <el-input v-model="inputMessage" placeholder="请输入您的需求..." @keyup.enter.native="sendMessage" :disabled="loading"
          type="textarea" :rows="2" resize="none"></el-input>
        <el-button type="primary" @click="sendMessage" :loading="loading" class="send-btn">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AiChat",
  data() {
    return {
      messages: [],
      inputMessage: "",
      loading: false,
      currentRole: "STUDENT",
    };
  },
  mounted() {
    this.loadHistory();
    const userStr = localStorage.getItem("user");
    let welcomeMsg = "您好！我是智能预约助手，可以帮助您完成座位预约、取消预约、查询预约等操作。请告诉我您的需求。";
    if (userStr) {
      const user = JSON.parse(userStr);
      this.currentRole = user.role;
      if (user.role === "TEACHER") {
        welcomeMsg = "您好！我是智能预约助手，可以帮助您查看所管辖自习室的预约信息。请告诉我您的需求。";
      } else if (user.role === "ADMIN") {
        welcomeMsg = "您好！我是智能预约助手，为了系统安全，仅支持查询操作，不支持增删改操作。请告诉我您的需求。";
      }
    }
    if (this.messages.length === 0) {
      this.messages.push({
        role: "assistant",
        content: welcomeMsg,
      });
    } else {
      this.messages[0] = {
        role: "assistant",
        content: welcomeMsg,
      };
    }
    this.scrollToBottom();
  },
  watch: {
    messages: {
      handler() {
        this.saveHistory();
      },
      deep: true,
    },
  },
  methods: {
    getHistoryKey() {
      const userStr = localStorage.getItem("user");
      if (userStr) {
        const user = JSON.parse(userStr);
        return `aiChatHistory_${user.id}`;
      }
      return "aiChatHistory_default";
    },
    loadHistory() {
      const history = localStorage.getItem(this.getHistoryKey());
      if (history) {
        try {
          this.messages = JSON.parse(history);
        } catch (e) {
          console.error("加载聊天记录失败:", e);
        }
      }
    },
    saveHistory() {
      localStorage.setItem(this.getHistoryKey(), JSON.stringify(this.messages));
    },
    clearHistory() {
      this.$confirm("确定要清空所有聊天记录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.messages = [{
          role: "assistant",
          content: "您好！我是智能预约助手，可以帮助您完成座位预约、取消预约、查询预约等操作。请告诉我您的需求。",
        }];
        localStorage.removeItem(this.getHistoryKey());
      }).catch(() => { });
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || this.loading) return;

      const userMessage = this.inputMessage.trim();
      this.messages.push({ role: "user", content: userMessage });
      this.inputMessage = "";
      this.loading = true;

      this.scrollToBottom();

      try {
        const res = await this.$http.post("/ai/chat", {
          message: userMessage,
          history: this.messages.slice(-10).map((m) => ({
            role: m.role,
            content: m.content,
          })),
        }, {
          timeout: 60000
        });

        if (res.code === 200) {
          const reply = res.data.reply || "抱歉，我无法处理您的请求。";
          this.messages.push({ role: "assistant", content: reply });
        } else {
          this.messages.push({
            role: "assistant",
            content: res.message || "抱歉，服务出现问题，请稍后再试。",
          });
        }
      } catch (err) {
        console.error("[AI Chat] 请求失败:", err);
        this.messages.push({
          role: "assistant",
          content: "抱歉，网络连接出现问题，请稍后再试。",
        });
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },
    quickAction(action) {
      if (this.loading) return;
      if (action === "query_my_bookings") {
        this.inputMessage = "查询我的所有预约";
        this.sendMessage();
      } else if (action === "query_available_seats") {
        if (this.currentRole === "TEACHER") {
          this.inputMessage = "查询我所管辖自习室的可用座位";
        } else {
          this.inputMessage = "查询所有空间的可用座位";
        }
        this.sendMessage();
      } else if (action === "create_booking") {
        this.inputMessage = "帮我预约座位";
        this.sendMessage();
      } else if (action === "query_space_bookings") {
        this.inputMessage = "查询我所管辖自习室的所有预约信息";
        this.sendMessage();
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messageContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },
    formatMessage(content) {
      return content.replace(/\n/g, '<br/>');
    },
  },
};
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(90vh - 220px);
  background: #f5f7fa;
  position: relative;
}


.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge {
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 4px 10px;
  border-radius: 10px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  overflow-x: hidden;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.assistant .avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-bubble {
  background: #667eea;
  color: white;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  margin: 0 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-text {
  line-height: 1.6;
  word-break: break-word;
  font-size: 14px;
  color: #303133;
}

.message.user .message-text {
  color: white;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {

  0%,
  80%,
  100% {
    transform: scale(0);
  }

  40% {
    transform: scale(1);
  }
}

.chat-input-area {
  background: white;
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
  flex-shrink: 0;
}

.quick-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.input-wrapper .el-input {
  flex: 1;
}

.input-wrapper .el-input {
  font-family: "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB", sans-serif;
}

.input-wrapper .el-input textarea {
  resize: none;
  min-height: 48px;
  max-height: 120px;
  font-family: inherit;
}

.send-btn {
  flex-shrink: 0;
  width: 80px;
  height: 48px;
  border-radius: 8px;
}
</style>
