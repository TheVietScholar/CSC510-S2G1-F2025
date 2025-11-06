import http from './http'

const categories = {
  getAll: () => http.get('/categories'),
  
  getById: (id) => http.get(`/categories/${id}`),
  
  create: (data) => http.post('/categories', data),
  
  update: (id, data) => http.put(`/categories/${id}`, data),
  
  delete: (id) => http.delete(`/categories/${id}`)
}

export default categories