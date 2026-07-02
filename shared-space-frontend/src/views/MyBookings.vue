<template>
  <div class="max-h-screen overflow-hidden box-border p-1 m-0 space-y-1">
    <div v-loading="scoreLoading" v-if="score < 80"
      style="
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        gap: 15px;
      "
    >
      <el-image
        src="https://cube.elemecdn.com/0/05/05a0e5b9b9da2e917446acc9f976dpng.png"
        style="width: 150px"
      ></el-image>
      <div style="font-size: 16px; color: #f56c6c; font-weight: bold">
        您的信誉分数低于80分，已被限制预约
      </div>
      <div style="font-size: 14px; color: #909399">
        解封时间：下个月1号 00:00
      </div>
    </div>

    <div v-else>
      <el-card class="h-full flex flex-col">
        <div slot="header" class="flex items-center gap-2">
          <span class="text-sm font-medium">我的预约</span>
        </div>

        <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="gap-2">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="已预约" name="RESERVED"></el-tab-pane>
          <el-tab-pane label="已签到" name="CHECKED_IN"></el-tab-pane>
          <el-tab-pane label="已签退" name="CHECKED_OUT"></el-tab-pane>
          <el-tab-pane label="已取消" name="CANCELLED"></el-tab-pane>
          <el-tab-pane label="已过期" name="EXPIRED"></el-tab-pane>
        </el-tabs>

        <div class="filter-bar gap-2 mt-2">
          <el-select
            v-model="filters.spaceName"
            placeholder="空间名称"
            clearable
            class="w-45 mr-2"
            @change="handleSpaceChange"
            @clear="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="space in spaceList"
              :key="space.id"
              :label="space.name"
              :value="space.name"
            ></el-option>
          </el-select>

          <el-select
            v-model="filters.seatNumber"
            placeholder="座位号"
            clearable
            class="w-38 mr-2"
            :disabled="!filters.spaceName"
            @clear="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="seat in seatList"
              :key="seat.id"
              :label="seat.seatNumber"
              :value="seat.seatNumber"
            ></el-option>
          </el-select>

          <el-date-picker
            v-model="filters.bookingDate"
            type="date"
            placeholder="预约日期"
            clearable
            class="w-45 mr-2"
            @change="handleFilter"
          >
          </el-date-picker>

          <el-select
            v-model="filters.status"
            placeholder="预约状态"
            clearable
            class="w-38 mr-2"
            @change="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="已预约" value="RESERVED"></el-option>
            <el-option label="已签到" value="CHECKED_IN"></el-option>
            <el-option label="已签退" value="CHECKED_OUT"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="已过期" value="EXPIRED"></el-option>
          </el-select>

          <el-button
            type="primary"
            icon="el-icon-search"
            @click="handleFilter"
            class="ml-auto"
            >筛选</el-button
          >
          <el-button icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </div>

        <el-table
          :data="currentPageData"
          v-loading="loading"
          class="w-full mt-3"
          height="560"
        >
          <el-table-column
            prop="spaceName"
            label="空间名称"
            width="150"
          ></el-table-column>
          <el-table-column
            prop="seatNumber"
            label="座位号"
            width="100"
          ></el-table-column>
          <el-table-column
            prop="bookingDate"
            label="预约日期"
            width="120"
          ></el-table-column>
          <el-table-column label="时间段" width="180">
            <template slot-scope="scope">
              {{ scope.row.startTime }} - {{ scope.row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320">
            <template slot-scope="scope">
              <el-button
                v-if="
                  scope.row.status === 'RESERVED' &&
                  !isTimePassed(scope.row) &&
                  !scope.row.isUpdated
                "
                type="primary"
                size="small"
                @click="handleOpenEditDialog(scope.row)"
              >
                修改
              </el-button>

              <el-button
                v-if="scope.row.status === 'RESERVED' && !isExpired(scope.row)"
                type="success"
                size="small"
                @click="handleCheckIn(scope.row)"
              >
                签到
              </el-button>

              <el-button
                v-if="scope.row.status === 'CHECKED_IN'"
                type="warning"
                size="small"
                @click="handleCheckOut(scope.row)"
              >
                签退
              </el-button>

              <el-button
                v-if="scope.row.status === 'RESERVED' && !isExpired(scope.row)"
                type="danger"
                size="small"
                @click="handleCancel(scope.row)"
              >
                取消
              </el-button>

              <el-button
                v-if="canDelete(scope.row)"
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[8]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredBookingList.length"
          class="mt-auto text-right"
        >
        </el-pagination>
      </el-card>

      <el-dialog
        title="修改预约时间"
        :visible.sync="editDialogVisible"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form
          :model="editForm"
          :rules="editRules"
          ref="editForm"
          label-width="100px"
        >
          <el-form-item label="预约日期">
            <el-date-picker
              v-model="editForm.bookingDate"
              type="date"
              placeholder="选择日期"
              value-format="yyyy-MM-dd"
              style="width: 100%"
              :disabled="true"
            />
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-select
              v-model="editForm.startTime"
              placeholder="选择开始时间"
              style="width: 100%"
              @change="editForm.endTime = null"
            >
              <el-option
                v-for="time in editStartTimeOptions"
                :key="time"
                :label="time"
                :value="time"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="结束时间" prop="endTime">
            <el-select
              v-model="editForm.endTime"
              placeholder="选择结束时间"
              style="width: 100%"
            >
              <el-option
                v-for="time in editEndTimeOptions"
                :key="time"
                :label="time"
                :value="time"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <span slot="footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitEditBooking"
            :loading="editLoading"
          >
            确认修改
          </el-button>
        </span>
      </el-dialog>

      <el-dialog
        title="取消预约"
        :visible.sync="cancelDialogVisible"
        width="400px"
      >
        <el-form :model="cancelForm" label-width="80px">
          <el-form-item label="取消原因">
            <el-input
              v-model="cancelForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入取消原因"
            ></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="gap-2">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="confirmCancel"
            :loading="cancelLoading"
            >确认取消</el-button
          >
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  getBookingList,
  checkIn,
  checkOut,
  cancelBooking,
  deleteBooking,
  updateBooking,
} from "@/api/booking";
import { getSpaceList } from "@/api/space";
import { getMyScore } from "@/api/violation";

export default {
  name: "MyBookings",
  data() {
    return {
      activeTab: "all",
      bookingList: [],
      loading: false,
      cancelDialogVisible: false,
      cancelLoading: false,
      cancelForm: { id: null, reason: "" },
      pagination: { pageNum: 1, pageSize: 8, total: 0 },
      filters: {
        spaceName: "",
        seatNumber: "",
        bookingDate: "",
        status: "",
      },
      spaceList: [],
      seatList: [],
      editDialogVisible: false,
      editLoading: false,
      editForm: {
        id: null,
        bookingDate: null,
        startTime: null,
        endTime: null,
      },
      editRules: {
        startTime: [
          { required: true, message: "请选择开始时间", trigger: "change" },
        ],
        endTime: [{ validator: this.validateDuration, trigger: "change" }],
      },
      score: 100,
      scoreLoading: true,
    };
  },
  computed: {
    filteredBookingList() {
      let list = this.bookingList;
      if (this.activeTab !== "all") {
        list = list.filter((item) => item.status === this.activeTab);
      }
      if (this.filters.spaceName) {
        list = list.filter((item) => item.spaceName === this.filters.spaceName);
      }
      if (this.filters.seatNumber) {
        list = list.filter(
          (item) => item.seatNumber === this.filters.seatNumber,
        );
      }
      if (this.filters.bookingDate) {
        list = list.filter(
          (item) => item.bookingDate === this.filters.bookingDate,
        );
      }
      if (this.filters.status) {
        list = list.filter((item) => item.status === this.filters.status);
      }
      return list;
    },
    currentPageData() {
      const { pageNum, pageSize } = this.pagination;
      const start = (pageNum - 1) * pageSize;
      const end = start + pageSize;
      return this.filteredBookingList.slice(start, end);
    },
    editStartTimeOptions() {
      const all = [
        "08:00",
        "08:30",
        "09:00",
        "09:30",
        "10:00",
        "10:30",
        "11:00",
        "11:30",
        "12:00",
        "12:30",
        "13:00",
        "13:30",
        "14:00",
        "14:30",
        "15:00",
        "15:30",
        "16:00",
        "16:30",
        "17:00",
        "17:30",
        "18:00",
        "18:30",
        "19:00",
        "19:30",
        "20:00",
        "20:30",
        "21:00",
        "21:30",
      ];
      if (!this.editForm.bookingDate) return all;

      const sel = new Date(this.editForm.bookingDate);
      const today = new Date();
      const isToday = sel.toDateString() === today.toDateString();
      if (!isToday) return all;

      const h = today.getHours();
      const m = today.getMinutes();
      const min = m <= 30 ? `${h}:30` : `${h + 1}:00`;
      return all.filter((t) => t >= min);
    },
    editEndTimeOptions() {
      if (!this.editForm.startTime) return [];
      const all = [
        "08:00",
        "08:30",
        "09:00",
        "09:30",
        "10:00",
        "10:30",
        "11:00",
        "11:30",
        "12:00",
        "12:30",
        "13:00",
        "13:30",
        "14:00",
        "14:30",
        "15:00",
        "15:30",
        "16:00",
        "16:30",
        "17:00",
        "17:30",
        "18:00",
        "18:30",
        "19:00",
        "19:30",
        "20:00",
        "20:30",
        "21:00",
        "21:30",
        "22:00",
      ];
      const idx = all.indexOf(this.editForm.startTime);
      return all.slice(idx + 1);
    },
  },
  mounted() {
    this.loadScore();
    document.body.style.overflow = "hidden";
  },
  beforeDestroy() {
    document.body.style.overflow = "";
  },
  methods: {
    loadScore() {
      getMyScore()
        .then((res) => {
          this.score = res.data.score;
        })
        .finally(() => {
          this.scoreLoading = false;
          if (this.score >= 80) {
            this.loadBookingList();
            this.loadSpaceList();
          }
        });
    },
    async loadBookingList() {
      this.loading = true;
      const params = {};
      if (this.activeTab !== "all") params.status = this.activeTab;
      try {
        const res = await getBookingList(params);
        this.bookingList = res.data.records || res.data || [];
        this.pagination.total = res.data.total || this.bookingList.length;
      } finally {
        this.loading = false;
      }
    },
    async loadSpaceList() {
      try {
        const res = await getSpaceList({ pageNum: 1, pageSize: 100 });
        this.spaceList =
          res.data?.records ||
          res.data?.list ||
          res.data?.data ||
          res.data?.spaces ||
          [];
      } catch (err) {
        this.spaceList = [];
      }
    },
    handleSpaceChange() {
      this.filters.seatNumber = "";
      if (!this.filters.spaceName) {
        this.seatList = [];
        return;
      }
      const seats = this.bookingList
        .filter((item) => item.spaceName === this.filters.spaceName)
        .map((item) => ({ id: item.seatNumber, seatNumber: item.seatNumber }));
      this.seatList = [
        ...new Map(seats.map((s) => [s.seatNumber, s])).values(),
      ];
    },
    handleTabClick() {
      this.pagination.pageNum = 1;
      this.loadBookingList();
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val;
    },
    handleCurrentChange(val) {
      this.pagination.pageNum = val;
    },
    handleFilter() {
      this.pagination.pageNum = 1;
    },
    handleReset() {
      this.filters = {
        spaceName: "",
        seatNumber: "",
        bookingDate: "",
        status: "",
      };
      this.seatList = [];
      this.pagination.pageNum = 1;
    },
    async handleCheckIn(row) {
      const now = new Date();
      const s = new Date(row.bookingDate + " " + row.startTime);
      const e = new Date(row.bookingDate + " " + row.endTime);
      if (now < s) return this.$message.warning("未到预约时段");
      if (now > e) return this.$message.warning("已过预约时间");
      this.$confirm("确认签到？")
        .then(async () => {
          await checkIn(row.id);
          this.$message.success("签到成功");
          this.loadBookingList();
        })
        .catch(() => {});
    },
    async handleCheckOut(row) {
      this.$confirm("确认签退？")
        .then(async () => {
          await checkOut(row.id);
          this.$message.success("签退成功");
          this.loadBookingList();
        })
        .catch(() => {});
    },
    handleCancel(row) {
      this.cancelForm.id = row.id;
      this.cancelForm.reason = "";
      this.cancelDialogVisible = true;
    },
    async confirmCancel() {
      this.cancelLoading = true;
      try {
        await cancelBooking(this.cancelForm.id, this.cancelForm.reason);
        this.$message.success("已取消");
        this.cancelDialogVisible = false;
        this.loadBookingList();
      } finally {
        this.cancelLoading = false;
      }
    },
    async handleDelete(row) {
      this.$confirm("确定删除该记录吗？", "提示", { type: "info" })
        .then(async () => {
          await deleteBooking(row.id);
          this.$message.success("删除成功");
          this.loadBookingList();
        })
        .catch(() => {
          this.$message.error("删除失败");
        });
    },
    getStatusType(s) {
      return (
        {
          RESERVED: "primary",
          CHECKED_IN: "success",
          CHECKED_OUT: "info",
          CANCELLED: "warning",
          EXPIRED: "danger",
        }[s] || "info"
      );
    },
    getStatusText(s) {
      return (
        {
          RESERVED: "已预约",
          CHECKED_IN: "已签到",
          CHECKED_OUT: "已签退",
          CANCELLED: "已取消",
          EXPIRED: "已过期",
        }[s] || s
      );
    },
    canDelete(row) {
      return (
        ["CANCELLED", "EXPIRED", "CHECKED_OUT"].includes(row.status) ||
        this.isExpired(row)
      );
    },
    isExpired(row) {
      return new Date() > new Date(row.bookingDate + " " + row.endTime);
    },
    handleOpenEditDialog(row) {
      this.editForm = {
        id: row.id,
        bookingDate: row.bookingDate,
        startTime: row.startTime,
        endTime: row.endTime,
      };
      this.editDialogVisible = true;
    },
    isTimePassed(row) {
      const now = new Date();
      const slot = new Date(row.bookingDate + " " + row.startTime);
      return now >= slot;
    },
    validateDuration(rule, value, cb) {
      if (!this.editForm.startTime || !value) return cb();
      const s = this.editForm.startTime;
      const e = value;
      const sh = +s.split(":")[0],
        sm = +s.split(":")[1];
      const eh = +e.split(":")[0],
        em = +e.split(":")[1];
      const total = eh * 60 + em - (sh * 60 + sm);
      if (total > 180) return cb(new Error("最长3小时"));
      cb();
    },
    async submitEditBooking() {
      if (!this.editForm.startTime) {
        this.$message.warning("请选择开始时间");
        return;
      }
      if (!this.editForm.endTime) {
        this.$message.warning("请选择结束时间");
        return;
      }

      this.editLoading = true;
      try {
        await updateBooking(this.editForm.id, {
          startTime: this.editForm.startTime,
          endTime: this.editForm.endTime,
        });
        this.$message.success("修改成功");
        this.editDialogVisible = false;
        this.loadBookingList();
      } catch (err) {
        this.$message.error(err.message || "修改失败");
      } finally {
        this.editLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.max-h-screen {
  height: calc(100vh - 100px) !important;
}
.overflow-hidden {
  overflow: hidden !important;
}
.box-border {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}
.p-1 {
  padding: 0.25rem;
}
.m-0 {
  margin: 0;
}
.space-y-1 {
  padding-top: 0.25rem;
}
.h-full {
  height: 100%;
}
.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
}
.mt-auto {
  margin-top: auto;
}
.items-center {
  align-items: center;
}
.gap-2 {
  gap: 0.5rem;
}
.text-sm {
  font-size: 0.875rem;
}
.font-medium {
  font-weight: 500;
}
.w-45 {
  width: 180px;
}
.w-38 {
  width: 150px;
}
.mr-2 {
  margin-right: 0.5rem;
}
.ml-auto {
  margin-left: auto;
}
.w-full {
  width: 100%;
}
.mt-3 {
  margin-top: 0.75rem;
}
.text-right {
  text-align: right;
}
</style>
