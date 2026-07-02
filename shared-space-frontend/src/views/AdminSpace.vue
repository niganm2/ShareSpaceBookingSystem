<template>
  <div class="admin-space">
    <el-card>
      <div slot="header">
        <span>空间管理</span>
      </div>

      <el-button
        type="primary"
        icon="el-icon-plus"
        @click="handleAddSpace"
        style="margin-bottom: 15px"
      >
        新增空间
      </el-button>

      <el-table :data="spaceList" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="空间名称" width="150" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column label="开放时间" width="150">
          <template slot-scope="scope">
            {{ scope.row.openTime }} - {{ scope.row.closeTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              size="medium"
            >
              {{ scope.row.status === 1 ? "正常" : "已禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="禁用信息" min-width="200">
          <template slot-scope="scope">
            <span v-if="scope.row.disabledFrom">
              {{ formatDateTime(scope.row.disabledFrom) }} 至
              {{ formatDateTime(scope.row.disabledUntil) }} <br /><span
                style="color: #909399"
                >原因: {{ scope.row.disabledReason }}</span
              >
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleEditSpace(scope.row)"
              >编辑</el-button
            >
            <el-button
              type="text"
              size="small"
              @click="handleManageSeats(scope.row)"
              >管理座位</el-button
            >
            <el-button
              v-if="scope.row.status === 1"
              type="text"
              size="small"
              style="color: #e6a23c"
              @click="handleDisableSpace(scope.row)"
              >禁用</el-button
            >
            <el-button
              v-else
              type="text"
              size="small"
              style="color: #67c23a"
              @click="handleEnableSpace(scope.row)"
              >启用</el-button
            >
            <el-button
              type="text"
              size="small"
              style="color: #f56c6c"
              @click="handleDeleteSpace(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      :title="spaceDialogTitle"
      :visible.sync="spaceDialogVisible"
      width="500px"
    >
      <el-form
        :model="spaceForm"
        :rules="spaceRules"
        ref="spaceForm"
        label-width="100px"
      >
        <el-form-item label="空间名称" prop="name">
          <el-input v-model="spaceForm.name" placeholder="请输入空间名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="spaceForm.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="spaceForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-time-picker
            v-model="spaceForm.openTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开始时间"
          />
          <span style="margin: 0 10px">至</span>
          <el-time-picker
            v-model="spaceForm.closeTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="spaceForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="spaceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSpaceForm">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="禁用空间"
      :visible.sync="disableDialogVisible"
      width="500px"
    >
      <el-form
        :model="disableForm"
        :rules="disableRules"
        ref="disableForm"
        label-width="100px"
      >
        <el-form-item label="禁用开始" prop="disabledFrom">
          <el-date-picker
            v-model="disableForm.disabledFrom"
            type="datetime"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="禁用结束" prop="disabledUntil">
          <el-date-picker
            v-model="disableForm.disabledUntil"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="禁用原因" prop="disabledReason">
          <el-input
            v-model="disableForm.disabledReason"
            type="textarea"
            :rows="2"
            placeholder="请输入禁用原因"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="disableDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitDisableForm"
          >确定禁用</el-button
        >
      </span>
    </el-dialog>

    <el-dialog title="座位管理" :visible.sync="seatDialogVisible" width="800px">
      <el-button
        type="primary"
        icon="el-icon-plus"
        size="small"
        @click="handleAddSeat"
        style="margin-bottom: 10px"
      >
        新增座位
      </el-button>
      <el-button
        type="success"
        icon="el-icon-plus"
        size="small"
        @click="handleBatchAddSeat"
        style="margin-bottom: 10px"
      >
        批量新增
      </el-button>
      <el-table :data="seatList" v-loading="seatLoading" style="width: 100%">
        <el-table-column prop="seatNumber" label="座位号" width="120" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.status === 1 ? "正常" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleEditSeat(scope.row)"
              >编辑</el-button
            >
            <el-button
              v-if="scope.row.status === 1"
              type="text"
              size="small"
              style="color: #e6a23c"
              @click="handleDisableSeat(scope.row)"
              >禁用</el-button
            >
            <el-button
              v-else
              type="text"
              size="small"
              style="color: #67c23a"
              @click="handleEnableSeat(scope.row)"
              >启用</el-button
            >
            <el-button
              type="text"
              size="small"
              style="color: #f56c6c"
              @click="handleDeleteSeat(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      :title="seatDialogTitle"
      :visible.sync="seatFormDialogVisible"
      width="400px"
    >
      <el-form
        :model="seatForm"
        :rules="seatRules"
        ref="seatForm"
        label-width="80px"
      >
        <el-form-item label="座位号" prop="seatNumber">
          <el-input v-model="seatForm.seatNumber" placeholder="请输入座位号" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="seatForm.type" placeholder="请选择类型">
            <el-option label="普通座位" value="普通座位" />
            <el-option label="靠窗座位" value="靠窗座位" />
            <el-option label="角落座位" value="角落座位" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="seatForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="seatFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSeatForm">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="批量新增座位"
      :visible.sync="batchAddDialogVisible"
      width="450px"
    >
      <el-form
        :model="batchAddForm"
        :rules="batchAddRules"
        ref="batchAddForm"
        label-width="100px"
      >
        <el-form-item label="座位数量" prop="count">
          <el-input-number v-model="batchAddForm.count" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="座位号前缀">
          <el-input
            v-model="batchAddForm.prefix"
            placeholder="如 A，座位号将为 A001, A002..."
          />
        </el-form-item>
        <el-form-item label="座位类型">
          <el-select v-model="batchAddForm.type" placeholder="请选择类型">
            <el-option label="普通座位" value="普通座位" />
            <el-option label="靠窗座位" value="靠窗座位" />
            <el-option label="角落座位" value="角落座位" />
          </el-select>
        </el-form-item>
      </el-form>
      <div style="padding-left: 20px; color: #909399; font-size: 13px">
        预览：{{ batchAddForm.prefix || "" }}001,
        {{ batchAddForm.prefix || "" }}002,
        {{ batchAddForm.prefix || "" }}003...
      </div>
      <span slot="footer">
        <el-button @click="batchAddDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchAddForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSpaceList,
  addSpace,
  updateSpace,
  deleteSpace,
  disableSpace,
  enableSpace,
  getSeats,
  addSeat,
  updateSeat,
  deleteSeat,
  batchAddSeats,
  disableSeat,
  enableSeat,
} from "@/api/adminSpace";

export default {
  name: "AdminSpace",
  data() {
    return {
      spaceList: [],
      loading: false,
      spaceDialogVisible: false,
      spaceDialogTitle: "",
      spaceForm: {
        id: null,
        name: "",
        location: "",
        capacity: 1,
        openTime: "08:00",
        closeTime: "22:00",
        description: "",
      },
      spaceRules: {
        name: [{ required: true, message: "请输入空间名称", trigger: "blur" }],
        location: [{ required: true, message: "请输入位置", trigger: "blur" }],
      },
      disableDialogVisible: false,
      disableForm: {
        id: null,
        disabledFrom: "",
        disabledUntil: "",
        disabledReason: "",
      },
      disableRules: {
        disabledFrom: [
          { required: true, message: "请选择开始时间", trigger: "change" },
        ],
        disabledUntil: [
          { required: true, message: "请选择结束时间", trigger: "change" },
        ],
        disabledReason: [
          { required: true, message: "请输入禁用原因", trigger: "blur" },
        ],
      },
      seatDialogVisible: false,
      seatList: [],
      seatLoading: false,
      currentSpaceId: null,
      seatFormDialogVisible: false,
      seatDialogTitle: "",
      seatForm: {
        id: null,
        spaceId: null,
        seatNumber: "",
        type: "普通座位",
        status: 1,
      },
      seatRules: {
        seatNumber: [
          { required: true, message: "请输入座位号", trigger: "blur" },
        ],
      },
      batchAddDialogVisible: false,
      batchAddForm: {
        spaceId: null,
        count: 10,
        prefix: "",
        type: "普通座位",
      },
      batchAddRules: {
        count: [{ required: true, message: "请输入座位数量", trigger: "blur" }],
      },
    };
  },
  mounted() {
    this.loadSpaceList();
  },
  methods: {
    async loadSpaceList() {
      this.loading = true;
      try {
        const res = await getSpaceList();
        if (res.code === 200) {
          this.spaceList = res.data || [];
        }
      } finally {
        this.loading = false;
      }
    },
    handleAddSpace() {
      this.spaceDialogTitle = "新增空间";
      this.spaceForm = {
        id: null,
        name: "",
        location: "",
        capacity: 1,
        openTime: "08:00",
        closeTime: "22:00",
        description: "",
      };
      this.spaceDialogVisible = true;
    },
    handleEditSpace(row) {
      this.spaceDialogTitle = "编辑空间";
      this.spaceForm = { ...row };
      this.spaceDialogVisible = true;
    },
    submitSpaceForm() {
      this.$refs.spaceForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.spaceForm.id) {
              await updateSpace(this.spaceForm);
              this.$message.success("更新成功");
            } else {
              await addSpace(this.spaceForm);
              this.$message.success("添加成功");
            }
            this.spaceDialogVisible = false;
            this.loadSpaceList();
          } catch (err) {
            this.$message.error("操作失败");
          }
        }
      });
    },
    handleDeleteSpace(row) {
      this.$confirm("删除空间将同时删除该空间下所有座位，确定删除？", "提示", {
        type: "warning",
      })
        .then(async () => {
          await deleteSpace(row.id);
          this.$message.success("删除成功");
          this.loadSpaceList();
        })
        .catch(() => {});
    },
    handleDisableSpace(row) {
      this.disableForm = {
        id: row.id,
        disabledFrom: "",
        disabledUntil: "",
        disabledReason: "",
      };
      this.disableDialogVisible = true;
    },
    submitDisableForm() {
      this.$refs.disableForm.validate(async (valid) => {
        if (valid) {
          await disableSpace(this.disableForm);
          this.$message.success("禁用成功");
          this.disableDialogVisible = false;
          this.loadSpaceList();
        }
      });
    },
    async handleEnableSpace(row) {
      await enableSpace(row.id);
      this.$message.success("启用成功");
      this.loadSpaceList();
    },
    async handleManageSeats(row) {
      this.currentSpaceId = row.id;
      this.seatDialogVisible = true;
      this.loadSeats();
    },
    async loadSeats() {
      this.seatLoading = true;
      try {
        const res = await getSeats(this.currentSpaceId);
        if (res.code === 200) {
          this.seatList = res.data || [];
        }
      } finally {
        this.seatLoading = false;
      }
    },
    handleAddSeat() {
      this.seatDialogTitle = "新增座位";
      this.seatForm = {
        id: null,
        spaceId: this.currentSpaceId,
        seatNumber: "",
        type: "普通座位",
        status: 1,
      };
      this.seatFormDialogVisible = true;
    },
    handleEditSeat(row) {
      this.seatDialogTitle = "编辑座位";
      this.seatForm = { ...row };
      this.seatFormDialogVisible = true;
    },
    submitSeatForm() {
      this.$refs.seatForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.seatForm.id) {
              await updateSeat(this.seatForm);
              this.$message.success("更新成功");
            } else {
              await addSeat(this.seatForm);
              this.$message.success("添加成功");
            }
            this.seatFormDialogVisible = false;
            this.loadSeats();
          } catch (err) {
            this.$message.error("操作失败");
          }
        }
      });
    },
    handleDeleteSeat(row) {
      this.$confirm("确定删除该座位？", "提示", { type: "warning" })
        .then(async () => {
          await deleteSeat(row.id);
          this.$message.success("删除成功");
          this.loadSeats();
        })
        .catch(() => {});
    },
    handleBatchAddSeat() {
      this.batchAddForm = {
        spaceId: this.currentSpaceId,
        count: 10,
        prefix: "",
        type: "普通座位",
      };
      this.batchAddDialogVisible = true;
    },
    submitBatchAddForm() {
      this.$refs.batchAddForm.validate(async (valid) => {
        if (valid) {
          try {
            await batchAddSeats(this.batchAddForm);
            this.$message.success("批量添加成功");
            this.batchAddDialogVisible = false;
            this.loadSeats();
          } catch (err) {
            this.$message.error("批量添加失败");
          }
        }
      });
    },
    async handleDisableSeat(row) {
      await disableSeat(row.id);
      this.$message.success("禁用成功");
      this.loadSeats();
    },
    async handleEnableSeat(row) {
      await enableSeat(row.id);
      this.$message.success("启用成功");
      this.loadSeats();
    },
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      return dateTime.replace("T", " ");
    },
  },
};
</script>

<style scoped>
.admin-space {
  padding: 20px;
}
</style>
