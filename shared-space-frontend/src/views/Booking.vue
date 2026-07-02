<template>
  <div class="booking">
    <el-card v-loading="scoreLoading">
      <div slot="header">
        <span>座位预约</span>
        <span v-if="userScore !== null" style="float: right; font-size: 14px">
          信用分：
          <el-tag :type="userScore >= 80 ? 'success' : 'danger'" size="small">
            {{ userScore }}
          </el-tag>
        </span>
      </div>
      <el-alert
        v-if="showScoreAlert"
        :title="scoreAlertMsg"
        type="error"
        :closable="false"
        style="margin-bottom: 15px"
      />
      <el-form
        v-if="!showScoreAlert"
        :model="bookingForm"
        :rules="rules"
        ref="bookingForm"
        label-width="100px"
      >
        <el-form-item label="选择空间" prop="spaceId">
          <el-select
            v-model="bookingForm.spaceId"
            placeholder="请选择空间"
            @change="handleSpaceChange"
            style="width: 100%"
          >
            <el-option
              v-for="space in spaceList"
              :key="space.id"
              :label="space.name"
              :value="space.id"
            >
              <span>{{ space.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{
                space.location
              }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择日期" prop="bookingDate">
          <el-date-picker
            v-model="bookingForm.bookingDate"
            type="date"
            placeholder="选择日期"
            :picker-options="dateOptions"
            value-format="yyyy-MM-dd"
            style="width: 100%"
            @change="loadBookedSeats"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="选择座位" prop="seatId">
          <div class="seat-grid">
            <div
              v-for="seat in seatList"
              :key="seat.id"
              :class="['seat-item', getSeatClass(seat)]"
              @click="handleSelectSeat(seat)"
            >
              {{ seat.seatNumber }}
            </div>
          </div>
          <div v-if="selectedSeatBookings.length > 0" class="booked-time-info">
            <el-alert
              title="该座位以下时间段已被预约："
              type="warning"
              :closable="false"
              style="margin-top: 10px"
            >
              <div
                v-for="booking in selectedSeatBookings"
                :key="booking.id"
                class="time-slot"
              >
                {{ booking.startTime }} - {{ booking.endTime }}
              </div>
            </el-alert>
          </div>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-select
            v-model="bookingForm.startTime"
            placeholder="选择开始时间"
            style="width: 100%"
            @change="bookingForm.endTime = null"
          >
            <el-option
              v-for="time in startTimeOptions"
              :key="time"
              :label="time"
              :value="time"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-select
            v-model="bookingForm.endTime"
            placeholder="选择结束时间"
            style="width: 100%"
          >
            <el-option
              v-for="time in endTimeOptions"
              :key="time"
              :label="time"
              :value="time"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading"
            >提交预约</el-button
          >
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="座位状态说明" :visible.sync="showLegend" width="400px">
      <div class="legend-content">
        <div class="legend-item">
          <div class="legend-box available"></div>
          <span>可用</span>
        </div>
        <div class="legend-item">
          <div class="legend-box selected"></div>
          <span>已选择</span>
        </div>
        <div class="legend-item">
          <div class="legend-box has-booking"></div>
          <span>部分时段已预约</span>
        </div>
        <div class="legend-item">
          <div class="legend-box disabled"></div>
          <span>不可用</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
import { getSpaceList } from "@/api/space";
import { getSeatListBySpaceId } from "@/api/seat";
import { getBookingsByDateAndSpace, createBooking } from "@/api/booking";
import { getMyScore } from "@/api/violation";

export default {
  name: "Booking",
  data() {
    return {
      userScore: null,
      scoreLoading: true,
      unlockTimeStr: "",
      showScoreAlert: false,
      scoreAlertMsg: "",
      bookingForm: {
        spaceId: null,
        seatId: null,
        bookingDate: null,
        startTime: null,
        endTime: null,
      },
      rules: {
        spaceId: [{ required: true, message: "请选择空间", trigger: "change" }],
        bookingDate: [
          { required: true, message: "请选择日期", trigger: "change" },
        ],
        seatId: [{ required: true, message: "请选择座位", trigger: "change" }],
        startTime: [
          { required: true, message: "请选择开始时间", trigger: "change" },
        ],
        endTime: [
          { required: true, message: "请选择结束时间", trigger: "change" },
          { validator: this.validateDuration, trigger: "change" },
        ],
      },
      spaceList: [],
      seatList: [],
      bookedSeats: [],
      loading: false,
      showLegend: false,
      dateOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        },
      },
    };
  },
  computed: {
    startTimeOptions() {
      const allOptions = [
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

      if (!this.bookingForm.bookingDate) return allOptions;

      const selected = new Date(this.bookingForm.bookingDate);
      const today = new Date();
      const todayStr = today.toISOString().split("T")[0];
      const selectedStr = selected.toISOString().split("T")[0];
      const isToday = selectedStr === todayStr;

      if (!isToday) return allOptions;

      const now = new Date();
      const h = now.getHours();
      const m = now.getMinutes();

      let earliestAllowableTime;

      if (m <= 30) {
        earliestAllowableTime = `${String(h).padStart(2, 0)}:30`;
      } else {
        earliestAllowableTime = `${String(h + 1).padStart(2, 0)}:00`;
      }

      return allOptions.filter((t) => t >= earliestAllowableTime);
    },
    endTimeOptions() {
      if (!this.bookingForm.startTime) return [];
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
      const idx = all.indexOf(this.bookingForm.startTime);
      return all.slice(idx + 1);
    },
    selectedSeatBookings() {
      if (!this.bookingForm.seatId) return [];
      return this.bookedSeats.filter(
        (b) => b.seatId === this.bookingForm.seatId,
      );
    },
  },
  mounted() {
    this.loadUserScore();
  },
  methods: {
    loadUserScore() {
      getMyScore()
        .then((res) => {
          if (res.code === 200 && res.data) {
            this.userScore = res.data.score;
            if (this.userScore < 80) {
              const now = new Date();
              const unlockTime = new Date(
                now.getFullYear(),
                now.getMonth() + 1,
                1,
                0,
                0,
                0,
              );
              const year = unlockTime.getFullYear();
              const month = String(unlockTime.getMonth() + 1).padStart(2, "0");
              const day = String(unlockTime.getDate()).padStart(2, "0");
              const unlockTimeStr = `${year}年${month}月${day}日 00:00`;

              this.showScoreAlert = true;
              this.scoreAlertMsg = `您的信用分低于80分，将于 ${unlockTimeStr} 解封，届时可正常预约`;
              this.bookingForm = {
                spaceId: null,
                seatId: null,
                bookingDate: null,
                startTime: null,
                endTime: null,
              };
            } else {
              this.showScoreAlert = false;
              this.loadSpaceList();
            }
          }
        })
        .catch(() => {
          this.showScoreAlert = false;
          this.loadSpaceList();
        })
        .finally(() => {
          this.scoreLoading = false;
        });
    },
    loadSpaceList() {
      getSpaceList()
        .then((res) => {
          let list = [];
          if (res?.data?.records && Array.isArray(res.data.records)) {
            list = res.data.records;
          } else if (res?.data?.spaces && Array.isArray(res.data.spaces)) {
            list = res.data.spaces;
          } else if (res?.data && Array.isArray(res.data)) {
            list = res.data;
          } else if (res && Array.isArray(res)) {
            list = res;
          }
          this.spaceList = list;
        })
        .catch(() => {
          this.spaceList = [];
        });
    },
    handleSpaceChange() {
      this.bookingForm.seatId = null;
      this.seatList = [];
      this.bookedSeats = [];
      if (this.bookingForm.spaceId) {
        const selectedSpace = this.spaceList.find(
          (s) => s.id === this.bookingForm.spaceId,
        );
        if (selectedSpace && selectedSpace.status === 0) {
          this.$alert(
            `该共享空间维修中${
              selectedSpace.disabledReason
                ? "：" + selectedSpace.disabledReason
                : ""
            }`,
            "提示",
            { confirmButtonText: "确定", type: "warning" },
          );
          this.bookingForm.spaceId = null;
          return;
        }
        this.loadSeatList();
      }
    },
    loadSeatList() {
      if (this.bookingForm.spaceId) {
        getSeatListBySpaceId(this.bookingForm.spaceId).then((res) => {
          this.seatList = res.data;
        });
      }
    },
    loadBookedSeats() {
      if (this.bookingForm.bookingDate && this.bookingForm.spaceId) {
        getBookingsByDateAndSpace(
          this.bookingForm.bookingDate,
          this.bookingForm.spaceId,
        ).then((res) => {
          this.bookedSeats = res.data;
        });
      }
    },
    getSeatClass(seat) {
      if (seat.status === 0) {
        return "disabled";
      }
      if (this.bookingForm.seatId === seat.id) {
        return "selected";
      }
      if (this.bookedSeats.some((b) => b.seatId === seat.id)) {
        return "has-booking";
      }
      return "available";
    },
    handleSelectSeat(seat) {
      if (seat.status === 0) {
        this.$message.warning("该座位不可用");
        return;
      }
      this.bookingForm.seatId = seat.id;
    },
    handleSubmit() {
      this.$refs.bookingForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          createBooking(this.bookingForm)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success("预约成功");
                this.handleReset();
              } else {
                this.$message.error(res.message || "预约失败");
              }
            })
            .catch(() => {
              this.$message.error("预约失败");
            })
            .finally(() => {
              this.loading = false;
            });
        }
      });
    },
    handleReset() {
      this.$refs.bookingForm.resetFields();
      this.seatList = [];
      this.bookedSeats = [];
    },
    validateDuration(rule, value, callback) {
      if (!this.bookingForm.startTime || !value) return callback();
      const start = this.bookingForm.startTime.split(":");
      const end = value.split(":");
      const s = parseInt(start[0]) + parseInt(start[1]) / 60;
      const e = parseInt(end[0]) + parseInt(end[1]) / 60;
      if (e - s > 3) {
        return callback(new Error("预约时长不能超过3小时"));
      }
      callback();
    },
  },
  watch: {
    "bookingForm.bookingDate"(newVal) {
      if (newVal) this.loadBookedSeats();
    },
  },
};
</script>

<style scoped>
.booking {
  padding: 20px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.seat-item {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  border: 2px solid #dcdfe6;
  background: white;
}

.seat-item.available {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.seat-item.available:hover {
  transform: scale(1.1);
}

.seat-item.selected {
  background: #409eff;
  border-color: #409eff;
  color: white;
  transform: scale(1.1);
}

.seat-item.has-booking {
  background: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.seat-item.has-booking:hover {
  transform: scale(1.1);
}

.seat-item.booked {
  background: #f56c6c;
  border-color: #f56c6c;
  color: white;
  cursor: not-allowed;
}

.seat-item.disabled {
  background: #909399;
  border-color: #909399;
  color: white;
  cursor: not-allowed;
}

.legend-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-box {
  width: 30px;
  height: 30px;
  border-radius: 4px;
}

.legend-box.available {
  background: #67c23a;
}

.legend-box.selected {
  background: #409eff;
}

.legend-box.booked {
  background: #f56c6c;
}

.legend-box.has-booking {
  background: #e6a23c;
}

.legend-box.disabled {
  background: #909399;
}

.booked-time-info {
  margin-top: 10px;
}

.time-slot {
  padding: 5px 0;
  font-weight: bold;
  color: #e6a23c;
}
</style>
