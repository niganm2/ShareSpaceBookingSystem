import request from "@/utils/request";

// 获取老师绑定的自习室
export function getMyManagedSpace() {
  return request({
    url: "/teacher/my-space",
    method: "get",
  });
}

// 获取今日预约
export function getTodayBookings() {
  return request({
    url: "/teacher/today-bookings",
    method: "get",
  });
}

// 获取剩余座位
export function getAvailableSeats() {
  return request({
    url: "/teacher/available-seats",
    method: "get",
  });
}

export function getRecent7DaysBookingCount() {
  return request({
    url: "/api/teacher-space/recent7days",
    method: "get",
  });
}

export function markViolation(data) {
  return request({
    url: "/teacher/mark-violation",
    method: "post",
    data,
  });
}

export function checkViolationTime(bookingId) {
  return request({
    url: `/teacher/check-violation-time/${bookingId}`,
    method: "get",
  });
}
