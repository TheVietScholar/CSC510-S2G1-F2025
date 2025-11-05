import React, { useState, useEffect } from 'react'
import { Plus, Trash2, LogOut, Store, RefreshCw } from 'lucide-react'
import { merchants } from '../services/api' // Import from your api barrel

const AdminHome = ({ onLogout }) => {
  const [merchantsList, setMerchantsList] = useState([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')
  
  const [newMerchant, setNewMerchant] = useState({
    name: '',
    cuisineType: '',
    address: '',
    phone: '',
    email: '',
    description: ''
  })

  // Load merchants on component mount
  useEffect(() => {
    loadMerchants()
  }, [])

  const loadMerchants = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await merchants.getAll()
      
      // Use response.data.data - the array is nested inside data property
      setMerchantsList(response.data.data) // This is the fix!
    } catch (err) {
      setError('Failed to load merchants. Make sure backend is running on port 8080.')
      console.error('Error loading merchants:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCreateMerchant = async (e) => {
    console.log('Create merchant clicked!')
    e.preventDefault()
    if (!newMerchant.name || !newMerchant.cuisineType) {
      alert('Please fill in required fields: Name and Cuisine Type')
      return
    }
  
    try {
      setCreating(true)
      setError('')
      
      const merchantData = {
        name: newMerchant.name,
        cuisineType: newMerchant.cuisineType,
        address: newMerchant.address || '',
        phone: newMerchant.phone || '',
        email: newMerchant.email || `${newMerchant.name.toLowerCase().replace(/\s+/g, '')}@boozebuddies.com`,
        description: newMerchant.description || `${newMerchant.cuisineType} offering quality beverages`,
        openingTime: '10:00:00',
        closingTime: '22:00:00',
        imageUrl: '/default-restaurant.jpg'
      }
      
      const response = await merchants.register(merchantData)
      console.log('=== DEBUG: Create response ===', response)
      
      // Use response.data.data for the created merchant
      const newMerchantData = response.data.data
      
      setMerchantsList(prev => [...prev, newMerchantData])
      
      // Reset form
      setNewMerchant({ 
        name: '', 
        cuisineType: '', 
        address: '', 
        phone: '', 
        email: '',
        description: ''
      })
      
      alert('Merchant created successfully!')
    } catch (err) {
      setError('Failed to create merchant. Please try again.')
      console.error('Error creating merchant:', err)
    } finally {
      setCreating(false)
    }
  }

  const handleDeleteMerchant = async (id) => {
    console.log('Delete merchant clicked!')
    if (!window.confirm('Are you sure you want to delete this merchant?')) {
      return
    }
  
    try {
      setError('')
      await merchants.delete(id)
      setMerchantsList(prev => prev.filter(merchant => merchant.id !== id))
      alert('Merchant deleted successfully!')
    } catch (err) {
      setError('Failed to delete merchant. Please try again.')
      console.error('Error deleting merchant:', err)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-4 text-red-600" />
          <p>Loading merchants...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-black text-white p-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-4xl font-bold mb-2">Admin Dashboard</h1>
            <p className="text-gray-400">Manage merchants and system settings</p>
          </div>
          <div className="flex space-x-4">
            <button
              onClick={loadMerchants}
              className="bg-gray-700 text-white px-4 py-3 rounded-lg font-semibold hover:bg-gray-600 transition duration-200 flex items-center"
            >
              <RefreshCw className="w-5 h-5 mr-2" />
              Refresh
            </button>
            <button
              onClick={onLogout}
              className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center"
            >
              <LogOut className="w-5 h-5 mr-2" />
              Logout
            </button>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-600 text-white p-4 rounded-lg mb-6">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Create Merchant Form */}
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6">
            <h2 className="text-2xl font-bold mb-6 flex items-center">
              <Plus className="w-6 h-6 mr-3 text-red-600" />
              Create New Merchant
            </h2>
            
            <form onSubmit={handleCreateMerchant} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Merchant Name *
                </label>
                <input
                  type="text"
                  value={newMerchant.name}
                  onChange={(e) => setNewMerchant(prev => ({ ...prev, name: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                  placeholder="Enter merchant name"
                  required
                  disabled={creating}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Cuisine Type *
                </label>
                <select
                  value={newMerchant.cuisineType}
                  onChange={(e) => setNewMerchant(prev => ({ ...prev, cuisineType: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:border-red-600"
                  required
                  disabled={creating}
                >
                  <option value="">Select cuisine type</option>
                  <option value="Brewery">Brewery</option>
                  <option value="Beer Bar">Beer Bar</option>
                  <option value="Wine Bar">Wine Bar</option>
                  <option value="Pub">Pub</option>
                  <option value="Sports Bar">Sports Bar</option>
                  <option value="Cocktail Bar">Cocktail Bar</option>
                  <option value="Lounge">Lounge</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Description
                </label>
                <textarea
                  value={newMerchant.description}
                  onChange={(e) => setNewMerchant(prev => ({ ...prev, description: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                  placeholder="Enter merchant description"
                  rows="2"
                  disabled={creating}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Address
                </label>
                <input
                  type="text"
                  value={newMerchant.address}
                  onChange={(e) => setNewMerchant(prev => ({ ...prev, address: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                  placeholder="Enter address"
                  disabled={creating}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Phone
                  </label>
                  <input
                    type="text"
                    value={newMerchant.phone}
                    onChange={(e) => setNewMerchant(prev => ({ ...prev, phone: e.target.value }))}
                    className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                    placeholder="Enter phone number"
                    disabled={creating}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Email
                  </label>
                  <input
                    type="email"
                    value={newMerchant.email}
                    onChange={(e) => setNewMerchant(prev => ({ ...prev, email: e.target.value }))}
                    className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                    placeholder="Enter email"
                    disabled={creating}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={creating}
                className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center disabled:bg-gray-600 disabled:cursor-not-allowed"
              >
                {creating ? (
                  <RefreshCw className="w-5 h-5 mr-2 animate-spin" />
                ) : (
                  <Store className="w-5 h-5 mr-2" />
                )}
                {creating ? 'Creating...' : 'Create Merchant'}
              </button>
            </form>
          </div>

          {/* Merchant List */}
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold">Managed Merchants</h2>
              <span className="bg-red-600 text-white px-3 py-1 rounded-full text-sm font-semibold">
                {merchantsList.length} merchants
              </span>
            </div>
            
            {merchantsList.length === 0 ? (
              <div className="text-center py-8 text-gray-400">
                <Store className="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>No merchants created yet</p>
                <p className="text-sm mt-2">Create your first merchant using the form</p>
              </div>
            ) : (
              <div className="space-y-4 max-h-96 overflow-y-auto">
                {merchantsList.map(merchant => (
                  <div key={merchant.id} className="bg-gray-800 border border-gray-700 rounded-lg p-4 hover:border-gray-600 transition duration-200">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <h3 className="text-xl font-semibold text-white mb-1">{merchant.name}</h3>
                        <p className="text-gray-400 mb-1">{merchant.cuisineType}</p>
                        <p className="text-gray-500 text-sm mb-2">{merchant.description}</p>
                        {merchant.address && (
                          <p className="text-gray-500 text-sm">📍 {merchant.address}</p>
                        )}
                        {merchant.phone && (
                          <p className="text-gray-500 text-sm">📞 {merchant.phone}</p>
                        )}
                        {merchant.email && (
                          <p className="text-gray-500 text-sm">✉️ {merchant.email}</p>
                        )}
                        <div className="flex items-center space-x-2 mt-2">
                          <span className="bg-red-600 text-white px-2 py-1 rounded text-xs font-semibold">
                            {merchant.rating || 4.5} ★
                          </span>
                          <span className={`px-2 py-1 rounded text-xs font-semibold ${
                            merchant.active 
                              ? 'bg-green-600 text-white' 
                              : 'bg-red-600 text-white'
                          }`}>
                            {merchant.active ? 'Active' : 'Inactive'}
                          </span>
                          <span className="text-gray-500 text-xs">
                            {merchant.openingTime} - {merchant.closingTime}
                          </span>
                        </div>
                        <p className="text-gray-600 text-xs mt-2">ID: {merchant.id}</p>
                      </div>
                      <button
                        onClick={() => handleDeleteMerchant(merchant.id)}
                        disabled={creating}
                        className="text-red-500 hover:text-red-400 transition duration-200 p-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        title="Delete merchant"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Stats */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">{merchantsList.length}</div>
            <div className="text-gray-400">Total Merchants</div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">
              {merchantsList.filter(m => m.cuisineType === 'Brewery').length}
            </div>
            <div className="text-gray-400">Breweries</div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">
              {merchantsList.filter(m => m.active).length}
            </div>
            <div className="text-gray-400">Active</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminHome