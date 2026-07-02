import request from "@/utils/request";

export function updatePassword(data) {
  return request({
    url: "/user/update-password",
    method: "post",
    data,
  });
}
