import request from './request'

export function getStudentReports(studentId, params = {}) {
  return request.get('/report/list', { params: { studentId, ...params } })
}

export function getReportDetail(id) {
  return request.get(`/report/${id}`)
}
