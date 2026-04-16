import http from './http'

const normalizeHistoryEntries = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  if (Array.isArray(payload?.entries)) {
    return payload.entries
  }

  if (Array.isArray(payload?.content)) {
    return payload.content
  }

  if (Array.isArray(payload?.items)) {
    return payload.items
  }

  return []
}

export const getTicketHistoryApi = async (ticketId) => {
  const { data } = await http.get(`/history/tickets/${ticketId}`)

  return {
    ticketId: data?.ticketId ?? ticketId,
    entries: normalizeHistoryEntries(data),
  }
}
