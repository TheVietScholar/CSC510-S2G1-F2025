import axios from 'axios'

// Prefer relative '/api' in dev to use Vite proxy and avoid CORS
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')

const http = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add request interceptor to include JWT token in Authorization header
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('bb_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      // Debug: Log token presence (remove in production)
      console.log(`[HTTP] Adding Authorization header for ${config.method?.toUpperCase()} ${config.url}`)
    } else {
      console.warn(`[HTTP] No token found for ${config.method?.toUpperCase()} ${config.url}`)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add response interceptor to handle token expiration
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - clear token and redirect to login
      localStorage.removeItem('bb_token')
      localStorage.removeItem('bb_refresh_token')
      // Optionally redirect to login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default http