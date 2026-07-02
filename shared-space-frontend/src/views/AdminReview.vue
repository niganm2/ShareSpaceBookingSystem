<template>
  <div class="admin-review">
    <el-card>
      <div slot="header">
        <span>教师认证审核</span>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审核" name="pending"></el-tab-pane>
        <el-tab-pane label="已审核" name="reviewed"></el-tab-pane>
      </el-tabs>

      <el-table
        :data="pendingList"
        v-loading="loading"
        v-if="activeTab === 'pending'"
        style="width: 100%"
      >
        <el-table-column
          prop="realName"
          label="真实姓名"
          width="120"
        ></el-table-column>
        <el-table-column
          prop="phone"
          label="电话号码"
          width="130"
        ></el-table-column>
        <el-table-column
          prop="verificationCode"
          label="验证码"
          width="120"
        ></el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button
              type="success"
              size="small"
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table
        :data="reviewedList"
        v-loading="loading"
        v-if="activeTab === 'reviewed'"
        style="width: 100%"
      >
        <el-table-column
          prop="realName"
          label="真实姓名"
          width="120"
        ></el-table-column>
        <el-table-column
          prop="phone"
          label="电话号码"
          width="130"
        ></el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.reviewStatus)" size="medium">
              {{ getStatusText(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="200">
          <template slot-scope="scope">
            <span
              v-if="
                String(scope.row.reviewStatus) === 'REJECTED' ||
                String(scope.row.reviewStatus) === '2'
              "
              style="color: #f56c6c"
            >
              {{ scope.row.rejectReason || "-" }}
            </span>
            <span v-else style="color: #67c23a">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      title="拒绝认证"
      :visible.sync="rejectDialogVisible"
      width="500px"
    >
      <el-form
        :model="rejectForm"
        :rules="rejectRules"
        ref="rejectForm"
        label-width="100px"
      >
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="500"
            show-word-limit
          >
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting"
          >确认拒绝</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getPendingVerifications,
  reviewVerification,
} from "@/api/teacherVerification";
import moment from "moment";

export default {
  name: "AdminReview",
  data() {
    return {
      activeTab: "pending",
      pendingList: [],
      reviewedList: [],
      loading: false,
      rejectDialogVisible: false,
      rejectForm: {
        verificationId: null,
        rejectReason: "",
      },
      rejectRules: {
        rejectReason: [
          { required: true, message: "请输入拒绝原因", trigger: "blur" },
          {
            min: 5,
            max: 500,
            message: "拒绝原因长度在5-500个字符之间",
            trigger: "blur",
          },
        ],
      },
      rejecting: false,
    };
  },
  mounted() {
    this.loadPendingVerifications();
  },
  methods: {
    handleTabClick(tab) {
      this.activeTab = tab.name;
      if (tab.name === "pending") {
        this.loadPendingVerifications();
      } else {
        this.loadReviewedVerifications();
      }
    },
    loadPendingVerifications() {
      this.loading = true;
      getPendingVerifications(1, 100)
        .then((res) => {
          const allRecords = res.data || [];
          this.pendingList = allRecords.filter(
            (item) =>
              String(item.reviewStatus) === "PENDING" ||
              String(item.reviewStatus) === "0",
          );
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadReviewedVerifications() {
      this.loading = true;
      getPendingVerifications(1, 100)
        .then((res) => {
          const allRecords = res.data || [];
          this.reviewedList = allRecords.filter(
            (item) =>
              String(item.reviewStatus) !== "PENDING" &&
              String(item.reviewStatus) !== "0",
          );
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleApprove(row) {
      this.$confirm("确认通过该教师的认证申请？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          reviewVerification({
            verificationId: row.id,
            reviewStatus: "APPROVED",
          }).then(() => {
            this.$message.success("审核通过");
            this.loadPendingVerifications();
          });
        })
        .catch(() => {});
    },
    handleReject(row) {
      this.rejectForm.verificationId = row.id;
      this.rejectForm.rejectReason = "";
      this.rejectDialogVisible = true;
    },
    confirmReject() {
      this.$refs.rejectForm.validate((valid) => {
        if (valid) {
          this.rejecting = true;
          reviewVerification({
            verificationId: this.rejectForm.verificationId,
            reviewStatus: "REJECTED",
            rejectReason: this.rejectForm.rejectReason,
          })
            .then(() => {
              this.$message.success("已拒绝");
              this.rejectDialogVisible = false;
              this.loadPendingVerifications();
            })
            .finally(() => {
              this.rejecting = false;
            });
        }
      });
    },
    getStatusType(status) {
      const statusStr = String(status);
      const map = {
        PENDING: "warning",
        APPROVED: "success",
        REJECTED: "danger",
        0: "warning",
        1: "success",
        2: "danger",
      };
      return map[statusStr] || "info";
    },
    getStatusText(status) {
      const statusStr = String(status);
      const map = {
        PENDING: "待审核",
        APPROVED: "已通过",
        REJECTED: "已拒绝",
        0: "待审核",
        1: "已通过",
        2: "已拒绝",
      };
      return map[statusStr] || "已审核";
    },
    formatDate(date) {
      return moment(date).format("YYYY-MM-DD HH:mm:ss");
    },
  },
};
</script>

<style scoped>
.admin-review {
  padding: 20px;
}
</style>
