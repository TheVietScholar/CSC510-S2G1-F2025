import React from 'react'
import { CheckCircle, Home, Package } from 'lucide-react'

const OrderConfirmed = ({ onBackToHome }) => {
  return (
    <div className="min-h-screen bg-black text-white p-4">
      <div className="max-w-2xl mx-auto text-center">
        {/* Success Icon */}
        <div className="mb-8">
          <div className="w-24 h-24 bg-red-600 rounded-full flex items-center justify-center mx-auto mb-6">
            <CheckCircle className="w-12 h-12 text-white" />
          </div>
        </div>

        {/* Success Message */}
        <h1 className="text-4xl font-bold mb-4">Order Confirmed! 🎉</h1>
        <p className="text-xl text-gray-400 mb-8">
          Thank you for your order. Your drinks are being prepared and will be delivered soon.
        </p>

        {/* Order Details Card */}
        <div className="bg-gray-900 border border-gray-700 rounded-lg p-8 mb-8 text-left">
          <div className="flex items-center justify-center mb-6">
            <Package className="w-8 h-8 text-red-600 mr-3" />
            <h2 className="text-2xl font-semibold">Order Details</h2>
          </div>
          
          <div className="space-y-4">
            <div className="flex justify-between">
              <span className="text-gray-400">Order Number</span>
              <span className="text-white font-mono">#BB{Math.random().toString(36).substr(2, 8).toUpperCase()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Estimated Delivery</span>
              <span className="text-white">25-35 minutes</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Delivery To</span>
              <span className="text-white">Your current location</span>
            </div>
            <div className="flex justify-between text-lg font-semibold border-t border-gray-700 pt-4">
              <span className="text-gray-400">Total</span>
              <span className="text-red-600">${(Math.random() * 50 + 20).toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Next Steps */}
        <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 mb-8">
          <h3 className="text-lg font-semibold mb-4">What's Next?</h3>
          <div className="space-y-3 text-gray-400 text-left">
            <p>✅ Your order has been received</p>
            <p>🔄 Preparing your drinks</p>
            <p>🚚 Driver will be assigned shortly</p>
            <p>📱 You'll receive tracking updates</p>
          </div>
        </div>

        {/* Action Button */}
        <button
          onClick={onBackToHome}
          className="w-full max-w-md bg-red-600 text-white py-4 px-6 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center text-lg mx-auto"
        >
          <Home className="w-5 h-5 mr-3" />
          Back to Home
        </button>

        {/* Thank You Message */}
        <p className="text-gray-500 mt-8">
          Thank you for choosing <span className="text-red-600 font-semibold">BoozeBuddies</span>! 🍻
        </p>
      </div>
    </div>
  )
}

export default OrderConfirmed