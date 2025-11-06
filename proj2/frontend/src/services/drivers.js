import http from './http'

export const drivers = {
  register: (driver) => http.post('/drivers/register', driver),
  updateCertification: (driverId, status) => http.put(`/drivers/${driverId}/certification?status=${encodeURIComponent(status)}`),
  updateAvailability: (available) => http.put(`/drivers/my-profile/availability?available=${available}`),
  updateLocation: (latitude, longitude) => http.put(`/drivers/my-profile/location?latitude=${latitude}&longitude=${longitude}`),
  getAvailable: () => http.get('/drivers/available'),
  getById: (driverId) => http.get(`/drivers/${driverId}`),
  getAll: () => http.get('/drivers'),
  getMyProfile: (userId) => http.get(`/drivers/my-profile?id=${userId}`),
}

export default drivers


