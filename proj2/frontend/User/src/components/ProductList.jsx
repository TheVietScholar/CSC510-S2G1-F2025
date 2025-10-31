import React, { useState, useEffect } from 'react'
import { productAPI } from '../services/api'

const ProductList = () => {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    try {
      const response = await productAPI.getAll()
      setProducts(response.data)
    } catch (error) {
      console.error('Error loading products:', error)
      setError('Failed to load products. Make sure your backend is running on port 8080.')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <div className="text-center py-8">Loading products...</div>
  if (error) return <div className="text-center py-8 text-red-600">{error}</div>

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">🍺 Our Products</h1>
      {products.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-gray-600">No products found.</p>
          <p className="text-sm text-gray-500 mt-2">Make sure your backend has products in the database.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map(product => (
            <div key={product.id} className="bg-white rounded-lg shadow-md p-6 border">
              <h3 className="text-xl font-semibold mb-2">{product.name}</h3>
              <p className="text-gray-600 mb-2">{product.description}</p>
              <div className="flex justify-between items-center">
                <span className="text-2xl font-bold text-green-600">${product.price}</span>
                <span className={`px-3 py-1 rounded-full text-sm ${
                  product.available 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                }`}>
                  {product.available ? 'Available' : 'Out of Stock'}
                </span>
              </div>
              {product.isAlcohol && (
                <div className="mt-2 flex items-center text-orange-600">
                  <span className="text-sm">🍺 Alcoholic • {product.alcoholContent}% ABV</span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ProductList