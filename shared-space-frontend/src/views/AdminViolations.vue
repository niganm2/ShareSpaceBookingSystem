<template>
  <div class="admin-violations">
    <el-card>
      <div slot="header">
        <span>违规记录管理</span>
      </div>
      <el-table :data="violationList" v-loading="loading" style="width: 100%">
        <el-table-column prop="type" label="违规类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getViolationType(scope.row.type)" size="small">
              {{ getViolationText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="违规原因"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column prop="spaceName" label="违规地点" width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.spaceName || "-" }}</span>
            <span
              v-if="scope.row.spaceLocation"
              style="color: #909399; font-size: 12px"
            >
              ({{ scope.row.spaceLocation }})
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="seatNumber" label="座位号" width="80">
          <template slot-scope="scope">
            {{ scope.row.seatNumber || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="80">
          <template slot-scope="scope">
            {{ maskName(scope.row.realName) }}
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="130">
          <template slot-scope="scope">
            {{ maskStudentId(scope.row.studentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话号码" width="130">
          <template slot-scope="scope">
            {{ maskPhone(scope.row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="扣分" width="70">
          <template slot-scope="scope">
            <span style="color: #f56c6c; font-weight: bold">
              -{{ scope.row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="违规时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!loading && violationList.length === 0"
        description="暂无违规记录"
      ></el-empty>
    </el-card>
  </div>
</template>

<script>
import { getAllViolationDetails } from "@/api/violation";

export default {
  name: "AdminViolations",
  data() {
    return {
      violationList: [],
      loading: false,
    };
  },
  mounted() {
    this.loadViolationList();
  },
  methods: {
    loadViolationList() {
      this.loading = true;
      getAllViolationDetails()
        .then((res) => {
          if (res.code === 200) {
            this.violationList = res.data || [];
          }
        })
        .catch(() => {
          this.$message.error("获取违规记录失败");
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getViolationType(type) {
      const map = {
        NO_SHOW: "danger",
        LATE: "warning",
        EARLY_LEAVE: "info",
        OTHER: "info",
      };
      return map[type] || "info";
    },
    getViolationText(type) {
      const map = {
        NO_SHOW: "未签到",
        LATE: "迟到",
        EARLY_LEAVE: "早退",
        OTHER: "其他",
      };
      return map[type] || type;
    },
    maskStudentId(studentId) {
      if (!studentId) return "-";
      if (studentId.length <= 6) return studentId;
      const start = studentId.substring(0, studentId.length - 6);
      const end = studentId.substring(studentId.length - 3);
      return start + "***" + end;
    },
    maskName(name) {
      if (!name) return "-";
      if (name.length === 1) return name;
      if (name.length === 2) return name.charAt(0) + "*";
      if (name.length === 3) return name.charAt(0) + "*" + name.charAt(2);
      return name.charAt(0) + "**" + name.charAt(name.length - 1);
    },
    maskPhone(phone) {
      if (!phone) return "-";
      if (phone.length !== 11) return phone;
      return phone.substring(0, 3) + "****" + phone.substring(7);
    },
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      return dateTime.replace("T", " ");
    },
  },
};
</script>

<style scoped>
.admin-violations {
  padding: 20px;
}
</style>
