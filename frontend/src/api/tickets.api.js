import http from './http'

const normalizeTicketsResponse = (payload) => {
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

export const getTicketsApi = async (params = {}) => {
  const { data } = await http.get('/tickets', { params })
  return normalizeTicketsResponse(data)
}

export const getTicketByIdApi = async (id) => {
  const { data } = await http.get(`/tickets/${id}`)
  return data
}
