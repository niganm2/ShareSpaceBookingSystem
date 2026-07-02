import request from "@/utils/request";

export function getSeatListBySpaceId(spaceId) {
  return request({
    url: `/seat/list/${spaceId}`,
    method: "get",
  });
}

export function getSeatListBySpaceIdWithStatus(spaceId) {
  return request({
    url: `/seat/list/${spaceId}/with-status`,
    method: "get",
  });
}

export function getAvailableSeats(spaceId) {
  return request({
    url: `/seat/available/${spaceId}`,
    method: "get",
  });
}

export function getSeatById(id) {
  return request({
    url: `/seat/${id}`,
    method: "get",
  });
}
