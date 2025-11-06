import React, { useState, useEffect, useRef } from 'react'
import { ArrowLeft, Lock, Loader2, AlertTriangle, Package, MapPin, Clock } from 'lucide-react'
import UserSettings from './UserSettings'
import OrderTrackingMap from './OrderTrackingMap'
import { orders as ordersAPI, users as usersAPI, deliveries as deliveriesAPI, merchants as merchantsAPI } from '../services/api'

const Checkout = ({ cart, onBack, onConfirm, user, restaurant }) => {
  const [showPayment, setShowPayment] = useState(false)
  const [showSettings, setShowSettings] = useState(false)
  const [placing, setPlacing] = useState(false)
  const [order, setOrder] = useState(null)
  const [delivery, setDelivery] = useState(null)
  const [userProfile, setUserProfile] = useState(null)
  const [merchant, setMerchant] = useState(null)
  const pollingIntervalRef = useRef(null)
  
  // Check if cart contains alcohol
  const hasAlcohol = cart.some(item => item.isAlcohol)
  
  useEffect(() => {
    // Fetch current user profile to check age verification status
    fetchUserProfile()
    
    // Fetch merchant info if restaurant is provided
    if (restaurant?.id) {
      fetchMerchantInfo(restaurant.id)
    }
  }, [])

  // Auto-polling for order and delivery updates
  useEffect(() => {
    if (!order?.id) return

    // Start polling immediately, then every 5 seconds
    const pollUpdates = async () => {
      try {
        // Fetch latest order status
        const orderRes = await ordersAPI.getById(order.id)
        const latestOrder = orderRes.data?.data || orderRes.data
        if (latestOrder) {
          setOrder(latestOrder)
        }

        // Fetch delivery status
        try {
          const deliveryRes = await deliveriesAPI.getByOrderId(order.id)
          const latestDelivery = deliveryRes.data?.data || deliveryRes.data
          if (latestDelivery) {
            setDelivery(latestDelivery)
          }
        } catch (e) {
          // Delivery might not exist yet, that's okay
          if (e.response?.status !== 404) {
            console.warn('Failed to fetch delivery:', e)
          }
        }
      } catch (e) {
        console.error('Failed to poll updates:', e)
      }
    }

    // Poll immediately
    pollUpdates()

    // Poll every 5 seconds
    pollingIntervalRef.current = setInterval(pollUpdates, 5000)

    // Cleanup on unmount or when order changes
    return () => {
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current)
        pollingIntervalRef.current = null
      }
    }
  }, [order?.id])

  const fetchMerchantInfo = async (merchantId) => {
    try {
      const response = await merchantsAPI.getById(merchantId)
      const merchantData = response.data?.data || response.data
      if (merchantData) {
        setMerchant(merchantData)
      }
    } catch (err) {
      console.error('Failed to fetch merchant info:', err)
    }
  }
  
  const fetchUserProfile = async () => {
    try {
      const response = await usersAPI.getMe()
      const profile = response.data?.data || response.data
      if (profile) {
        console.log('User profile from API:', profile)
        console.log('ageVerified value:', profile.ageVerified, typeof profile.ageVerified)
        setUserProfile(profile)
      }
    } catch (err) {
      console.error('Failed to fetch user profile:', err)
      // Try using user prop as fallback
      if (user) {
        console.log('Using user prop as fallback:', user)
        console.log('user.ageVerified:', user.ageVerified)
        setUserProfile(user)
      }
    }
  }

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

      // Ensure a valid user id (fallback to first existing user)
      let userId = user?.id
      if (!userId) {
        try {
          const ures = await usersAPI.getAll()
          const list = ures.data?.data || ures.data || []
          userId = list[0]?.id || 1
        } catch {}
      }

      const payload = {
        userId,
        merchantId: restaurant?.id,
        deliveryAddress,
        specialInstructions: null,
        items: cart.map(i => ({
          productId: i.id,
          quantity: i.quantity || 1,
          unitPrice: i.price != null ? Number(i.price) : 0.00  // Ensure it's a number, not null/undefined
        }))
      }
      
      // Validate payload before sending
      if (!payload.userId || !payload.merchantId || !payload.deliveryAddress) {
        throw new Error('Missing required order fields')
      }
      if (!payload.items || payload.items.length === 0) {
        throw new Error('Order must contain at least one item')
      }
      // Ensure all items have valid values
      for (const item of payload.items) {
        if (!item.productId || !item.quantity || item.unitPrice == null) {
          throw new Error(`Invalid order item: ${JSON.stringify(item)}`)
        }
      }

      const created = await ordersAPI.create(payload)
      const orderData = created.data?.data || created.data || created
      setOrder(orderData)

      // Mock payment confirm
      const mockPayment = { status: 'PAID_TEST' }
      console.log('Payment mock result:', mockPayment)

      // Order is created successfully - driver will be assigned by the system
      // Users should wait for driver assignment, not manage it themselves
      setDelivery({ id: orderData.id, status: 'PENDING_ASSIGNMENT' })
      setShowPayment(false)
    } catch (e) {
      console.error(e)
    } finally {
      setPlacing(false)
    }
  }

  // Removed refreshDelivery - users should not manually check driver status
  // Driver assignment will be handled by the system

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
            
            {/* Age Verification Warning */}
            {hasAlcohol && !(userProfile?.ageVerified === true) && (
              <div className="mb-6 bg-yellow-50 border-2 border-yellow-400 rounded-lg p-4">
                <div className="flex items-start gap-3">
                  <AlertTriangle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
                  <div className="flex-1">
                    <h3 className="font-semibold text-yellow-800 mb-1">Age Verification Required</h3>
                    <p className="text-sm text-yellow-700 mb-3">
                      Your cart contains alcohol products. You must verify your age before placing this order.
                    </p>
                    <button
                      onClick={() => setShowSettings(true)}
                      className="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition text-sm font-medium"
                    >
                      Verify Age in Settings
                    </button>
                  </div>
                </div>
              </div>
            )}
            
            <button
              onClick={handlePlaceOrder}
              disabled={placing || (hasAlcohol && !(userProfile?.ageVerified === true))}
              className="w-full bg-red-600 text-white py-4 px-6 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center text-lg disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Lock className="w-5 h-5 mr-2" />
              {placing ? 'Placing…' : `Pay & Place Order - $${total.toFixed(2)}`}
            </button>
            {order && (
              <div className="mt-6 space-y-4">
                {/* Order Status Card */}
                <div className="bg-gradient-to-r from-red-50 to-orange-50 border-2 border-red-200 rounded-xl shadow-sm p-6">
                  <div className="flex items-start gap-4">
                    <div className="flex-shrink-0 mt-1">
                      {delivery?.status === 'DELIVERED' ? (
                        <Package className="w-6 h-6 text-green-600" />
                      ) : delivery?.driverId ? (
                        <MapPin className="w-6 h-6 text-blue-600" />
                      ) : (
                        <Loader2 className="w-6 h-6 text-red-600 animate-spin" />
                      )}
                    </div>
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-900 mb-2">Order Confirmed!</h3>
                      <p className="text-gray-700 mb-3">
                        Your order #{order.id} has been placed successfully. Payment has been processed.
                      </p>
                      
                      {/* Order Status */}
                      <div className="bg-white/70 rounded-lg p-4 border border-red-100 mb-3">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-sm font-medium text-gray-800">Order Status</span>
                          <span className="text-sm font-semibold text-gray-900 capitalize">{order.status?.toLowerCase() || 'pending'}</span>
                        </div>
                        {delivery && (
                          <>
                            <div className="flex items-center justify-between mb-2">
                              <span className="text-sm font-medium text-gray-800">Delivery Status</span>
                              <span className="text-sm font-semibold text-gray-900 capitalize">{delivery.status?.toLowerCase() || 'pending'}</span>
                            </div>
                            {delivery.driverName && (
                              <div className="flex items-center justify-between">
                                <span className="text-sm font-medium text-gray-800">Driver</span>
                                <span className="text-sm text-gray-900">{delivery.driverName}</span>
                              </div>
                            )}
                            {delivery.estimatedDeliveryTime && (
                              <div className="flex items-center gap-2 mt-2 pt-2 border-t border-red-100">
                                <Clock className="w-4 h-4 text-gray-600" />
                                <span className="text-xs text-gray-600">
                                  Est. delivery: {new Date(delivery.estimatedDeliveryTime).toLocaleTimeString()}
                                </span>
                              </div>
                            )}
                          </>
                        )}
                        {!delivery && (
                          <div className="mt-2">
                            <p className="text-sm text-gray-600">
                              🚗 Hold tight! We are assigning the best driver for you. You'll be notified once your driver is on the way.
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Map Component - Show when delivery exists and has location data */}
                {delivery && (delivery.currentLatitude || delivery.deliveryLatitude) && (
                  <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-4">
                    <h4 className="text-md font-semibold text-gray-900 mb-3 flex items-center gap-2">
                      <MapPin className="w-5 h-5 text-red-600" />
                      Live Tracking
                    </h4>
                    <OrderTrackingMap
                      deliveryLat={delivery.deliveryLatitude}
                      deliveryLng={delivery.deliveryLongitude}
                      currentLat={delivery.currentLatitude}
                      currentLng={delivery.currentLongitude}
                      merchantLat={merchant?.latitude}
                      merchantLng={merchant?.longitude}
                    />
                  </div>
                )}
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
          <div className="absolute inset-0 bg-black/50" onClick={() => {
            setShowSettings(false)
            fetchUserProfile() // Refresh user profile when closing settings
          }} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-auto">
            <UserSettings 
              asModal 
              onClose={() => {
                setShowSettings(false)
                fetchUserProfile() // Refresh user profile after verification
              }} 
            />
          </div>
        </div>
      )}
    </div>
  )
}

export default Checkout