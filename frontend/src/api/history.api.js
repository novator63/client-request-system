import http, { normalizeListResponse } from './http'

export const getTicketHistoryApi = async (ticketId) => {
  const { data } = await http.get(`/history/tickets/${ticketId}`)

  return {
    ticketId: data?.ticketId ?? ticketId,
    entries: normalizeListResponse(data),
  }
}
