import React, { useState } from 'react'
import { ArrowLeft, Lock, RefreshCw } from 'lucide-react'
import UserSettings from './UserSettings'
import { orders as ordersAPI, deliveries as deliveriesAPI } from '../services/api'

const Checkout = ({ cart, onBack, onConfirm, user, restaurant }) => {
  const [showPayment, setShowPayment] = useState(false)
  const [showSettings, setShowSettings] = useState(false)
  const [placing, setPlacing] = useState(false)
  const [order, setOrder] = useState(null)
  const [delivery, setDelivery] = useState(null)

  const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0)
  const tax = subtotal * 0.08
  const deliveryFee = 2.99
  const total = subtotal + tax + deliveryFee

  const loadLocal = () => {
    let address = null, payment = null
    try {
      address = JSON.parse(localStorage.getItem('bb_address') || 'null')
      payment = JSON.parse(localStorage.getItem('bb_payment') || 'null')
    } catch {}
    return { address, payment }
  }

  const isValid = (address, payment) => {
    const okAddress = address && address.line1 && address.city && address.state && address.zip
    const okPayment = payment && payment.cardNumber && payment.exp && payment.cvc
    return !!(okAddress && okPayment)
  }

  const handlePlaceOrder = () => {
    const { address, payment } = loadLocal()
    if (!isValid(address, payment)) {
      setShowSettings(true)
      return
    }
    setShowPayment(true)
  }

  const confirmPaymentAndCreate = async () => {
    try {
      setPlacing(true)
      const { address } = loadLocal()
      const deliveryAddress = [address.line1, address.line2, address.city, address.state, address.zip]
        .filter(Boolean).join(', ')

      const payload = {
        userId: user?.id || 1,
        merchantId: restaurant?.id,
        deliveryAddress,
        specialInstructions: null,
        items: cart.map(i => ({ productId: i.id, quantity: i.quantity, unitPrice: i.price }))
      }

      const created = await ordersAPI.create(payload)
      const orderData = created.data?.data || created.data || created
      setOrder(orderData)

      // Mock payment confirm
      const mockPayment = { status: 'PAID_TEST' }
      console.log('Payment mock result:', mockPayment)

      // Assign delivery with demo driver id
      const assigned = await deliveriesAPI.assign({ orderId: orderData.id, driverId: 10 })
      const dlv = assigned.data?.data || assigned.data || assigned
      setDelivery(dlv)
      setShowPayment(false)
    } catch (e) {
      console.error(e)
    } finally {
      setPlacing(false)
    }
  }

  const refreshDelivery = async () => {
    if (!delivery?.id) return
    const res = await deliveriesAPI.getById(delivery.id)
    const dlv = res.data?.data || res.data || res
    setDelivery(dlv)
  }

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <div className="max-w-5xl mx-auto px-6 py-6">
        <button
          onClick={onBack}
          className="flex items-center text-gray-600 hover:text-gray-900 transition duration-200 mb-8"
        >
          <ArrowLeft className="w-5 h-5 mr-2" />
          Back to Cart
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Checkout action */}
          <div>
            <h1 className="text-3xl font-bold mb-2">Checkout</h1>
            <p className="text-gray-600 mb-8">Review and place your order</p>
            <button
              onClick={handlePlaceOrder}
              disabled={placing}
              className="w-full bg-red-600 text-white py-4 px-6 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center text-lg"
            >
              <Lock className="w-5 h-5 mr-2" />
              {placing ? 'Placing…' : `Pay & Place Order - $${total.toFixed(2)}`}
            </button>
            {delivery && (
              <div className="mt-6 bg-white border border-transparent rounded-xl shadow-sm p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm text-gray-600">Delivery ID</div>
                    <div className="text-lg font-semibold">{delivery.id}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm text-gray-600">Status</div>
                    <div className="text-lg font-semibold">{delivery.status}</div>
                  </div>
                </div>
                <button onClick={refreshDelivery} className="mt-4 inline-flex items-center px-3 py-2 rounded-lg border border-gray-300 hover:bg-gray-100">
                  <RefreshCw className="w-4 h-4 mr-2" /> Refresh status
                </button>
              </div>
            )}
          </div>

          {/* Order Summary */}
          <div>
            <div className="bg-white border border-transparent rounded-xl shadow-sm p-6 sticky top-4">
              <h2 className="text-2xl font-bold mb-4">Order Summary</h2>
              <div className="space-y-3 mb-6">
                {cart.map(item => (
                  <div key={item.id} className="flex justify-between items-center">
                    <div className="flex items-center space-x-3">
                      <span className="bg-red-600 text-white text-sm font-semibold px-2 py-1 rounded">
                        {item.quantity}
                      </span>
                      <span className="text-gray-800">{item.name}</span>
                    </div>
                    <span className="font-semibold">
                      ${(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
              <div className="space-y-2 border-t border-gray-200 pt-4">
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
                <div className="flex justify-between text-xl font-bold border-t border-gray-200 pt-2">
                  <span>Total</span>
                  <span>${total.toFixed(2)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Payment modal */}
      {showPayment && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowPayment(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
            <h2 className="text-xl font-semibold mb-2">Confirm Payment</h2>
            <p className="text-gray-600 mb-4">This is a mock confirmation. No real charge will occur.</p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setShowPayment(false)} className="px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-100">Cancel</button>
              <button onClick={confirmPaymentAndCreate} disabled={placing} className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700">{placing ? 'Processing…' : 'Confirm'}</button>
            </div>
          </div>
        </div>
      )}

      {/* Settings modal when info missing */}
      {showSettings && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowSettings(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-auto">
            <UserSettings asModal onClose={() => setShowSettings(false)} />
          </div>
        </div>
      )}
    </div>
  )
}

export default Checkout