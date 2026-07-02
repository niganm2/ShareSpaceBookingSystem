<template>
  <div class="teacher-verification">
    <el-card>
      <div slot="header">
        <span>教师认证</span>
      </div>

      <div v-if="!statusLoaded" class="loading-container">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>

      <template v-else>
        <el-alert
          v-if="statusInfo.isVerified"
          title="您已通过认证"
          type="success"
          description="您的教师认证已通过审核，无需再次提交认证。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px"
        >
        </el-alert>

        <el-alert
          v-else-if="
            statusInfo.hasSubmitted && statusInfo.reviewStatus === 'PENDING'
          "
          title="认证申请审核中"
          type="warning"
          description="您已提交认证申请，请耐心等待管理员审核。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px"
        >
        </el-alert>

        <el-alert
          v-else-if="
            statusInfo.hasSubmitted && statusInfo.reviewStatus === 'REJECTED'
          "
          title="认证被拒绝"
          type="error"
          description="您的认证申请被拒绝，请核对信息后重新提交。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px"
        >
        </el-alert>

        <el-form
          ref="verificationForm"
          :model="verificationForm"
          :rules="rules"
          label-width="100px"
          v-if="
            !statusInfo.isVerified &&
            !(statusInfo.hasSubmitted && statusInfo.reviewStatus === 'PENDING')
          "
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="verificationForm.realName"
              placeholder="请输入真实姓名"
            ></el-input>
            <div class="form-tip" v-if="userInfo.realName">
              <i class="el-icon-info"></i>
              账户姓名：{{ userInfo.realName }}
            </div>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="verificationForm.phone"
              placeholder="请输入手机号"
            ></el-input>
            <div class="form-tip" v-if="userInfo.phone">
              <i class="el-icon-info"></i>
              账户手机：{{ userInfo.phone }}
            </div>
          </el-form-item>
          <el-form-item label="认证码" prop="verificationCode">
            <el-input
              v-model="verificationForm.verificationCode"
              placeholder="请输入六位认证码"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              <i class="el-icon-check"></i>
              提交认证
            </el-button>
            <el-button @click="handleReset"> 重置 </el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-card>
  </div>
</template>

<script>
import {
  submitVerification,
  getVerificationStatus,
} from "@/api/teacherVerification";

export default {
  name: "TeacherVerification",
  data() {
    const validateRealName = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入真实姓名"));
      } else if (this.userInfo.realName && value !== this.userInfo.realName) {
        callback(new Error("真实姓名与账户信息不匹配"));
      } else {
        callback();
      }
    };
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入手机号"));
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error("请输入正确的手机号"));
      } else if (this.userInfo.phone && value !== this.userInfo.phone) {
        callback(new Error("电话号码与账户信息不匹配"));
      } else {
        callback();
      }
    };
    return {
      verificationForm: {
        realName: "",
        phone: "",
        verificationCode: "",
      },
      rules: {
        realName: [
          { required: true, validator: validateRealName, trigger: "blur" },
        ],
        phone: [{ required: true, validator: validatePhone, trigger: "blur" }],
        verificationCode: [
          { required: true, message: "请输入认证码", trigger: "blur" },
          {
            pattern: /^\d{6}$/,
            message: "请输入六位数字认证码",
            trigger: "blur",
          },
        ],
      },
      loading: false,
      statusLoaded: false,
      statusInfo: {
        isVerified: false,
        hasSubmitted: false,
        reviewStatus: null,
      },
      userInfo: {
        realName: "",
        phone: "",
      },
    };
  },
  mounted() {
    this.loadVerificationStatus();
  },
  methods: {
    async loadVerificationStatus() {
      try {
        const res = await getVerificationStatus();
        if (res.code === 200 && res.data) {
          this.statusInfo = {
            isVerified: res.data.isVerified || false,
            hasSubmitted: res.data.hasSubmitted || false,
            reviewStatus: res.data.reviewStatus,
          };
          this.userInfo = {
            realName: res.data.realName || "",
            phone: res.data.phone || "",
          };
          this.verificationForm.realName = this.userInfo.realName;
          this.verificationForm.phone = this.userInfo.phone;
        }
      } catch (err) {
        // ignore
      } finally {
        this.statusLoaded = true;
      }
    },
    handleSubmit() {
      this.$refs.verificationForm.validate((valid) => {
        if (valid) {
          const user = JSON.parse(localStorage.getItem("user") || "{}");
          this.loading = true;
          submitVerification({
            userId: user.id,
            realName: this.verificationForm.realName,
            phone: this.verificationForm.phone,
            verificationCode: this.verificationForm.verificationCode,
          })
            .then(() => {
              this.$message.success("认证提交成功，请等待管理员审核");
              this.loadVerificationStatus();
            })
            .catch((err) => {
              this.$message.error(
                "认证提交失败：" + (err.message || "网络错误"),
              );
            })
            .finally(() => {
              this.loading = false;
            });
        }
      });
    },
    handleReset() {
      this.$refs.verificationForm.resetFields();
      this.verificationForm.realName = this.userInfo.realName;
      this.verificationForm.phone = this.userInfo.phone;
    },
  },
};
</script>

<style scoped>
.teacher-verification {
  padding: 20px;
}
.loading-container {
  text-align: center;
  padding: 40px;
  color: #909399;
}
.loading-container i {
  margin-right: 8px;
  font-size: 18px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.form-tip i {
  margin-right: 4px;
}
</style>
