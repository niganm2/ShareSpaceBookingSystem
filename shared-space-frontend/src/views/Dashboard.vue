<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ spaceCount }}</div>
              <div class="stat-label">{{ spaceLabel }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon seat-icon">
              <i class="el-icon-s-grid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ seatCount }}</div>
              <div class="stat-label">{{ seatLabel }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon violation-icon">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ violationCount }}</div>
              <div class="stat-label">违规记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近7天预约统计折线图 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <div class="chart-title">{{ chartTitle }}</div>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="operation-card">
          <div class="operation-title">快速操作</div>
          <div class="operation-buttons">
            <el-button type="primary" size="large" @click="goToBooking">
            <i class="el-icon-plus"></i> 立即预约
          </el-button>
          <el-button type="success" size="large" @click="goToMyBookings">
            <i class="el-icon-document"></i> 我的预约
          </el-button>
          <el-button type="warning" size="large" @click="goToAiChat">
            <i class="el-icon-service"></i> AI智能助手
          </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
import { getRecent7DaysBookingCount as getStudentRecent7Days } from "@/api/booking";
import {
  getRecent7DaysBookingCount as getTeacherRecent7Days,
  getAvailableSeats,
} from "@/api/teacherSpace";
import { getSpaceList } from "@/api/space";
import { getAllRecent7Days } from "@/api/admin";
import {
  getMyViolationCount,
  getTeacherViolationCount,
  getAdminViolationCount,
} from "@/api/violation";

export default {
  /* eslint-disable vue/multi-word-component-names */
  name: "Dashboard",
  data() {
    return {
      spaceCount: 0,
      seatCount: 0,
      violationCount: 0,
      chartInstance: null,
      recent7Days: [],
      bookingData: [],
      userRole: "",
      spaceLabel: "共享空间",
      seatLabel: "可用座位",
      chartTitle: "近7天预约统计",
    };
  },
  mounted() {
    this.getUserRole();
    this.initDashboardData();
  },
  methods: {
    getUserRole() {
      const userStr = localStorage.getItem("user");
      if (userStr) {
        const user = JSON.parse(userStr);
        this.userRole = user.role || "";
      }
    },
    async initDashboardData() {
      try {
        this.generateRecent7Days();

        if (this.userRole === "TEACHER") {
          this.spaceLabel = "管理共享空间";
          this.seatLabel = "剩余座位";
          this.chartTitle = "近7天预约统计（我的共享空间）";
          this.spaceCount = 1;

          const chartRes = await getTeacherRecent7Days();
          this.mapDateAndCount(chartRes.data);

          try {
            const seatRes = await getAvailableSeats();
            if (seatRes.code === 200) {
              this.seatCount = seatRes.data || 0;
            }
          } catch (e) {
            // ignore
          }

          try {
            const violationRes = await getTeacherViolationCount();
            if (violationRes.code === 200) {
              this.violationCount = violationRes.data || 0;
            }
          } catch (e) {
            // ignore
          }
        } else if (this.userRole === "ADMIN") {
          this.spaceLabel = "共享空间";
          this.seatLabel = "总座位数";
          this.chartTitle = "近7天预约统计（全平台）";

          const chartRes = await getAllRecent7Days();
          this.mapDateAndCount(chartRes.data);

          try {
            const spaceRes = await getSpaceList();
            if (spaceRes.code === 200 && spaceRes.data) {
              this.spaceCount = spaceRes.data.spaceCount || 0;
              this.seatCount = spaceRes.data.availableSeatCount || 0;
            }
          } catch (e) {
            // ignore
          }

          try {
            const violationRes = await getAdminViolationCount();
            if (violationRes.code === 200) {
              this.violationCount = violationRes.data || 0;
            }
          } catch (e) {
            // ignore
          }
        } else {
          this.spaceLabel = "共享空间";
          this.seatLabel = "可用座位";
          this.chartTitle = "近7天预约统计（个人）";

          const chartRes = await getStudentRecent7Days();
          this.mapDateAndCount(chartRes.data);

          try {
            const spaceRes = await getSpaceList();
            if (spaceRes.code === 200 && spaceRes.data) {
              this.spaceCount = spaceRes.data.spaceCount || 0;
              this.seatCount = spaceRes.data.availableSeatCount || 0;
            }
          } catch (e) {
            // ignore
          }

          try {
            const violationRes = await getMyViolationCount();
            if (violationRes.code === 200 && violationRes.data) {
              this.violationCount = violationRes.data.count || 0;
            }
          } catch (e) {
            // ignore
          }
        }

        this.initChart();
      } catch (error) {
        this.$message.error("数据加载失败");
      }
    },
    generateRecent7Days() {
      const dates = [];
      const today = new Date();
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        dates.push(`${year}-${month}-${day}`);
      }
      this.recent7Days = dates;
      this.bookingData = new Array(7).fill(0);
    },
    mapDateAndCount(serverData) {
      if (!serverData) return;
      serverData.forEach((item) => {
        const index = this.recent7Days.indexOf(item.date);
        if (index !== -1) {
          this.bookingData[index] = item.count;
        }
      });
    },
    initChart() {
      this.$nextTick(() => {
        if (!this.$refs.chartRef) {
          return;
        }
        const echarts = require("echarts");
        this.chartInstance = echarts.init(this.$refs.chartRef);

        const maxVal = Math.max(...this.bookingData, 6);
        const yMax = Math.max(6, Math.ceil(maxVal * 1.2));

        const option = {
          title: {
            text: this.chartTitle,
            left: "center",
          },
          tooltip: {
            trigger: "axis",
            formatter: "{b}<br/>预约次数：{c}",
          },
          xAxis: {
            type: "category",
            data: this.recent7Days.map((date) =>
              date.split("-").slice(1).join("-"),
            ),
            axisLine: {
              lineStyle: {
                color: "#333",
              },
            },
          },
          yAxis: {
            type: "value",
            min: 0,
            max: yMax,
            minInterval: 1,
            axisLine: {
              lineStyle: {
                color: "#333",
              },
            },
          },
          series: [
            {
              name: "预约次数",
              type: "line",
              data: this.bookingData,
              smooth: true,
              itemStyle: {
                color: "#409EFF",
              },
              lineStyle: {
                color: "#409EFF",
              },
            },
          ],
          grid: {
            left: "3%",
            right: "4%",
            bottom: "3%",
            containLabel: true,
          },
        };

        this.chartInstance.setOption(option);
        window.addEventListener("resize", this.handleResize);
      });
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },
    goToBooking() {
      this.$router.push("/booking");
    },
    goToMyBookings() {
      this.$router.push("/my-bookings");
    },
    goToAiChat() {
      this.$router.push("/ai-chat");
    },
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
      window.removeEventListener("resize", this.handleResize);
    }
  },
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  width: 100%;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 20px;
}

.user-icon {
  background-color: #67c23a;
}

.seat-icon {
  background-color: #409eff;
}

.violation-icon {
  background-color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.operation-card {
  padding: 20px;
}

.operation-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.operation-buttons {
  display: flex;
  gap: 15px;
}
</style>
