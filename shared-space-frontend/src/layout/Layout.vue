<template>
  <div class="layout-container">
    <el-container>
      <!-- 左侧侧边栏 -->
      <el-aside width="200px">
        <div class="logo">
          <h3>共享空间预约系统</h3>
        </div>
        <el-menu :default-active="activeMenu" router background-color="#f5f7fa" text-color="#333"
          active-text-color="#fff">
          <el-menu-item index="/dashboard">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>

          <!-- 学生：预约相关 -->
          <el-menu-item index="/booking" v-if="userRole === 'STUDENT'">
            <i class="el-icon-date"></i>
            <span slot="title">座位预约</span>
          </el-menu-item>
          <el-menu-item index="/my-bookings" v-if="userRole === 'STUDENT'">
            <i class="el-icon-document"></i>
            <span slot="title">我的预约</span>
          </el-menu-item>
          <el-menu-item index="/violations" v-if="userRole === 'STUDENT'">
            <i class="el-icon-warning"></i>
            <span slot="title">违规记录</span>
          </el-menu-item>

          <!-- 老师：认证相关 -->
          <el-menu-item index="/teacher-verification" v-if="userRole === 'TEACHER'">
            <i class="el-icon-edit"></i>
            <span slot="title">教师资格认证</span>
          </el-menu-item>
          <el-menu-item index="/teacher-feedback" v-if="userRole === 'TEACHER'">
            <i class="el-icon-message"></i>
            <span slot="title">认证反馈</span>
          </el-menu-item>
          <el-menu-item index="/teacher-spaces" v-if="userRole === 'TEACHER' && isVerified === 1">
            <i class="el-icon-office-building"></i>
            <span slot="title">共享空间管理</span>
          </el-menu-item>

          <el-menu-item index="/profile" v-if="userRole !== 'ADMIN'">
            <i class="el-icon-user"></i>
            <span slot="title">个人中心</span>
          </el-menu-item>

          <!-- 管理员：审核相关 -->
          <el-menu-item index="/admin-review" v-if="userRole === 'ADMIN'">
            <i class="el-icon-s-check"></i>
            <span slot="title">教师认证审核</span>
          </el-menu-item>
          <el-menu-item index="/admin-bookings" v-if="userRole === 'ADMIN'">
            <i class="el-icon-tickets"></i>
            <span slot="title">预约记录管理</span>
          </el-menu-item>
          <el-menu-item index="/admin-space" v-if="userRole === 'ADMIN'">
            <i class="el-icon-office-building"></i>
            <span slot="title">空间管理</span>
          </el-menu-item>
          <el-menu-item index="/admin-violations" v-if="userRole === 'ADMIN'">
            <i class="el-icon-warning"></i>
            <span slot="title">违规记录管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧主内容区 -->
      <el-container>
        <el-header>
          <div class="header-content">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-button class="ai-assistant-btn" type="primary" icon="el-icon-robot" @click="aiDialogVisible = true">
                AI助手
              </el-button>
              <span>欢迎，{{
                userInfo?.realName || userInfo?.username || "用户"
                }}</span>
              <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link">
                  <i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                  <el-dropdown-item command="logout" style="color: #F56C6C;">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
        <!-- 修改密码对话框 -->
        <el-dialog title="修改密码" :visible.sync="dialogVisible" width="400px" append-to-body>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password
                placeholder="请输入旧密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password
                placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password
                placeholder="请再次输入新密码"></el-input>
            </el-form-item>
          </el-form>
          <span slot="footer" class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="handleUpdatePassword" :loading="loading">确 定</el-button>
          </span>
        </el-dialog>

        <el-dialog title="AI 智能预约助手" :visible.sync="aiDialogVisible" width="900px" height="700px" append-to-body
          class="ai-chat-dialog">
          <AiChat />
        </el-dialog>
      </el-container>
    </el-container>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
import AiChat from "@/views/AiChat.vue";
export default {
  name: "Layout",
  components: {
    AiChat
  },
  data() {
    return {
      userInfo: null,
      dialogVisible: false,
      aiDialogVisible: false,
      loading: false,
      passwordForm: {
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      },
      passwordRules: {
        oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
        newPassword: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
        ],
        confirmPassword: [
          { required: true, message: "请再次输入新密码", trigger: "blur" },
          {
            validator: (rule, value, callback) => {
              if (value !== this.passwordForm.newPassword) {
                callback(new Error("两次输入的密码不一致"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
      },
    };
  },
  computed: {
    activeMenu() {
      return this.$route.path;
    },
    userRole() {
      return this.userInfo?.role || "";
    },
    isVerified() {
      return this.userInfo?.isVerified || 0;
    },
  },
  created() {
    const user = localStorage.getItem("user");
    if (user) {
      this.userInfo = JSON.parse(user);
    }
  },
  methods: {
    handleCommand(command) {
      if (command === "logout") {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        this.$router.push("/login");
        location.reload();
      } else if (command === "changePassword") {
        this.dialogVisible = true;
        this.$nextTick(() => {
          this.$refs.passwordFormRef.resetFields();
        });
      }
    },
    handleUpdatePassword() {
      this.$refs.passwordFormRef.validate(async (valid) => {
        if (!valid) return;
        this.loading = true;
        try {
          const userId = this.userInfo.id;
          const res = await this.$http.post("/user/update-password", {
            userId,
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword,
          });
          if (res.data.code === 200) {
            this.$message.success("密码修改成功，请重新登录");
            this.dialogVisible = false;
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            this.$router.push("/login");
            location.reload();
          } else {
            this.$message.error(res.data.message || "密码修改失败");
          }
        } catch (err) {
          this.$message.error("密码修改失败");
        } finally {
          this.loading = false;
        }
      });
    },
  },
};
</script>

<style scoped>
.layout-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #f5f7fa;
  overflow-x: hidden;
  height: 100%;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #333;
  background-color: #cacfd8;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
}

.el-menu {
  background-color: #f5f7fa !important;
  border-right: none !important;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.el-menu-item {
  transition: all 0.3s ease !important;
}

.el-menu-item:hover {
  background-color: #f56c6c !important;
  color: #fff !important;
}

.el-menu-item.is-active {
  background-color: #f56c6c !important;
  color: #fff !important;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.el-main {
  padding: 0;
  overflow: hidden;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-dropdown-link {
  margin-left: 10px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 9px !important;
  height: calc(100% - 60px) !important;
  overflow: hidden !important;
}

::v-deep .el-card__body,
::v-deep .el-card__header {
  padding: 15px !important;
}

.ai-assistant-btn {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-color: #90caf9;
  color: #1976d2;
  margin-right: 15px;
  font-weight: 500;
}

.ai-assistant-btn:hover {
  background: linear-gradient(135deg, #bbdefb 0%, #90caf9 100%);
  border-color: #64b5f6;
  color: #1565c0;
}

.ai-chat-dialog {
  padding: 0;
}

.ai-chat-dialog .el-dialog__body {
  padding: 0;
  overflow: hidden;
}

.ai-chat-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
}

.ai-chat-dialog .el-dialog__title {
  color: white;
  font-size: 16px;
}

.ai-chat-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
}
</style>
