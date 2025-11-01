import React, { useState } from 'react'
import Login from './components/Login'
import Home from './components/Home'
import RestaurantMenu from './components/RestaurantMenu'
import Cart from './components/Cart'
import Checkout from './components/Checkout'
import UserSettings from './components/UserSettings'
import './App.css'

function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [user, setUser] = useState(null)
  const [selectedRestaurant, setSelectedRestaurant] = useState(null)
  const [cart, setCart] = useState([])

  const handleLogin = () => {
    setUser({ id: 1, username: 'user' })
    setCurrentPage('home')
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

  const renderPage = () => {
    switch (currentPage) {
      case 'login':
        return <Login onLogin={handleLogin} />
      case 'home':
        return <Home onSelectRestaurant={handleSelectRestaurant} onOpenSettings={() => setCurrentPage('settings')} />
      case 'menu':
        return (
          <RestaurantMenu
            restaurant={selectedRestaurant}
            cart={cart}
            onAddToCart={handleAddToCart}
            onRemoveFromCart={handleRemoveFromCart}
            onBack={() => setCurrentPage('home')}
            onViewCart={() => setCurrentPage('cart')}
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
      default:
        return <Login onLogin={handleLogin} />
    }
  }

  return <div className="App">{renderPage()}</div>
}

export default App