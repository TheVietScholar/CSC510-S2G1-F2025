import http from './http'

export const deliveries = {
  assign: ({ orderId, driverId }) => http.post(`/deliveries/assign?orderId=${orderId}&driverId=${driverId}`),
  updateStatus: (deliveryId, status) => http.put(`/deliveries/${deliveryId}/status?status=${encodeURIComponent(status)}`),
  cancel: (deliveryId, reason) => http.post(`/deliveries/${deliveryId}/cancel?reason=${encodeURIComponent(reason)}`),
  getByDriver: (driverId) => http.get(`/deliveries/driver/${driverId}`),
  getById: (deliveryId) => http.get(`/deliveries/${deliveryId}`),
  getByOrderId: (orderId) => http.get(`/deliveries/order/${orderId}`),
  getActive: () => http.get('/deliveries/active'),
}

export default deliveries


