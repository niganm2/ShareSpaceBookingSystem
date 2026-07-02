import request from "@/utils/request";

// 登录接口（对应后端 /user/login）
export function login(loginForm) {
  return request({
    url: "/user/login", // 后端登录接口路径
    method: "post",
    data: loginForm,
  });
}

// 注册接口（对应后端 /user/register）
export function register(registerForm) {
  return request({
    url: "/user/register", // 后端注册接口路径
    method: "post",
    data: registerForm,
  });
}
