import request from "@/utils/request";

export function getViolationList() {
  return request({
    url: "/violation/list",
    method: "get",
  });
}

export function getMyViolationCount() {
  return request({
    url: "/violation/my-count",
    method: "get",
  });
}

export function getTeacherViolationCount() {
  return request({
    url: "/violation/teacher-count",
    method: "get",
  });
}

export function getAdminViolationCount() {
  return request({
    url: "/violation/admin-count",
    method: "get",
  });
}

export function getMyScore() {
  return request({
    url: "/violation/my-score",
    method: "get",
  });
}

export function getAllViolationDetails() {
  return request({
    url: "/violation/admin/all-details",
    method: "get",
  });
}
