import request from './request'

export function getInsurancePolicies(params = {}) {
  return request.get('/insurance/list', { params })
}

export function getInsuranceDetail(id) {
  return request.get(`/insurance/${id}`)
}

export function getInsuranceByOrder(orderId) {
  return request.get(`/insurance/order/${orderId}`)
}
