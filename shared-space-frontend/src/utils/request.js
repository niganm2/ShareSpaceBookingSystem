import axios from "axios";
import { Message } from "element-ui";
import router from "../router";

const request = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

// 请求拦截器：登录页不发非登录请求
request.interceptors.request.use(
  (config) => {
    const isLoginPage = router.currentRoute.path === "/login";
    const isLoginApi = config.url.includes("/login");
    const isRegisterApi = config.url.includes("/register");

    // 在登录页，只允许调用登录和注册接口，其他全部拦截
    if (isLoginPage && !isLoginApi && !isRegisterApi) {
      return Promise.reject("登录页禁止请求非登录接口");
    }

    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 被我们自己拦截的请求，不报错
    if (error === "登录页禁止请求非登录接口") {
      return Promise.reject(error);
    }

    if (error.response?.status === 401) {
      if (router.currentRoute.path !== "/login") {
        Message.error("登录已过期，请重新登录");
        localStorage.clear();
        router.push("/login");
      }
      return Promise.reject(error);
    }

    const msg = error.response?.data?.message || "服务器异常";
    Message.error(msg);
    return Promise.reject(error);
  },
);

export default request;
