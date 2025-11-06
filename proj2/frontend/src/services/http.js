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
      // Validate token format (JWT tokens typically start with "eyJ" which is base64 for "{")
      if (!token.startsWith('eyJ')) {
        console.warn(`[HTTP] Token doesn't appear to be a valid JWT format. Token starts with: ${token.substring(0, 10)}`)
      }
      
      // Set Authorization header - ensure it's set correctly
      const authHeader = `Bearer ${token.trim()}`
      // Ensure headers object exists
      if (!config.headers) {
        config.headers = {}
      }
      config.headers.Authorization = authHeader
      
      // Debug: Log token presence and header (remove in production)
      console.log(`[HTTP] Adding Authorization header for ${config.method?.toUpperCase()} ${config.url}`)
      console.log(`[HTTP] Token preview: ${token.substring(0, 20)}...`)
      console.log(`[HTTP] Authorization header value:`, authHeader.substring(0, 30) + '...')
    } else {
      console.warn(`[HTTP] No token found for ${config.method?.toUpperCase()} ${config.url}`)
    }
    // Log all headers being sent (for debugging)
    console.log(`[HTTP] Request headers:`, Object.keys(config.headers).reduce((acc, key) => {
      acc[key] = typeof config.headers[key] === 'string' && config.headers[key].length > 50 
        ? config.headers[key].substring(0, 50) + '...' 
        : config.headers[key]
      return acc
    }, {}))
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