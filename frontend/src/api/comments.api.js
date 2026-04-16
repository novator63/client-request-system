import http, { normalizeListResponse } from './http'

export const getCommentsByTicketIdApi = async (ticketId) => {
  const { data } = await http.get(`/comments/tickets/${ticketId}`)
  return normalizeListResponse(data)
}

export const createCommentByTicketIdApi = async (ticketId, payload) => {
  const { data } = await http.post(`/comments/tickets/${ticketId}`, payload)
  return data
}
