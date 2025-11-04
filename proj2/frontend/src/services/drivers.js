import http from './http'

export const drivers = {
  register: (driver) => http.post('/drivers/register', driver),
  updateCertification: (driverId, status) => http.put(`/drivers/${driverId}/certification?status=${encodeURIComponent(status)}`),
  updateAvailability: (driverId, available) => http.put(`/drivers/${driverId}/availability?available=${available}`),
  getAvailable: () => http.get('/drivers/available'),
  getById: (driverId) => http.get(`/drivers/${driverId}`),
  getAll: () => http.get('/drivers'),
}

export default drivers


