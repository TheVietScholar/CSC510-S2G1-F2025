import http from './http'

export const auth = {
  register: (userData) => http.post('/auth/register', userData),
  login: (credentials) => http.post('/auth/login', credentials),
  refresh: (refreshToken) => http.post('/auth/refresh', { refreshToken }),
  logout: (userId) => http.post(`/auth/logout/${userId}`),
}

export default auth

