import http from './http'

const normalizeCommentsResponse = (payload) => {
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

export const getCommentsByTicketIdApi = async (ticketId) => {
  const { data } = await http.get(`/comments/tickets/${ticketId}`)
  return normalizeCommentsResponse(data)
}

export const createCommentByTicketIdApi = async (ticketId, payload) => {
  const { data } = await http.post(`/comments/tickets/${ticketId}`, payload)
  return data
}
