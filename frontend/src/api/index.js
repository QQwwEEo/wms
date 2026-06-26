import request from '@/utils/request'

// 认证相关
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  getInfo: () => request.get('/auth/info')
}

// 用户管理
export const userApi = {
  page: (params) => request.get('/user/page', { params }),
  add: (data) => request.post('/user', data),
  update: (data) => request.put('/user', data),
  delete: (id) => request.delete(`/user/${id}`),
  updateStatus: (id, status) => request.put(`/user/status/${id}`, { status }),
  updateInfo: (data) => request.put('/user/updateInfo', data)
}

// 供应商管理
export const supplierApi = {
  page: (params) => request.get('/supplier/page', { params }),
  list: () => request.get('/supplier/list'),
  add: (data) => request.post('/supplier', data),
  update: (data) => request.put('/supplier', data),
  delete: (id) => request.delete(`/supplier/${id}`)
}

// 仓库管理
export const warehouseApi = {
  page: (params) => request.get('/warehouse/page', { params }),
  list: () => request.get('/warehouse/list'),
  add: (data) => request.post('/warehouse', data),
  update: (data) => request.put('/warehouse', data),
  delete: (id) => request.delete(`/warehouse/${id}`)
}

// 物资类型
export const materialTypeApi = {
  page: (params) => request.get('/materialType/page', { params }),
  list: () => request.get('/materialType/list'),
  add: (data) => request.post('/materialType', data),
  update: (data) => request.put('/materialType', data),
  delete: (id) => request.delete(`/materialType/${id}`)
}

// 物资管理
export const materialApi = {
  page: (params) => request.get('/material/page', { params }),
  list: () => request.get('/material/list'),
  add: (data) => request.post('/material', data),
  update: (data) => request.put('/material', data),
  delete: (id) => request.delete(`/material/${id}`),
  importExcel: (formData) => request.post('/material/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 入库管理
export const inboundApi = {
  page: (params) => request.get('/inbound/page', { params }),
  add: (data) => request.post('/inbound', data),
  delete: (id) => request.delete(`/inbound/${id}`)
}

// 出库管理
export const outboundApi = {
  page: (params) => request.get('/outbound/page', { params }),
  add: (data) => request.post('/outbound', data),
  delete: (id) => request.delete(`/outbound/${id}`)
}

// 物资申请
export const applyApi = {
  page: (params) => request.get('/apply/page', { params }),
  add: (data) => request.post('/apply', data),
  approve: (id, data) => request.put(`/apply/approve/${id}`, data),
  delete: (id) => request.delete(`/apply/${id}`)
}

// 物资归还
export const returnApi = {
  page: (params) => request.get('/returnRecord/page', { params }),
  add: (data) => request.post('/returnRecord', data),
  approve: (id, data) => request.put(`/returnRecord/approve/${id}`, data),
  delete: (id) => request.delete(`/returnRecord/${id}`)
}

// 统计
export const dashboardApi = {
  stats: () => request.get('/dashboard/stats')
}
