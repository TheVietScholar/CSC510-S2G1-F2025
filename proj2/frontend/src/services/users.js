import http from './http'

export const users = {
  register: (user) => http.post('/users/register', user),
  getAll: () => http.get('/users'),
  getById: (id) => http.get(`/users/${id}`),
}

export default users


