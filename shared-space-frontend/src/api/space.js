import request from "@/utils/request";

export function getSpaceList() {
  return request({
    url: "/space/list",
    method: "get",
  });
}

export function getSpaceById(id) {
  return request({
    url: `/space/${id}`,
    method: "get",
  });
}

export function getSeatListBySpaceId(spaceId) {
  return request({
    url: `/seat/list/${spaceId}`,
    method: "get",
  });
}
