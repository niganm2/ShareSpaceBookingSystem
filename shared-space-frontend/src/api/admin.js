import request from "@/utils/request";

export function getAllBookings(params) {
  return request({
    url: "/booking/all",
    method: "get",
    params,
  });
}

export function getAllRecent7Days() {
  return request({
    url: "/booking/all/recent7days",
    method: "get",
  });
}

export function getSeatsBySpaceName(spaceName) {
  return request({
    url: "/booking/seats-by-space",
    method: "get",
    params: { spaceName },
  });
}
