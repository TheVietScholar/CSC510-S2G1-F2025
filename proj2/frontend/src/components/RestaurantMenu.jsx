import React, { useState } from 'react'
import { Plus, Minus, ArrowLeft } from 'lucide-react'

const RestaurantMenu = ({ restaurant, cart, onAddToCart, onRemoveFromCart, onBack, onViewCart }) => {
  // Mock menu data
  const menuItems = [
    { id: 1, name: 'Craft IPA', description: 'Hoppy IPA with citrus notes', price: 8.99, isAlcohol: true, alcoholContent: 6.5 },
    { id: 2, name: 'Imperial Stout', description: 'Rich dark beer with coffee notes', price: 9.99, isAlcohol: true, alcoholContent: 8.0 },
    { id: 3, name: 'Pale Ale', description: 'Smooth and balanced ale', price: 7.99, isAlcohol: true, alcoholContent: 5.2 },
    { id: 4, name: 'Craft Soda', description: 'House-made root beer', price: 4.99, isAlcohol: false },
    { id: 5, name: 'Whiskey Sour', description: 'Classic cocktail with bourbon', price: 12.99, isAlcohol: true, alcoholContent: 15.0 },
  ]

  const getItemQuantity = (itemId) => {
    return cart.find(item => item.id === itemId)?.quantity || 0
  }

  const cartItemCount = cart.reduce((total, item) => total + item.quantity, 0)

  return (
    <div className="min-h-screen bg-black text-white p-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <button
            onClick={onBack}
            className="flex items-center text-gray-400 hover:text-white transition duration-200"
          >
            <ArrowLeft className="w-5 h-5 mr-2" />
            Back to Restaurants
          </button>
          
          {/* Cart Button */}
          <button
            onClick={onViewCart}
            className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center"
          >
            🛒 Cart ({cartItemCount})
          </button>
        </div>

        {/* Restaurant Info */}
        <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 mb-8">
          <h1 className="text-3xl font-bold mb-2">{restaurant.name}</h1>
          <p className="text-gray-400 mb-4">{restaurant.type} • {restaurant.distance} • {restaurant.rating} ★</p>
          <p className="text-white">Browse our selection of fine beverages</p>
        </div>

        {/* Menu Items */}
        <div className="space-y-4">
          {menuItems.map(item => (
            <div key={item.id} className="bg-gray-900 border border-gray-700 rounded-lg p-6">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="text-xl font-semibold text-white mb-1">{item.name}</h3>
                      <p className="text-gray-400 mb-2">{item.description}</p>
                      {item.isAlcohol && (
                        <span className="inline-block bg-red-600 text-white px-2 py-1 rounded text-sm font-semibold mb-2">
                          🍺 {item.alcoholContent}% ABV
                        </span>
                      )}
                    </div>
                    <span className="text-2xl font-bold text-white ml-4">${item.price}</span>
                  </div>
                  
                  {/* Quantity Controls */}
                  <div className="flex items-center justify-between mt-4">
                    <span className="text-gray-400">
                      {getItemQuantity(item.id) > 0 ? `${getItemQuantity(item.id)} in cart` : 'Not in cart'}
                    </span>
                    <div className="flex items-center space-x-2">
                      {getItemQuantity(item.id) > 0 && (
                        <button
                          onClick={() => onRemoveFromCart(item)}
                          className="bg-gray-700 text-white p-2 rounded-lg hover:bg-gray-600 transition duration-200"
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