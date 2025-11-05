import React, { useState } from 'react'

const Login = ({ onLogin }) => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    e.stopPropagation()
    setLoading(true)
  
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      })
  
      if (response.ok) {
        const userData = await response.json()
        console.log('Login successful:', userData)
        
        // Store token and role from the nested user object
        if (userData.token) {
          localStorage.setItem('authToken', userData.token)
        }
        if (userData.user && userData.user.roles) {
          localStorage.setItem('userRole', userData.user.roles[0]) // Store first role
        }
        
        onLogin(userData)
      } else {
        console.log('API login failed, using hardcoded auth')
        handleHardcodedAuth()
      }
    } catch (error) {
      console.log('API error, using hardcoded auth:', error)
      handleHardcodedAuth()
    } finally {
      setLoading(false)
    }
  }
  
  const handleHardcodedAuth = () => {
    console.log('Hardcoded auth triggered with:', email, password)
    
    if (email === 'admin@boozebuddies.com' && password === 'password') {
      const token = 'simulated-admin-token-123'
      localStorage.setItem('authToken', token)
      localStorage.setItem('userRole', 'ADMIN')
      
      const userData = {
        token: token,
        user: {
          email: 'admin@boozebuddies.com',
          roles: ['ADMIN']
        }
      }
      
      console.log('Hardcoded login successful, calling onLogin')
      onLogin(userData)
    } else if (email === 'merchant1@boozebuddies.com' && password === 'password') {
      const token = 'simulated-merchant-token-123'
      localStorage.setItem('authToken', token)
      localStorage.setItem('userRole', 'MERCHANT_ADMIN')
      localStorage.setItem('merchantId', '1') // Store merchant ID
      
      const userData = {
        token: token,
        user: {
          email: 'merchant1@boozebuddies.com',
          roles: ['MERCHANT_ADMIN'],
          merchantId: 1 // Include merchant ID
        }
      }
      
      console.log('Merchant admin login successful, calling onLogin')
      onLogin(userData)
    } else {
      alert('Invalid credentials. Use: admin@boozebuddies.com / password OR merchant1@boozebuddies.com / password')
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
        
        <div className="mt-6 text-center text-sm text-gray-600">
          <p>Demo credentials:</p>
          <p className="font-mono">Admin: admin@boozebuddies.com / password</p>
          <p className="font-mono">Merchant: merchant1@boozebuddies.com / password</p>
        </div>
      </div>
    </div>
  )
}

export default Login