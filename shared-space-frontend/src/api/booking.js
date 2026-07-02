import request from "@/utils/request";

export function createBooking(data) {
  return request({
    url: "/booking/create",
    method: "post",
    data,
  });
}

export function cancelBooking(id, reason) {
  return request({
    url: `/booking/cancel/${id}`,
    method: "post",
    params: { reason },
  });
}

export function checkIn(id) {
  return request({
    url: `/booking/checkin/${id}`,
    method: "post",
  });
}

export function checkOut(id) {
  return request({
    url: `/booking/checkout/${id}`,
    method: "post",
  });
}

export function getBookingList(params) {
  return request({
    url: "/booking/list",
    method: "get",
    params,
  });
}

export function getBookingById(id) {
  return request({
    url: `/booking/${id}`,
    method: "get",
  });
}

export function getBookingsByDateAndSeat(date, seatId) {
  return request({
    url: `/booking/date/${date}/seat/${seatId}`,
    method: "get",
  });
}

export function getBookingsByDateAndSpace(date, spaceId) {
  return request({
    url: `/booking/date/${date}/space/${spaceId}`,
    method: "get",
  });
}

export function getBookingsByUserAndDate(date) {
  return request({
    url: `/booking/date/${date}`,
    method: "get",
  });
}

export function deleteBooking(id) {
  return request({
    url: `/booking/delete/${id}`,
    method: "delete",
  });
}

// 修改预约
export function updateBooking(id, data) {
  return request({
    url: `/booking/${id}`,
    method: "put",
    data,
  });
}

export function getRecent7DaysBookingCount() {
  return request({
    url: "/booking/recent7days",
    method: "get",
  });
}
