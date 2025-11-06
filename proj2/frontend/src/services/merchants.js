// services/merchants.js
import http from './http'

export const merchants = {
  getAll: () => http.get('/merchants'),
  getById: (id) => http.get(`/merchants/${id}`),
  // Token is automatically added by axios interceptor in http.js
  getByDistance: () => http.get('/merchants/by-distance'),
  register: (merchant) => http.post('/merchants/register', merchant),
  verify: (id, verified) => http.put(`/merchants/${id}/verify?verified=${verified}`),
  delete: (id) => http.delete(`/merchants/${id}`),
  getOrders: (id, page = 0, size = 10) => http.get(`/merchants/${id}/orders?page=${page}&size=${size}`),
}

export default merchants