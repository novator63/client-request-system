import http from './http'

export const loginApi = async (payload) => {
  const { data } = await http.post('/auth/login', payload)
  return data
}

export const getCurrentUserApi = async () => {
  const { data } = await http.get('/auth/me')
  return data
}