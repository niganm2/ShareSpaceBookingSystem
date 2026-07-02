import request from "@/utils/request";

export function getSpaceList() {
  return request({
    url: "/admin/space/list",
    method: "get",
  });
}

export function addSpace(data) {
  return request({
    url: "/admin/space/add",
    method: "post",
    data,
  });
}

export function updateSpace(data) {
  return request({
    url: "/admin/space/update",
    method: "put",
    data,
  });
}

export function deleteSpace(id) {
  return request({
    url: `/admin/space/${id}`,
    method: "delete",
  });
}

export function disableSpace(data) {
  return request({
    url: "/admin/space/disable",
    method: "post",
    data,
  });
}

export function enableSpace(id) {
  return request({
    url: `/admin/space/enable/${id}`,
    method: "post",
  });
}

export function getSeats(spaceId) {
  return request({
    url: `/admin/space/seats/${spaceId}`,
    method: "get",
  });
}

export function addSeat(data) {
  return request({
    url: "/admin/space/seat/add",
    method: "post",
    data,
  });
}

export function batchAddSeats(data) {
  return request({
    url: "/admin/space/seat/batch-add",
    method: "post",
    data,
  });
}

export function disableSeat(id) {
  return request({
    url: `/admin/space/seat/disable/${id}`,
    method: "post",
  });
}

export function enableSeat(id) {
  return request({
    url: `/admin/space/seat/enable/${id}`,
    method: "post",
  });
}

export function updateSeat(data) {
  return request({
    url: "/admin/space/seat/update",
    method: "put",
    data,
  });
}

export function deleteSeat(id) {
  return request({
    url: `/admin/space/seat/${id}`,
    method: "delete",
  });
}
