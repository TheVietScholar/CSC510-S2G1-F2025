import React, { useState } from 'react'
import auth from '../services/auth'

const Login = ({ onLogin, onGoToDriverLogin }) => {
  const [isSignUp, setIsSignUp] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    e.stopPropagation()
    setLoading(true)
    setError('')

    try {
      if (isSignUp) {
        // Registration
        const registerData = {
          name,
          email,
          password,
          phone,
          dateOfBirth: dateOfBirth || null,
        }
        const response = await auth.register(registerData)
        
        // Ensure we have response data
        const responseData = response?.data || response
        
        if (responseData) {
          // Store tokens from registration response
          if (responseData.token) {
            localStorage.setItem('bb_token', responseData.token)
            console.log('Token stored:', responseData.token.substring(0, 20) + '...')
          } else {
            console.warn('No token in registration response')
          }
          
          if (responseData.refreshToken) {
            localStorage.setItem('bb_refresh_token', responseData.refreshToken)
          }
          
          // Verify token is stored before proceeding
          const storedToken = localStorage.getItem('bb_token')
          if (!storedToken) {
            throw new Error('Failed to store authentication token')
          }
          
          onLogin(responseData)
        } else {
          throw new Error('Invalid registration response')
        }
      } else {
        // Sign in
        const response = await auth.login({ email, password })
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
          
          onLogin(responseData)
        } else {
          throw new Error('Invalid login response')
        }
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'An error occurred'
      setError(errorMessage)
      // Fallback to hardcoded for demo if API fails
      if (!isSignUp) {
        handleHardcodedAuth()
      }
    } finally {
      setLoading(false)
    }
  }
  
  const handleHardcodedAuth = () => {
    // Simple authentication fallback for demo
    if (email === 'user@boozebuddies.com' && password === 'password') {
      onLogin({ email: 'user@boozebuddies.com', role: 'user' })
    } else if (email === 'admin@boozebuddies.com' && password === 'password') {
      onLogin({ email: 'admin@boozebuddies.com', role: 'admin' })
    } else if (email === 'merchant1@boozebuddies.com' && password === 'password') {
      onLogin({ 
        email: 'merchant1@boozebuddies.com', 
        role: 'merchant',
        merchantId: 1
      })
    } else {
      setError('Invalid credentials')
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

        {/* Toggle between Sign In and Sign Up */}
        <div className="flex mb-6 bg-gray-100 rounded-lg p-1">
          <button
            type="button"
            onClick={() => {
              setIsSignUp(false)
              setError('')
            }}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              !isSignUp
                ? 'bg-red-600 text-white'
                : 'text-gray-700 hover:text-gray-900'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => {
              setIsSignUp(true)
              setError('')
            }}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition ${
              isSignUp
                ? 'bg-red-600 text-white'
                : 'text-gray-700 hover:text-gray-900'
            }`}
          >
            Sign Up
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-4">
          {isSignUp && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Full Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
                placeholder="Enter your full name"
                required={isSignUp}
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
              placeholder="Enter your email"
              required
              autoComplete="username" // Helps with browser autofill
            />
          </div>

          {isSignUp && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Phone
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
                  placeholder="Enter your phone number"
                  required={isSignUp}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Date of Birth
                </label>
                <input
                  type="date"
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
                  required={isSignUp}
                />
              </div>
            </>
          )}
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-600 focus:border-transparent bg-white text-black"
              placeholder={isSignUp ? "At least 8 characters with letters and numbers" : "Enter password"}
              required
              autoComplete="current-password" // Helps with browser autofill
            />
            {isSignUp && (
              <p className="mt-1 text-xs text-gray-500">
                Password must be at least 8 characters with letters and numbers
              </p>
            )}
          </div>
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-red-700 transition duration-200 transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
          >
            {loading ? 'Processing...' : isSignUp ? 'Sign Up' : 'Sign In'}
          </button>
        </form>

        

        
        {!isSignUp && (
          <div className="mt-6 text-center text-sm text-gray-600">
            <p>Demo credentials:</p>
            <p className="font-mono">User: user@boozebuddies.com / password</p>
            <p className="font-mono">Admin: admin@boozebuddies.com / password</p>
            <p className="font-mono">Merchant: merchant1@boozebuddies.com / password</p>
          </div>
        )}
        
        {/* Driver login redirect button */}
        <div className="mt-4 text-center">
          <button
            type="button"
            onClick={() => onGoToDriverLogin && onGoToDriverLogin()}
            className="inline-block mt-2 px-4 py-2 border border-transparent text-sm font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 transition"
          >
            Driver login
          </button>
        </div>
      </div>
    </div>
  )
}

export default Login