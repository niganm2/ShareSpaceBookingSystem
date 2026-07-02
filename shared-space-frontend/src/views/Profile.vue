<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <div slot="header">
        <span>个人信息</span>
      </div>
      <el-form :model="user" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="user.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="user.realName" disabled />
        </el-form-item>

        <el-form-item v-if="user.role === 'STUDENT'" label="学号">
          <el-input v-model="user.studentId" disabled />
        </el-form-item>
        <el-form-item v-if="user.role === 'TEACHER'" label="教师工号">
          <el-input v-model="user.teacherJobNo" disabled />
        </el-form-item>
        <el-form-item v-if="user.role === 'ADMIN'" label="管理员工号">
          <el-input v-model="user.adminJobNo" disabled />
        </el-form-item>

        <el-form-item label="手机">
          <el-input v-model="user.phone" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="user.email" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="roleName" disabled />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
/* eslint-disable vue/multi-word-component-names */
export default {
  name: "Profile",
  data() {
    return {
      user: {},
      roleName: "",
    };
  },
  created() {
    const u = localStorage.getItem("user");
    if (u) {
      this.user = JSON.parse(u);
      if (this.user.role === "STUDENT") {
        this.roleName = "学生";
      } else if (this.user.role === "TEACHER") {
        this.roleName = this.user.isVerified === 1 ? "教师（已认证）" : "教师（未认证）";
      } else if (this.user.role === "ADMIN") {
        this.roleName = "管理员";
      } else {
        this.roleName = "未知";
      }
    }
  },
};
</script>

<style scoped>
.profile-container {
  width: 100%;
  max-width: none;
  margin: 0 auto;
}
.profile-card {
  width: 100%;
  box-sizing: border-box;
}
</style>