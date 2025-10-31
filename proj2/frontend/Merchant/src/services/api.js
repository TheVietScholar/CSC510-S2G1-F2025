import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const productAPI = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  create: (product) => api.post('/products', product),
  update: (id, product) => api.put(`/products/${id}`, product),
  delete: (id) => api.delete(`/products/${id}`),
  search: (keyword) => api.get(`/products/search?keyword=${keyword}`),
  checkAvailability: (id) => api.get(`/products/${id}/available`),
  getByMerchant: (merchantId) => api.get(`/products/merchant/${merchantId}`),
  getAvailable: () => api.get('/products/available'),
}

export const userAPI = {
  register: (user) => api.post('/users/register', user),
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
}

export const orderAPI = {
  create: (order) => api.post('/orders', order),
  getByUser: (userId) => api.get(`/orders/user/${userId}`),
  getAll: () => api.get('/orders'),
  cancel: (orderId) => api.post(`/orders/${orderId}/cancel`),
}

export default api