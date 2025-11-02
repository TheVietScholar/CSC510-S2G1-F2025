import http from './http'

export const merchants = {
  getAll: () => http.get('/merchants'),
  getById: (id) => http.get(`/merchants/${id}`),
}

export default merchants


