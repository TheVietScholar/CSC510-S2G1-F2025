import http from './http'

export const users = {
  register: (user) => http.post('/users/register', user),
  getAll: () => http.get('/users'),
  getById: (id) => http.get(`/users/${id}`),
  getMe: () => http.get('/users/me'),
  verifyAge: (userId) => http.post(`/users/${userId}/verify-age`),
}

export default users


