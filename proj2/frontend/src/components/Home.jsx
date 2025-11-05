import React, { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
import { merchants } from '../services/api'
import { THUMBNAIL_SIZE, TOP_BAR_HEIGHT, PAGE_BG, BORDER_LIGHT, BUTTON_SECONDARY } from '../config/ui'
const IMG_PLACEHOLDER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect width="100" height="100" fill="%23e5e7eb"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="10" fill="%239ca3af">No Image</text></svg>'
import UserSettings from './UserSettings'

const Home = ({ onSelectRestaurant }) => {
  const [searchTerm, setSearchTerm] = useState('')

  const [restaurants, setRestaurants] = useState([])
  const [settingsOpen, setSettingsOpen] = useState(false)

  useEffect(() => {
    let mounted = true
    // Token is automatically added by axios interceptor in http.js
    merchants.getByDistance().then(resp => {
      // MerchantController returns ApiResponse envelope
      const payload = resp.data?.data || []
      if (mounted) setRestaurants(payload)
    }).catch((err) => {
      console.error('Failed to load merchants:', err)
      if (mounted) setRestaurants([])
    })
    return () => { mounted = false }
  }, []) // Empty dependency array - only run once on mount

  const filteredRestaurants = restaurants.filter(restaurant =>
    restaurant.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    restaurant.cuisineType?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      {/* Floating search bar */}
      <div
        className="fixed top-0 inset-x-0 z-50 w-full"
        style={{ background: PAGE_BG, borderBottom: `1px solid ${BORDER_LIGHT}` }}
      >
        <div
          style={{ display: 'flex', alignItems: 'center', gap: 16, maxWidth: 960, margin: '0 auto', padding: '12px 24px' }}
        >
          <div className="relative" style={{ width: '100%', maxWidth: 640 }}>
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Search restaurants or bars..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-12 pr-4 py-3 bg-white border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 shadow-sm"
            />
          </div>
          <div style={{ flex: 1, display: 'flex', justifyContent: 'flex-end' }}>
            <button onClick={() => setSettingsOpen(true)} className={`${BUTTON_SECONDARY}`}>
              User Settings
            </button>
          </div>
        </div>
      </div>

      {/* Spacer for fixed bar */}
      <div style={{ height: TOP_BAR_HEIGHT }} />

      <div className="px-6 pb-10" style={{ maxWidth: 960, margin: '0 auto' }}>
        {/* Restaurants Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
          {filteredRestaurants.map(restaurant => (
            <div
              key={restaurant.id}
              onClick={() => onSelectRestaurant(restaurant)}
              className="bg-white p-6 rounded-xl border border-transparent shadow-sm cursor-pointer transition-all duration-300 ease-out hover:-translate-y-1.5 hover:shadow-2xl hover:border-gray-300"
            >
              <div className="mb-3" style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <img
                  src={restaurant.imageUrl || IMG_PLACEHOLDER}
                  alt={restaurant.name}
                  width={THUMBNAIL_SIZE}
                  height={THUMBNAIL_SIZE}
                  className="object-cover"
                  style={{ borderRadius: 8, flexShrink: 0 }}
                  onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = IMG_PLACEHOLDER }}
                />
                <div className="flex-1">
                  <div className="flex justify-between items-start">
                    <h3 className="text-xl font-semibold">{restaurant.name}</h3>
                    <span className="bg-red-600 text-white px-2 py-1 rounded-md text-sm font-semibold">
                      {restaurant.rating ?? 0} ★
                    </span>
                  </div>
                  <div className="flex items-center gap-2 mb-2">
                    {restaurant.cuisineType && (
                      <span className="text-xs px-2 py-1 rounded-full bg-gray-100 border border-gray-300 text-gray-700">
                        {restaurant.cuisineType}
                      </span>
                    )}
                  </div>
                  <p className="text-gray-600 text-sm">📍 {restaurant.address}</p>
                </div>
              </div>
              <div className="flex justify-end"></div>
            </div>
          ))}
        </div>

        {filteredRestaurants.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg">No restaurants found matching "{searchTerm}"</p>
          </div>
        )}
      </div>

      {/* Settings Modal */}
      {settingsOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setSettingsOpen(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-auto p-0">
            <UserSettings asModal onClose={() => setSettingsOpen(false)} />
          </div>
        </div>
      )}
    </div>
  )
}

export default Home