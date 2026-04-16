import http, { normalizeListResponse } from './http'

export const getReportSummaryApi = async () => {
  const { data } = await http.get('/reports/summary')
  return data
}

export const getReportByStatusApi = async () => {
  const { data } = await http.get('/reports/by-status')
  return normalizeListResponse(data)
}

export const getReportByCategoryApi = async () => {
  const { data } = await http.get('/reports/by-category')
  return normalizeListResponse(data)
}
