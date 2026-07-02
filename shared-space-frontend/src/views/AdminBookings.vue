<template>
  <div class="admin-bookings">
    <el-card>
      <div slot="header">
        <span>预约记录管理</span>
      </div>

      <div class="filter-bar">
        <el-select
          v-model="filters.spaceName"
          placeholder="空间名称"
          clearable
          class="filter-item"
          @change="handleSpaceChange"
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
          class="filter-item"
          :disabled="!filters.spaceName"
          @change="handleFilter"
        >
          <el-option label="全部" value=""></el-option>
          <el-option
            v-for="(seat, index) in seatList"
            :key="index"
            :label="seat.seatNumber"
            :value="seat.seatNumber"
          ></el-option>
        </el-select>
        <el-date-picker
          v-model="filters.bookingDate"
          type="date"
          placeholder="预约日期"
          clearable
          value-format="yyyy-MM-dd"
          class="filter-item"
          @change="handleFilter"
        />
        <el-select
          v-model="filters.status"
          placeholder="预约状态"
          clearable
          class="filter-item"
          @change="handleFilter"
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="已预约" value="RESERVED"></el-option>
          <el-option label="已签到" value="CHECKED_IN"></el-option>
          <el-option label="已签退" value="CHECKED_OUT"></el-option>
          <el-option label="已取消" value="CANCELLED"></el-option>
          <el-option label="已过期" value="EXPIRED"></el-option>
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleFilter">
          查询
        </el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table
        :data="bookingList"
        v-loading="loading"
        style="width: 100%"
        height="500"
      >
        <el-table-column prop="spaceName" label="空间名称" width="150" />
        <el-table-column prop="seatNumber" label="座位号" width="100" />
        <el-table-column prop="userName" label="预约人" width="120" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column label="时间段" width="160">
          <template slot-scope="scope">
            {{ scope.row.startTime }} - {{ scope.row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="medium">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script>
import { getAllBookings, getSeatsBySpaceName } from "@/api/admin";
import { getSpaceList } from "@/api/space";

export default {
  name: "AdminBookings",
  data() {
    return {
      bookingList: [],
      loading: false,
      spaceList: [],
      seatList: [],
      filters: {
        spaceName: "",
        seatNumber: "",
        bookingDate: "",
        status: "",
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0,
      },
    };
  },
  mounted() {
    this.loadSpaceList();
    this.loadBookings();
  },
  methods: {
    async loadSpaceList() {
      try {
        const res = await getSpaceList();
        if (res.code === 200 && res.data) {
          this.spaceList = res.data.spaces || [];
        }
      } catch (err) {
        // ignore
      }
    },
    async handleSpaceChange(val) {
      this.filters.seatNumber = "";
      this.seatList = [];
      if (val) {
        try {
          const res = await getSeatsBySpaceName(val);
          if (res.code === 200) {
            this.seatList = (res.data || []).map((seatNumber) => ({
              seatNumber,
            }));
          }
        } catch (err) {
          // ignore
        }
      }
      this.handleFilter();
    },
    async loadBookings() {
      this.loading = true;
      try {
        const res = await getAllBookings({
          spaceName: this.filters.spaceName || null,
          seatNumber: this.filters.seatNumber || null,
          bookingDate: this.filters.bookingDate || null,
          status: this.filters.status || null,
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
        });
        if (res.code === 200) {
          this.bookingList = res.data.records || [];
          this.pagination.total = res.data.total || 0;
        }
      } catch (err) {
        this.$message.error("获取预约记录失败：" + (err.message || "网络错误"));
      } finally {
        this.loading = false;
      }
    },
    handleFilter() {
      this.pagination.pageNum = 1;
      this.loadBookings();
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
      this.loadBookings();
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.loadBookings();
    },
    handleCurrentChange(val) {
      this.pagination.pageNum = val;
      this.loadBookings();
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
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      return dateTime.replace("T", " ");
    },
  },
};
</script>

<style scoped>
.admin-bookings {
  padding: 20px;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.filter-item {
  width: 150px;
}
</style>
