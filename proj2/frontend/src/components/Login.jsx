import React, { useState } from 'react'

const Login = ({ onLogin }) => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    try {
      // Call your backend authentication endpoint
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      })

      if (response.ok) {
        const userData = await response.json()
        onLogin(userData) // userData should include role, merchantId, etc.
      } else {
        // Fallback to hardcoded for demo
        handleHardcodedAuth()
      }
    } catch (error) {
      // Fallback to hardcoded for demo
      handleHardcodedAuth()
    } finally {
      setLoading(false)
    }
  }

  const handleHardcodedAuth = () => {
    // Simple authentication fallback
    if (username === 'user' && password === 'password') {
      onLogin({ username: 'user', role: 'user' })
    } else if (username === 'admin' && password === 'password') {
      onLogin({ username: 'admin', role: 'admin' })
    } else if (username === 'merchant1' && password === 'password') {
      onLogin({ 
        username: 'merchant1', 
        role: 'merchant',
        merchantId: 1 // This should come from the actual merchant user
      })
    } else {
      alert('Invalid credentials')
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
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
              placeholder="Enter username"
              required
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
            />
          </div>
          
          <button
            type="submit"
            className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-red-700 transition duration-200 transform hover:scale-105"
          >
            Login
          </button>
        </form>
        
        <div className="mt-6 text-center text-sm text-gray-600">
          <p>Demo credentials:</p>
          <p className="font-mono">User: user / password</p>
          <p className="font-mono">Admin: admin / password</p>
          <p className="font-mono">Merchant: merchant1 / password</p>
        </div>
      </div>
    </div>
  )
}

export default Login