import http from './http'

const normalizeListResponse = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  if (Array.isArray(payload?.content)) {
    return payload.content
  }

  if (Array.isArray(payload?.items)) {
    return payload.items
  }

  return []
}

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
