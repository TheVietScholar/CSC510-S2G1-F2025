import http from './http'

export const orders = {
  create: (order) => http.post('/orders', order),
  getById: (orderId) => http.get(`/orders/${orderId}`),
  getByUser: (userId) => http.get(`/orders/user/${userId}`),
  getAll: () => http.get('/orders'),
  cancel: (orderId) => http.post(`/orders/${orderId}/cancel`),
  getAvailableForDriver: (latitude, longitude, radiusKm) => http.get('/orders/by-distance', {
    params: { latitude, longitude, radiusKm }
  }),
}

export default orders


