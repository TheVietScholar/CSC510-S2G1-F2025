import axios from 'axios'

// Prefer relative '/api' in dev to use Vite proxy and avoid CORS
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')

const http = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export default http