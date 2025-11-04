import http from './http'

export const products = {
  getAll: () => http.get('/products'),
  getById: (id) => http.get(`/products/${id}`),
  create: (product) => http.post('/products', product),
  update: (id, product) => http.put(`/products/${id}`, product),
  delete: (id) => http.delete(`/products/${id}`),
  search: (keyword) => http.get(`/products/search?keyword=${encodeURIComponent(keyword)}`),
  checkAvailability: (id) => http.get(`/products/${id}/available`),
  getByMerchant: (merchantId) => http.get(`/products/merchant/${merchantId}`),
  getAvailable: () => http.get('/products/available'),
}

export default products


