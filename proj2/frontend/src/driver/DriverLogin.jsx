import React, { useState } from 'react'
import auth from '../services/auth'

const DriverLogin = ({ onDriverLogin }) => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    e.stopPropagation()
    setLoading(true)
  
    try {
      const response = await auth.driverlogin({email, password})
      const responseData = response?.data || response
  
      if (responseData) {       
        // Store tokens in localStorage
        if (responseData.token) {
          localStorage.setItem('bb_token', responseData.token)
          console.log('Login token stored')
        }
        if (responseData.refreshToken) {
          localStorage.setItem('bb_refresh_token', responseData.refreshToken)
        }
        
        // Verify token is stored
        const storedToken = localStorage.getItem('bb_token')
        if (!storedToken) {
          throw new Error('Failed to store authentication token')
        }
        
        onDriverLogin(responseData)
    } 
    } catch (error) {
      console.error("Error when logging in as driver:", error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-black flex items-center justify-center px-4">
      <div className="bg-white text-black rounded-lg shadow-2xl p-8 w-full max-w-md border-2 border-red-600">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-red-600 mb-2">🍻</h1>
          <h2 className="text-3xl font-bold text-gray-900">BoozeBuddies</h2>
          <p className="text-gray-600 mt-2">Your alcohol delivery service</p>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
              placeholder="Enter email"
              required
              autoComplete="username" // Helps with browser autofill
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
              placeholder="Enter password"
              required
              autoComplete="current-password" // Helps with browser autofill
            />
          </div>
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-red-700 transition duration-200 transform hover:scale-105 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {loading ? 'Signing in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default DriverLogin