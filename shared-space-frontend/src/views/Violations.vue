<template>
  <div class="violations">
    <el-card>
      <div slot="header">
        <span>违规记录</span>
      </div>
      <el-table :data="violationList" v-loading="loading" style="width: 100%">
        <el-table-column prop="type" label="违规类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getViolationType(scope.row.type)">
              {{ getViolationText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="违规描述"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column prop="points" label="扣分" width="100">
          <template slot-scope="scope">
            <span style="color: #f56c6c; font-weight: bold"
              >-{{ scope.row.points }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="违规时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="剩余分数" width="120">
          <template slot-scope="scope">
            <span
              :style="{
                color: scope.row.remainingScore < 80 ? '#f56c6c' : '#67c23a',
                fontWeight: 'bold',
              }"
            >
              {{ scope.row.remainingScore }} 分
            </span>
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
/* eslint-disable vue/multi-word-component-names */
import { getViolationList } from "@/api/violation";
import moment from "moment";

export default {
  name: "Violations",
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
      getViolationList()
        .then((res) => {
          let list = res.data || [];
          list.sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
          let score = 100;
          list.forEach((item) => {
            score = Math.max(0, score - (item.points || 1));
            item.remainingScore = score;
          });
          list.reverse();
          this.violationList = list;
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
      };
      return map[type] || "info";
    },
    getViolationText(type) {
      const map = {
        NO_SHOW: "未签到",
        LATE: "迟到",
        EARLY_LEAVE: "早退",
      };
      return map[type] || type;
    },
    formatDate(date) {
      return moment(date).format("YYYY-MM-DD HH:mm:ss");
    },
  },
};
</script>

<style scoped>
.violations {
  padding: 20px;
}
</style>
