import React from 'react'
import { Minus, Plus, Trash2, ArrowLeft, CreditCard } from 'lucide-react'

const Cart = ({ cart, onUpdateQuantity, onRemoveItem, onBack, onCheckout }) => {
  const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0)
  const tax = subtotal * 0.08 // 8% tax
  const deliveryFee = 2.99
  const total = subtotal + tax + deliveryFee

  if (cart.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 text-gray-900">
        <div className="max-w-5xl mx-auto px-6 py-6">
          <button
            onClick={onBack}
            className="flex items-center text-gray-600 hover:text-gray-900 transition duration-200 mb-8"
          >
            <ArrowLeft className="w-5 h-5 mr-2" />
            Back to Menu
          </button>
          
          <div className="text-center py-16">
            <div className="text-6xl mb-4">🛒</div>
            <h2 className="text-2xl font-bold mb-4">Your cart is empty</h2>
            <p className="text-gray-600 mb-8">Add some delicious drinks to get started!</p>
            <button
              onClick={onBack}
              className="bg-red-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-red-700 transition duration-200"
            >
              Browse Restaurants
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <div className="max-w-5xl mx-auto px-6 py-6">
        <button
          onClick={onBack}
          className="flex items-center text-gray-600 hover:text-gray-900 transition duration-200 mb-8"
        >
          <ArrowLeft className="w-5 h-5 mr-2" />
          Back to Menu
        </button>

        <h1 className="text-3xl font-bold mb-8">Your Cart</h1>

        {/* Cart Items */}
        <div className="space-y-4 mb-8">
          {cart.map(item => (
            <div key={item.id} className="bg-white border border-transparent rounded-xl shadow-sm p-6">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h3 className="text-xl font-semibold mb-1">{item.name}</h3>
                  <p className="text-gray-600 mb-2">{item.description}</p>
                  {item.isAlcohol && (
                    <span className="inline-block bg-red-600 text-white px-2 py-1 rounded text-sm font-semibold">
                      🍺 Alcoholic
                    </span>
                  )}
                </div>
                <span className="text-2xl font-bold ml-4">${item.price}</span>
              </div>
              
              <div className="flex items-center justify-between mt-4">
                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
                    className="bg-gray-200 text-gray-900 p-2 rounded-lg hover:bg-gray-300 transition duration-200"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="text-lg font-semibold w-8 text-center">{item.quantity}</span>
                  <button
                    onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
                    className="bg-gray-200 text-gray-900 p-2 rounded-lg hover:bg-gray-300 transition duration-200"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
                
                <div className="flex items-center space-x-4">
                  <span className="text-xl font-bold">
                    ${(item.price * item.quantity).toFixed(2)}
                  </span>
                  <button
                    onClick={() => onRemoveItem(item.id)}
                    className="text-red-600 hover:text-red-500 transition duration-200 p-2"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Order Summary */}
        <div className="bg-white border border-transparent rounded-xl shadow-sm p-6 mb-8">
          <h2 className="text-2xl font-bold mb-4">Order Summary</h2>
          <div className="space-y-2">
            <div className="flex justify-between text-gray-600">
              <span>Subtotal</span>
              <span>${subtotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Tax</span>
              <span>${tax.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Delivery Fee</span>
              <span>${deliveryFee.toFixed(2)}</span>
            </div>
            <div className="border-t border-gray-200 pt-2 mt-2">
              <div className="flex justify-between text-xl font-bold">
                <span>Total</span>
                <span>${total.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Checkout Button */}
        <button
          onClick={onCheckout}
          className="w-full bg-red-600 text-white py-4 px-6 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center text-lg"
        >
          <CreditCard className="w-6 h-6 mr-3" />
          Proceed to Checkout
        </button>
      </div>
    </div>
  )
}

export default Cart