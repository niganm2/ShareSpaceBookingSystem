<template>
  <div class="login-container">
    <div class="login-box">
      <h2>高校共享空间预约系统</h2>
      <el-form
        :model="loginForm"
        :rules="rules"
        ref="loginForm"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            v-model="loginForm.role"
            placeholder="请选择登录身份"
            style="width: 100%"
          >
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            style="width: 100%"
            >登录</el-button
          >
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="showRegister = true"
            >还没有账号？立即注册</el-button
          >
        </el-form-item>
      </el-form>
    </div>

    <el-dialog
      title="用户注册"
      :visible.sync="showRegister"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="handleCloseRegisterDialog"
    >
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerForm"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="学号/工号" prop="studentId">
          <el-input v-model="registerForm.studentId"></el-input>
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <el-select
            v-model="registerForm.role"
            placeholder="请选择注册身份"
            style="width: 100%"
          >
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showRegister = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleRegister"
          :loading="registerLoading"
          >注册</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
import { login, register } from "@/api/auth";

export default {
  name: "Login",
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        role: "STUDENT",
      },
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        realName: "",
        studentId: "",
        phone: "",
        email: "",
        role: "STUDENT",
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        role: [
          { required: true, message: "请选择登录身份", trigger: "change" },
        ],
      },
      registerRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { validator: this.validatePassword, trigger: "blur" },
        ],
        confirmPassword: [
          { required: true, message: "请输入确认密码", trigger: "blur" },
          { validator: this.validateConfirmPassword, trigger: "blur" },
        ],
        realName: [
          { required: true, message: "请输入真实姓名", trigger: "blur" },
        ],
        studentId: [
          {
            required: true,
            validator: this.validateStudentId,
            trigger: "blur",
          },
        ],
        phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
        email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],

        role: [
          { required: true, message: "请选择注册身份", trigger: "change" },
        ],
      },
      loading: false,
      registerLoading: false,
      showRegister: false,
    };
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          login(this.loginForm)
            .then((res) => {
              if (res.code !== 200) {
                this.$message.error("登录失败：" + (res.message || "未知错误"));
                this.loading = false;
                return;
              }

              const { token, user } = res.data;

              if (!token || !user) {
                this.$message.error("登录失败：用户数据不完整");
                this.loading = false;
                return;
              }

              localStorage.setItem("token", token);
              localStorage.setItem("user", JSON.stringify(user));

              const realRole = user.role;
              if (realRole === "ADMIN") {
                this.$router.push("/dashboard");
              } else if (realRole === "TEACHER") {
                this.$router.push("/teacher-spaces");
              } else if (realRole === "STUDENT") {
                this.$router.push("/dashboard");
              } else {
                this.$message.error("未知角色，无法登录");
                this.loading = false;
                return;
              }

              this.$store.dispatch("login", { token, user }).catch(() => {});

              this.$message.success("登录成功");
              this.loading = false;
            })
            .catch((err) => {
              let errMsg = "登录失败，请检查账号密码";
              if (err.response?.data?.message) {
                errMsg = err.response.data.message;
              } else if (err.message) {
                errMsg = err.message;
              }
              this.$message.error(errMsg);
              this.loading = false;
            });
        }
      });
    },
    validatePassword(rule, value, callback) {
      if (!value) {
        return callback(new Error("请输入密码"));
      }
      if (value.length < 6) {
        return callback(new Error("密码至少6位"));
      }
      callback();
    },
    validateConfirmPassword(rule, value, callback) {
      if (!value) {
        return callback(new Error("请输入确认密码"));
      }
      if (value !== this.registerForm.password) {
        return callback(new Error("两次输入的密码不一致"));
      }
      callback();
    },
    validateStudentId(rule, value, callback) {
      if (!value) {
        return callback(new Error("请输入学号/工号"));
      }
      callback();
    },
    handleRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          this.registerLoading = true;
          register(this.registerForm)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("注册成功，请登录");
                this.showRegister = false;
                this.$refs.registerForm.resetFields();
              } else {
                this.$message.error(
                  "注册失败：" + (res.message || "服务器错误"),
                );
              }
            })
            .catch((err) => {
              this.$message.error("注册失败：" + (err.message || "服务器错误"));
            })
            .finally(() => {
              this.registerLoading = false;
            });
        }
      });
    },
    handleCloseRegisterDialog() {},
  },
};
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #d3e8f9 0%, #fddee5 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-form {
  margin-top: 20px;
}

.login-form .el-form-item {
  margin-bottom: 22px !important;
}

::v-deep .el-dialog__body .el-form-item {
  margin-bottom: 24px !important;
}

>>> .el-card__body,
>>> .el-main {
  padding: 9px !important;
}
</style>
