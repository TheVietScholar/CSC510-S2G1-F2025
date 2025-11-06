import React, { useEffect, useMemo, useState } from 'react'
import { Plus, Minus, ArrowLeft } from 'lucide-react'
import { products } from '../services/api'
import { THUMBNAIL_SIZE } from '../config/ui'
const IMG_PLACEHOLDER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect width="100" height="100" fill="%23e5e7eb"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="10" fill="%239ca3af">No Image</text></svg>'

const RestaurantMenu = ({ restaurant, cart, onAddToCart, onRemoveFromCart, onBack, onViewCart }) => {
  const [menuItems, setMenuItems] = useState([])
  const [search, setSearch] = useState('')

  useEffect(() => {
    if (!restaurant?.id) return
    let mounted = true
    products.getByMerchant(restaurant.id)
      .then(resp => {
        // ProductController returns ApiResponse envelope: { success, data: [...], message }
        const items = resp.data?.data || resp.data || []
        if (mounted) setMenuItems(Array.isArray(items) ? items : [])
      })
      .catch((err) => {
        console.error('Failed to load menu items:', err)
        if (mounted) setMenuItems([])
      })
    return () => { mounted = false }
  }, [restaurant?.id])

  const getItemQuantity = (itemId) => {
    return cart.find(item => item.id === itemId)?.quantity || 0
  }

  const cartItemCount = cart.reduce((total, item) => total + item.quantity, 0)

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      {/* Floating top bar */}
      <div className="fixed top-0 inset-x-0 z-50 w-full" style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, maxWidth: 960, margin: '0 auto', padding: '12px 24px' }}>
          <button
            onClick={onBack}
            className="flex items-center px-4 py-2 rounded-lg border border-gray-300 bg-white hover:bg-gray-100 transition"
          >
            <ArrowLeft className="w-5 h-5 mr-2" />
            Back to Restaurants
          </button>
          <div style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search products..."
              className="w-full max-w-xl px-4 py-2 bg-white border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 shadow-sm"
            />
          </div>
          <button
            onClick={onViewCart}
            className="px-4 py-2 rounded-lg border border-red-300 bg-red-600 text-white hover:bg-red-700 transition"
          >
            🛒 Cart ({cartItemCount})
          </button>
        </div>
      </div>

      {/* Spacer for fixed bar */}
      <div style={{ height: 112 }} />

      <div className="px-6 pb-10" style={{ maxWidth: 960, margin: '0 auto' }}>

        {/* Restaurant Info */}
        <div className="bg-white p-6 mb-8 rounded-xl border border-transparent shadow-sm">
          <h1 className="text-3xl font-bold mb-2">{restaurant.name}</h1>
          <p className="text-gray-600 mb-4">{restaurant.cuisineType} • {restaurant.rating ?? 0} ★</p>

          {/* Centered product search */}
          <div className="w-full flex justify-center mt-4">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search products..."
              className="w-full max-w-xl px-4 py-2 bg-white border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 shadow-sm"
            />
          </div>
        </div>

        {/* Menu Items */}
        <div className="space-y-4">
          {menuItems
            .filter(i => !search || i.name?.toLowerCase().includes(search.toLowerCase()) || i.description?.toLowerCase().includes(search.toLowerCase()))
            .map(item => (
            <div key={item.id} className="bg-white p-6 rounded-xl border border-transparent shadow-sm transition-all duration-300 ease-out hover:-translate-y-1.5 hover:shadow-2xl hover:border-gray-300">
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <img
                  src={item.imageUrl || IMG_PLACEHOLDER}
                  alt={item.name}
                  width={THUMBNAIL_SIZE}
                  height={THUMBNAIL_SIZE}
                  className="object-cover"
                  style={{ borderRadius: 8, flexShrink: 0 }}
                  onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = IMG_PLACEHOLDER }}
                />
                <div className="flex-1">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="text-xl font-semibold mb-1">{item.name}</h3>
                      <p className="text-gray-600 mb-2">{item.description}</p>
                      <div className="flex items-center gap-2 mb-2">
                        {item.category && (
                          <span className="text-xs px-2 py-1 rounded-full bg-gray-100 border border-gray-300 text-gray-700">
                            {item.category}
                          </span>
                        )}
                        {item.isAlcohol && (
                          <span className="text-xs px-2 py-1 rounded-full bg-red-600 text-white">🍺 {item.alcoholContent}% ABV</span>
                        )}
                      </div>
                    </div>
                    <span className="text-2xl font-bold ml-4">${Number(item.price).toFixed(2)}</span>
                  </div>

                  <div className="flex items-center justify-between mt-4">
                    <span className="text-gray-600">
                      {getItemQuantity(item.id) > 0 ? `${getItemQuantity(item.id)} in cart` : 'Not in cart'}
                    </span>
                    <div className="flex items-center space-x-2">
                      {getItemQuantity(item.id) > 0 && (
                        <button
                          onClick={() => onRemoveFromCart(item)}
                          className="bg-gray-200 text-gray-900 p-2 rounded-lg hover:bg-gray-300 transition duration-200"
                        >
                          <Minus className="w-4 h-4" />
                        </button>
                      )}
                      <button
                        onClick={() => onAddToCart(item)}
                        className="bg-red-600 text-white px-4 py-2 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center"
                      >
                        <Plus className="w-4 h-4 mr-2" />
                        Add to Cart
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default RestaurantMenu