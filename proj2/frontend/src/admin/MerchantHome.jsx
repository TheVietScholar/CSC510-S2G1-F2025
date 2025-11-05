import React, { useState, useEffect } from 'react'
import { Plus, Trash2, LogOut, Package, Beer, Wine, RefreshCw } from 'lucide-react'
import { products } from '../services/api' // Import from your api barrel

const MerchantHome = ({ user, onLogout }) => { // Add user to props
  const [productsList, setProductsList] = useState([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')
  
  const [newProduct, setNewProduct] = useState({
    name: '',
    description: '',
    price: '',
    isAlcohol: true,
    alcoholContent: '',
    category: 'Beer'
  })

  // Get merchantId from user prop
  const currentMerchantId = user?.merchantId || 1; // Fallback to 1 for safety 

  // Load products on component mount
  useEffect(() => {
    loadProducts()
  }, [currentMerchantId])

  const loadProducts = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await products.getByMerchant(currentMerchantId)
      console.log('Products response:', response)
      
      // Handle nested response structure like merchants
      if (response.data && response.data.data) {
        setProductsList(response.data.data)
      } else if (Array.isArray(response.data)) {
        setProductsList(response.data)
      } else {
        console.error('Unexpected products response:', response.data)
        setProductsList([])
      }
    } catch (err) {
      setError('Failed to load products. Make sure backend is running on port 8080.')
      console.error('Error loading products:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCreateProduct = async (e) => {
    e.preventDefault()
    if (!newProduct.name || !newProduct.price) {
      alert('Please fill in required fields: Name and Price')
      return
    }
  
    try {
      setCreating(true)
      setError('')
      
      const productData = {
        name: newProduct.name,
        description: newProduct.description || '',
        price: parseFloat(newProduct.price),
        category: newProduct.category,
        merchantId: currentMerchantId,
        isAlcohol: newProduct.isAlcohol, 
        alcoholContent: newProduct.isAlcohol ? (parseFloat(newProduct.alcoholContent) || 5.0) : 0.0,
        isAvailable: true,
        imageUrl: '/default-product.jpg'
      }
      
      console.log('DEBUG - Frontend sending:', JSON.stringify(productData, null, 2))
      
      const response = await products.create(productData)
      console.log('Create product response:', response)
      
      const newProductData = response.data.data || response.data
      
      setProductsList(prev => [...prev, newProductData])
      
      setNewProduct({ 
        name: '', 
        description: '', 
        price: '', 
        isAlcohol: true, 
        alcoholContent: '', 
        category: 'Beer' 
      })
      
      alert('Product added successfully!')
    } catch (err) {
      setError('Failed to create product. Please try again.')
      console.error('Error creating product:', err)
    } finally {
      setCreating(false)
    }
  }

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return
    }

    try {
      setError('')
      await products.delete(id)
      setProductsList(prev => prev.filter(product => product.id !== id))
      alert('Product deleted successfully!')
    } catch (err) {
      setError('Failed to delete product. Please try again.')
      console.error('Error deleting product:', err)
    }
  }

  const handleToggleAvailability = async (id, newAvailability) => {
    try {
      setError('')
      // Call your update product endpoint
      const response = await products.update(id, { available: newAvailability })
      console.log('Toggle availability response:', response)
      
      // Update local state
      setProductsList(prev => 
        prev.map(product => 
          product.id === id 
            ? { ...product, available: newAvailability }
            : product
        )
      )
      
      alert(`Product ${newAvailability ? 'made available' : 'marked unavailable'}!`)
    } catch (err) {
      setError('Failed to update product availability. Please try again.')
      console.error('Error toggling availability:', err)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-4 text-red-600" />
          <p>Loading products...</p>
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
            <h1 className="text-4xl font-bold mb-2">Merchant Dashboard</h1>
            <p className="text-gray-400">Manage your restaurant's product menu</p>
            <p className="text-gray-500 text-sm">Merchant ID: {currentMerchantId}</p>
          </div>
          <div className="flex space-x-4">
            <button
              onClick={loadProducts}
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
          {/* Create Product Form */}
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6">
            <h2 className="text-2xl font-bold mb-6 flex items-center">
              <Plus className="w-6 h-6 mr-3 text-red-600" />
              Add New Product
            </h2>
            
            <form onSubmit={handleCreateProduct} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Product Name *
                </label>
                <input
                  type="text"
                  value={newProduct.name}
                  onChange={(e) => setNewProduct(prev => ({ ...prev, name: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                  placeholder="Enter product name"
                  required
                  disabled={creating}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">
                  Description
                </label>
                <textarea
                  value={newProduct.description}
                  onChange={(e) => setNewProduct(prev => ({ ...prev, description: e.target.value }))}
                  className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                  placeholder="Enter product description"
                  rows="3"
                  disabled={creating}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Price ($) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={newProduct.price}
                    onChange={(e) => setNewProduct(prev => ({ ...prev, price: e.target.value }))}
                    className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                    placeholder="0.00"
                    required
                    disabled={creating}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Category *
                  </label>
                  <select
                    value={newProduct.category}
                    onChange={(e) => setNewProduct(prev => ({ ...prev, category: e.target.value }))}
                    className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:border-red-600"
                    required
                    disabled={creating}
                  >
                    <option value="Beer">Beer</option>
                    <option value="Wine">Wine</option>
                    <option value="Spirits">Spirits</option>
                    <option value="Cocktails">Cocktails</option>
                    <option value="Non-Alcoholic">Non-Alcoholic</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center space-x-4">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={newProduct.isAlcohol}
                    onChange={(e) => setNewProduct(prev => ({ ...prev, isAlcohol: e.target.checked }))}
                    className="mr-2"
                    disabled={creating}
                  />
                  <span className="text-gray-400">Alcoholic Beverage</span>
                </label>
              </div>

              {newProduct.isAlcohol && (
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">
                    Alcohol Content (% ABV)
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    min="0"
                    max="100"
                    value={newProduct.alcoholContent}
                    onChange={(e) => setNewProduct(prev => ({ ...prev, alcoholContent: e.target.value }))}
                    className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-red-600"
                    placeholder="5.0"
                    disabled={creating}
                  />
                </div>
              )}

              <button
                type="submit"
                disabled={creating}
                className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center justify-center disabled:bg-gray-600 disabled:cursor-not-allowed"
              >
                {creating ? (
                  <RefreshCw className="w-5 h-5 mr-2 animate-spin" />
                ) : (
                  <Package className="w-5 h-5 mr-2" />
                )}
                {creating ? 'Creating...' : 'Add Product'}
              </button>
            </form>
          </div>

          {/* Product List */}
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold">Menu Products</h2>
              <span className="bg-red-600 text-white px-3 py-1 rounded-full text-sm font-semibold">
                {productsList.length} products
              </span>
            </div>
            
            {productsList.length === 0 ? (
              <div className="text-center py-8 text-gray-400">
                <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>No products added yet</p>
                <p className="text-sm mt-2">Add your first product using the form</p>
              </div>
            ) : (
              <div className="space-y-4 max-h-96 overflow-y-auto">
                {productsList.map(product => (
                  <div key={product.id} className="bg-gray-800 border border-gray-700 rounded-lg p-4 hover:border-gray-600 transition duration-200">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-start justify-between">
                          <div>
                            <h3 className="text-xl font-semibold text-white mb-1">{product.name}</h3>
                            <p className="text-gray-400 mb-2">{product.description}</p>
                            <div className="flex items-center space-x-4">
                              <span className="text-2xl font-bold text-red-600">${product.price}</span>
                              <span className="bg-gray-700 text-gray-300 px-2 py-1 rounded text-sm">
                                {product.category}
                              </span>
                              {product.isAlcohol && (
                                <span className="bg-red-600 text-white px-2 py-1 rounded text-sm flex items-center">
                                  <Beer className="w-3 h-3 mr-1" />
                                  {product.alcoholContent}% ABV
                                </span>
                              )}
                              {/* AVAILABILITY TOGGLE */}
                              <button
                                onClick={() => handleToggleAvailability(product.id, !product.available)}
                                className={`px-3 py-1 rounded text-sm font-semibold transition duration-200 ${
                                  product.available 
                                    ? 'bg-green-600 hover:bg-green-700 text-white' 
                                    : 'bg-red-600 hover:bg-red-700 text-white'
                                }`}
                              >
                                {product.available ? 'Available' : 'Unavailable'}
                              </button>
                            </div>
                            <p className="text-gray-600 text-xs mt-2">ID: {product.id}</p>
                          </div>
                        </div>
                      </div>
                      <div className="flex flex-col space-y-2">
                        <button
                          onClick={() => handleDeleteProduct(product.id)}
                          disabled={creating}
                          className="text-red-500 hover:text-red-400 transition duration-200 p-2 disabled:opacity-50 disabled:cursor-not-allowed"
                          title="Delete product"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Stats */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">{productsList.length}</div>
            <div className="text-gray-400">Total Products</div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">
              {productsList.filter(p => p.category === 'Beer').length}
            </div>
            <div className="text-gray-400 flex items-center justify-center">
              <Beer className="w-4 h-4 mr-1" /> Beers
            </div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">
              {productsList.filter(p => p.category === 'Wine').length}
            </div>
            <div className="text-gray-400 flex items-center justify-center">
              <Wine className="w-4 h-4 mr-1" /> Wines
            </div>
          </div>
          <div className="bg-gray-900 border border-gray-700 rounded-lg p-6 text-center">
            <div className="text-2xl font-bold text-red-600">
              {productsList.filter(p => !p.isAlcohol).length}
            </div>
            <div className="text-gray-400">Non-Alcoholic</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default MerchantHome