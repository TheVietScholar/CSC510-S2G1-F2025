import React, { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
import { merchants } from '../services/api'

const Home = ({ onSelectRestaurant }) => {
  const [searchTerm, setSearchTerm] = useState('')

  const [restaurants, setRestaurants] = useState([])

  useEffect(() => {
    let mounted = true
    merchants.getAll().then(resp => {
      // MerchantController returns ApiResponse envelope
      const payload = resp.data?.data || []
      if (mounted) setRestaurants(payload)
    }).catch(() => setRestaurants([]))
    return () => { mounted = false }
  }, [])

  const filteredRestaurants = restaurants.filter(restaurant =>
    restaurant.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    restaurant.cuisineType?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="min-h-screen bg-black text-white p-4">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-2">Find Bars & Restaurants</h1>
        <p className="text-gray-400 mb-8">Discover the best alcohol delivery near you</p>
        
        {/* Search Bar */}
        <div className="relative mb-8">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Search restaurants or bars..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-3 bg-gray-900 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-red-600"
          />
        </div>

        {/* Restaurants Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredRestaurants.map(restaurant => (
            <div
              key={restaurant.id}
              onClick={() => onSelectRestaurant(restaurant)}
              className="bg-gray-900 border border-gray-700 rounded-lg p-6 cursor-pointer hover:border-red-600 transition duration-200 transform hover:scale-105"
            >
              <div className="flex justify-between items-start mb-3">
                <h3 className="text-xl font-semibold text-white">{restaurant.name}</h3>
                <span className="bg-red-600 text-white px-2 py-1 rounded text-sm font-semibold">
                  {restaurant.rating ?? 0} ★
                </span>
              </div>
              <p className="text-gray-400 mb-2">{restaurant.cuisineType}</p>
              <div className="flex justify-between items-center text-sm text-gray-500">
                <span>📍 {restaurant.address}</span>
                <button className="text-red-500 hover:text-red-400 font-semibold">
                  View Menu →
                </button>
              </div>
            </div>
          ))}
        </div>

        {filteredRestaurants.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg">No restaurants found matching "{searchTerm}"</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default Home