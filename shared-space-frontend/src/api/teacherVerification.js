import request from "@/utils/request";

export function submitVerification(data) {
  return request({
    url: "/teacher/verification/submit",
    method: "post",
    data,
  });
}

export function getVerificationStatus() {
  return request({
    url: "/teacher/verification/status",
    method: "get",
  });
}

export function getTeacherFeedbackList() {
  return request({
    url: "/teacher/verification/feedback",
    method: "get",
  });
}

export function getPendingVerifications(pageNum, pageSize) {
  return request({
    url: "/teacher/verification/list",
    method: "get",
    params: {
      pageNum,
      pageSize,
    },
  });
}

export function reviewVerification(data) {
  return request({
    url: "/teacher/verification/review",
    method: "post",
    data,
  });
}

// 我的管理空间
export function getMySpace() {
  return request({
    url: "/teacher/my-space",
    method: "get",
  });
}

export function getTodayBookings() {
  return request({
    url: "/teacher/today-bookings",
    method: "get",
  });
}

export function getAvailableSeats() {
  return request({
    url: "/teacher/available-seats",
    method: "get",
  });
}
