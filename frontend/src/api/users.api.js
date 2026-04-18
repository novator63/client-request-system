import http, { normalizeListResponse } from './http'

export const getAvailableOperatorsApi = async () => {
  const { data } = await http.get('/users/operators')
  return normalizeListResponse(data)
}
