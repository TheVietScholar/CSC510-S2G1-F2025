import React, { useState } from 'react'
import Login from './components/Login'
import DriverLogin from './driver/DriverLogin'
import Home from './components/Home'
import RestaurantMenu from './components/RestaurantMenu'
import Cart from './components/Cart'
import Checkout from './components/Checkout'
import UserSettings from './components/UserSettings'
import AdminHome from './admin/AdminHome'
import MerchantHome from './admin/MerchantHome'
import DriverHome from './driver/DriverHome'
import './App.css'
import DriverOrderState from './driver/DriverOrderState'

function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [user, setUser] = useState(null)
  const [selectedRestaurant, setSelectedRestaurant] = useState(null)
  const [cart, setCart] = useState([])

  const handleLogin = (userData) => {
    console.log('Login userData:', userData)
    setUser(userData.user)
    
    // Check if user has ADMIN or MERCHANT_ADMIN role
    if (userData.user && userData.user.roles && userData.user.roles.includes('ADMIN')) {
      console.log('User is ADMIN, redirecting to admin-home')
      setCurrentPage('admin-home')
    } else if (userData.user && userData.user.roles && userData.user.roles.includes('MERCHANT_ADMIN')) {
      console.log('User is MERCHANT_ADMIN, redirecting to merchant-home')
      setCurrentPage('merchant-home')
    } else {
      console.log('User is regular user, redirecting to home')
      setCurrentPage('home')
    }
  }

  const handleDriverLogin = (userData) => {
    console.log('Driver Login userData:', userData)
    setUser(userData.user)
    
    if (userData.user && userData.user.roles && userData.user.roles.includes('DRIVER')) {
      console.log('User is DRIVER, redirecting to driver-home')
      setCurrentPage('driver-home')
    } else {
      console.log('User is regular user, redirecting to login')
      setCurrentPage('login')
    }
  }

  const handleSelectRestaurant = (restaurant) => {
    setSelectedRestaurant(restaurant)
    setCurrentPage('menu')
  }

  const handleAddToCart = (item) => {
    setCart(prevCart => {
      const existingItem = prevCart.find(cartItem => cartItem.id === item.id)
      if (existingItem) {
        return prevCart.map(cartItem =>
          cartItem.id === item.id
            ? { ...cartItem, quantity: cartItem.quantity + 1 }
            : cartItem
        )
      } else {
        return [...prevCart, { ...item, quantity: 1 }]
      }
    })
  }

  const handleRemoveFromCart = (item) => {
    setCart(prevCart => {
      const existingItem = prevCart.find(cartItem => cartItem.id === item.id)
      if (existingItem && existingItem.quantity > 1) {
        return prevCart.map(cartItem =>
          cartItem.id === item.id
            ? { ...cartItem, quantity: cartItem.quantity - 1 }
            : cartItem
        )
      } else {
        return prevCart.filter(cartItem => cartItem.id !== item.id)
      }
    })
  }

  const handleUpdateQuantity = (itemId, newQuantity) => {
    if (newQuantity === 0) {
      setCart(prevCart => prevCart.filter(item => item.id !== itemId))
    } else {
      setCart(prevCart =>
        prevCart.map(item =>
          item.id === itemId ? { ...item, quantity: newQuantity } : item
        )
      )
    }
  }

  const handleRemoveItem = (itemId) => {
    setCart(prevCart => prevCart.filter(item => item.id !== itemId))
  }

  const handleLogout = () => {
    // Clear tokens from localStorage
    localStorage.removeItem('bb_token')
    localStorage.removeItem('bb_refresh_token')
    setUser(null)
    setCart([])
    setCurrentPage('login')
  }

  const renderPage = () => {
    switch (currentPage) {
      case 'login':
        return <Login onLogin={handleLogin} onGoToDriverLogin={() => setCurrentPage('driver-login')} />
      case 'driver-login':
        return <DriverLogin onDriverLogin={handleDriverLogin} />
      case 'home':
        return <Home onSelectRestaurant={handleSelectRestaurant} onOpenSettings={() => setCurrentPage('settings')} onLogout={handleLogout} />
      case 'menu':
        return (
          <RestaurantMenu
            restaurant={selectedRestaurant}
            cart={cart}
            onAddToCart={handleAddToCart}
            onRemoveFromCart={handleRemoveFromCart}
            onBack={() => setCurrentPage('home')}
            onViewCart={() => setCurrentPage('cart')}
            onLogout={handleLogout}
          />
        )
      case 'cart':
        return (
          <Cart
            cart={cart}
            onUpdateQuantity={handleUpdateQuantity}
            onRemoveItem={handleRemoveItem}
            onBack={() => setCurrentPage('menu')}
            onCheckout={() => setCurrentPage('checkout')}
          />
        )
      case 'checkout':
        return (
          <Checkout
            cart={cart}
            user={user}
            restaurant={selectedRestaurant}
            onBack={() => setCurrentPage('cart')}
            onConfirm={() => setCurrentPage('home')}
          />
        )
      case 'settings':
        return <UserSettings onBack={() => setCurrentPage('home')} />
      case 'admin-home':
        return <AdminHome onLogout={handleLogout} />
      case 'merchant-home':
        console.log('Rendering: MerchantHome')
        return <MerchantHome user={user} onLogout={handleLogout} /> // Add user prop
      case 'driver-home':
        console.log('Rendering Driver-Home')
        return <DriverHome user={user} onLogout={handleLogout} />
      case 'driver-order-status':
        return <DriverOrderState onBack={() => setCurrentPage('driver-home')}/>
      default:
        return <Login onLogin={handleLogin} onGoToDriverLogin={() => setCurrentPage('driver-login') } />
    }
  }

  return <div className="App">{renderPage()}</div>
}

export default App