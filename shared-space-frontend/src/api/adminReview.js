import request from "@/utils/request";

export function getPendingVerifications(params) {
  return request({
    url: "/teacher-verification/pending-list",
    method: "get",
    params,
  });
}

export function reviewVerification(data) {
  return request({
    url: "/teacher-verification/review",
    method: "post",
    data,
  });
}
