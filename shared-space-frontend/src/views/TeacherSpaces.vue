<template>
  <div class="teacher-spaces">
    <el-card>
      <div slot="header">
        <span>{{ spaceName }} - 预约管理</span>
      </div>

      <el-row :gutter="10">
        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <i class="el-icon-document"></i>
              <span>今日预约记录</span>
            </div>
            <el-statistic title="今日预约数量" :value="todayBookingCount">
              <template slot="suffix">
                <span>个</span>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <i class="el-icon-s-grid"></i>
              <span>剩余座位</span>
            </div>
            <el-statistic title="可用座位" :value="availableSeats">
              <template slot="suffix">
                <span>个</span>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" style="margin-top: 20px">
        <div slot="header">
          <i class="el-icon-tickets"></i>
          <span>预约详细信息</span>
        </div>
        <el-table
          :data="currentPageData"
          v-loading="loading"
          style="width: 100%"
          empty-text="暂无预约数据"
        >
          <el-table-column prop="seatNumber" label="座位号" width="100" />

          <el-table-column label="预约时间" width="160">
            <template slot-scope="scope">
              {{ formatFullTime(scope.row) }}
            </template>
          </el-table-column>

          <el-table-column label="学号" width="120">
            <template slot-scope="scope">
              <span>{{ getMaskStuId(scope.row.userStudentId) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="startTime" label="开始时间" width="120" />
          <el-table-column prop="endTime" label="结束时间" width="120" />

          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="medium">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template slot-scope="scope">
              <el-button
                v-if="canShowViolationBtn(scope.row)"
                type="text"
                size="small"
                style="color: #f56c6c"
                @click="handleViolation(scope.row)"
                >违规处理</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[8]"
          :page-size="pagination.pageSize"
          layout="total, prev, pager, next, jumper"
          :total="todayBookings.length"
          style="margin-top: 15px; text-align: right"
        >
        </el-pagination>
      </el-card>
    </el-card>

    <el-dialog
      title="违规处理"
      :visible.sync="violationDialogVisible"
      width="450px"
    >
      <el-form
        :model="violationForm"
        :rules="violationRules"
        ref="violationForm"
        label-width="80px"
      >
        <el-form-item label="违规类型" prop="type">
          <el-select v-model="violationForm.type" placeholder="请选择违规类型">
            <el-option label="未签到" value="NO_SHOW" />
            <el-option label="迟到" value="LATE" />
            <el-option label="早退" value="EARLY_LEAVE" />
            <el-option label="其他违规" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="违规描述" prop="description">
          <el-input
            v-model="violationForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入违规描述"
          />
        </el-form-item>
        <el-form-item label="扣分">
          <el-input-number v-model="violationForm.points" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="violationDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitViolation">确定处理</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyManagedSpace,
  getTodayBookings,
  getAvailableSeats,
  markViolation,
} from "@/api/teacherSpace";

export default {
  name: "TeacherSpaces",
  data() {
    return {
      spaceName: "未绑定共享空间",
      todayBookings: [],
      availableSeats: 0,
      loading: false,
      violationDialogVisible: false,
      violationForm: {
        bookingId: null,
        type: "NO_SHOW",
        description: "",
        points: 1,
      },
      violationRules: {
        type: [
          { required: true, message: "请选择违规类型", trigger: "change" },
        ],
        description: [
          { required: true, message: "请输入违规描述", trigger: "blur" },
        ],
      },
      pagination: {
        pageNum: 1,
        pageSize: 8,
      },
    };
  },
  computed: {
    currentPageData() {
      const { pageNum, pageSize } = this.pagination;
      const start = (pageNum - 1) * pageSize;
      const end = start + pageSize;
      return this.todayBookings.slice(start, end);
    },
    todayBookingCount() {
      const today = new Date().toISOString().split("T")[0];
      return this.todayBookings.filter((b) => b.bookingDate === today).length;
    },
  },
  mounted() {
    this.loadMySpace();
    this.loadSeats();
  },
  methods: {
    getMaskStuId(id) {
      if (!id || id.length < 3) return "无";
      return id.charAt(0) + "****" + id.slice(-2);
    },

    formatFullTime(row) {
      if (!row.bookingDate || !row.startTime) return "无";
      return row.bookingDate + " " + row.startTime;
    },

    async loadMySpace() {
      this.loading = true;
      try {
        const res = await getMyManagedSpace();
        if (res.code === 200 && res.data) {
          this.spaceName = res.data.name || "管理共享空间 ID: " + res.data.id;
          await this.loadBookings();
        } else {
          this.$message.warning(res.msg || "未绑定管理共享空间");
        }
      } catch (err) {
        this.$message.error(
          "获取共享空间信息失败: " + (err.message || "网络错误"),
        );
      } finally {
        this.loading = false;
      }
    },

    async loadBookings() {
      try {
        const res = await getTodayBookings();
        if (res.code === 200) {
          this.todayBookings = res.data || [];
        } else {
          this.$message.warning(res.msg || "获取预约记录失败");
        }
      } catch (err) {
        this.$message.error("获取预约记录失败: " + (err.message || "网络错误"));
      }
    },

    async loadSeats() {
      try {
        const res = await getAvailableSeats();
        if (res.code === 200) {
          this.availableSeats = res.data || 0;
        } else {
          this.availableSeats = 0;
        }
      } catch (err) {
        this.availableSeats = 0;
      }
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.pagination.pageNum = 1;
    },

    handleCurrentChange(val) {
      this.pagination.pageNum = val;
    },

    getStatusType(status) {
      const typeMap = {
        RESERVED: "info",
        CHECKED_IN: "success",
        CHECKED_OUT: "primary",
        CANCELLED: "warning",
        EXPIRED: "danger",
      };
      return typeMap[status] || "info";
    },
    getStatusText(status) {
      const textMap = {
        RESERVED: "已预约",
        CHECKED_IN: "已签到",
        CHECKED_OUT: "已签退",
        CANCELLED: "已取消",
        EXPIRED: "已过期",
      };
      return textMap[status] || status;
    },
    canShowViolationBtn(row) {
      if (row.status !== "RESERVED") {
        return false;
      }
      const now = new Date();
      const today = now.toISOString().split("T")[0];
      if (row.bookingDate !== today) {
        return false;
      }
      const currentTime = now.getHours() * 60 + now.getMinutes();
      const [startH, startM] = row.startTime.split(":").map(Number);
      const [endH, endM] = row.endTime.split(":").map(Number);
      const startMinutes = startH * 60 + startM;
      const endMinutes = endH * 60 + endM;
      return currentTime >= startMinutes && currentTime <= endMinutes;
    },
    handleViolation(row) {
      this.violationForm = {
        bookingId: row.id,
        type: "NO_SHOW",
        description: "",
        points: 1,
      };
      this.violationDialogVisible = true;
    },
    submitViolation() {
      this.$refs.violationForm.validate(async (valid) => {
        if (valid) {
          try {
            await markViolation(this.violationForm);
            this.$message.success("违规处理成功");
            this.violationDialogVisible = false;
            this.loadBookings();
          } catch (err) {
            this.$message.error(err.message || "违规处理失败");
          }
        }
      });
    },
  },
};
</script>

<style scoped>
.teacher-spaces {
  padding: 20px;
}
.el-card {
  margin-bottom: 10px;
}
</style>
