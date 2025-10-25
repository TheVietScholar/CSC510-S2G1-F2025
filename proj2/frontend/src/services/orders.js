import http from './http'

export const orders = {
  create: (order) => http.post('/orders', order),
  getByUser: (userId) => http.get(`/orders/user/${userId}`),
  getAll: () => http.get('/orders'),
  cancel: (orderId) => http.post(`/orders/${orderId}/cancel`),
}

export default orders


