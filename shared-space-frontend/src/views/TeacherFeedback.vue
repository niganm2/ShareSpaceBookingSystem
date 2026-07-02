<template>
  <div class="teacher-feedback">
    <el-card>
      <div slot="header">
        <span>认证反馈</span>
      </div>
      <el-table :data="feedbackList" v-loading="loading" style="width: 100%">
        <el-table-column
          prop="realName"
          label="真实姓名"
          width="120"
        ></el-table-column>
        <el-table-column
          prop="phone"
          label="手机号"
          width="130"
        ></el-table-column>
        <el-table-column
          prop="verificationCode"
          label="认证码"
          width="120"
        ></el-table-column>
        <el-table-column prop="reviewStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.reviewStatus)">
              {{ getStatusText(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template slot-scope="scope">
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="审核备注" min-width="200">
          <template slot-scope="scope">
            <span
              v-if="scope.row.reviewStatus === 'REJECTED'"
              style="color: #f56c6c"
            >
              {{ scope.row.rejectReason || "无" }}
            </span>
            <span
              v-else-if="scope.row.reviewStatus === 'APPROVED'"
              style="color: #67c23a"
            >
              认证已通过
            </span>
            <span v-else style="color: #909399"> 等待审核中... </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getTeacherFeedbackList } from "@/api/teacherVerification";

export default {
  name: "TeacherFeedback",
  data() {
    return {
      feedbackList: [],
      loading: false,
    };
  },
  mounted() {
    this.loadFeedbackList();
  },
  methods: {
    loadFeedbackList() {
      this.loading = true;
      getTeacherFeedbackList()
        .then((res) => {
          this.feedbackList = res.data || [];
        })
        .catch((err) => {
          this.$message.error(
            "加载认证反馈失败：" + (err.message || "网络错误"),
          );
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getStatusType(status) {
      const typeMap = {
        PENDING: "warning",
        APPROVED: "success",
        REJECTED: "danger",
      };
      return typeMap[status] || "info";
    },
    getStatusText(status) {
      const textMap = {
        PENDING: "待审核",
        APPROVED: "已通过",
        REJECTED: "已拒绝",
      };
      return textMap[status] || status;
    },
  },
};
</script>

<style scoped>
.teacher-feedback {
  padding: 20px;
}
</style>
