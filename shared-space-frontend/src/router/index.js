import Vue from "vue";
import VueRouter from "vue-router";
import Login from "../views/Login.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/",
    redirect: "/dashboard",
    component: () => import("../layout/Layout.vue"),
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("../views/Dashboard.vue"),
        meta: { title: "首页" },
      },
      {
        path: "booking",
        name: "Booking",
        component: () => import("../views/Booking.vue"),
        meta: { title: "座位预约" },
      },
      {
        path: "my-bookings",
        name: "MyBookings",
        component: () => import("../views/MyBookings.vue"),
        meta: { title: "我的预约" },
      },
      {
        path: "violations",
        name: "Violations",
        component: () => import("../views/Violations.vue"),
        meta: { title: "违规记录" },
      },
      {
        path: "teacher-verification",
        name: "TeacherVerification",
        component: () => import("../views/TeacherVerification.vue"),
        meta: { title: "教师资格验证" },
      },
      {
        path: "admin-review",
        name: "AdminReview",
        component: () => import("../views/AdminReview.vue"),
        meta: { title: "教师验证审核" },
      },
      {
        path: "admin-bookings",
        name: "AdminBookings",
        component: () => import("../views/AdminBookings.vue"),
        meta: { title: "预约记录管理" },
      },
      {
        path: "admin-space",
        name: "AdminSpace",
        component: () => import("../views/AdminSpace.vue"),
        meta: { title: "空间管理" },
      },
      {
        path: "admin-violations",
        name: "AdminViolations",
        component: () => import("../views/AdminViolations.vue"),
        meta: { title: "违规记录管理" },
      },
      {
        path: "teacher-feedback",
        name: "TeacherFeedback",
        component: () => import("../views/TeacherFeedback.vue"),
        meta: { title: "认证反馈" },
      },
      {
        path: "teacher-spaces",
        name: "TeacherSpaces",
        component: () => import("../views/TeacherSpaces.vue"),
        meta: { title: "共享空间管理" },
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("../views/Profile.vue"),
        meta: { title: "个人信息" },
      },
    ],
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

// ===================== 修复路由重复跳转报错 =====================
const originalPush = router.push;
router.push = function push(location) {
  return originalPush.call(this, location).catch((err) => err);
};

// ===================== 路由守卫（已修复，不报错） =====================
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem("user");
  const token = localStorage.getItem("token");

  // 没登录 → 去登录页
  if (!token || !userStr) {
    if (to.path === "/login") {
      next();
    } else {
      next("/login");
    }
    return;
  }

  // 已登录 → 解析用户信息
  const user = JSON.parse(userStr);

  // 老师未认证 → 只能去认证页
  if (user.role === "TEACHER" && user.isVerified !== 1) {
    if (to.path !== "/teacher-verification" && to.path !== "/login" && to.path !== "/dashboard" && to.path !== "/profile") {
      next("/teacher-verification");
      return;
    }
  }

  // 角色隔离
  if (user.role === "TEACHER" && to.path === "/booking") {
    next("/teacher-spaces");
    return;
  }
  if (user.role === "STUDENT" && to.path === "/teacher-spaces") {
    next("/booking");
    return;
  }

  // 正常放行
  next();
});

export default router;