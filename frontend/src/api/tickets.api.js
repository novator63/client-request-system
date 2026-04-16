import http, { normalizeListResponse } from './http'

export const getTicketsApi = async (params = {}) => {
  const { data } = await http.get('/tickets', { params })
  return normalizeListResponse(data)
}

export const getTicketByIdApi = async (id) => {
  const { data } = await http.get(`/tickets/${id}`)
  return data
}

export const createTicketApi = async (payload) => {
  const { data } = await http.post('/tickets', payload)
  return data
}

export const getCategoriesForTicketApi = async () => {
  const { data } = await http.get('/categories')
  return normalizeListResponse(data)
}

export const updateTicketStatusApi = async (id, status) => {
  const { data } = await http.patch(`/tickets/${id}/status`, { status })
  return data
}

export const updateTicketClassificationApi = async (id, categoryId, priority) => {
  const { data } = await http.patch(`/tickets/${id}/classification`, {
    categoryId,
    priority
  })
  return data
}

export const assignTicketApi = async (id, assigneeId) => {
  const { data } = await http.patch(`/tickets/${id}/assign`, { assigneeId })
  return data
}

export const closeTicketApi = async (id) => {
  const { data } = await http.post(`/tickets/${id}/close`)
  return data
}

export const deleteTicketApi = async (id) => {
  const { data } = await http.delete(`/tickets/${id}`)
  return data
}
