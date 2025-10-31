import React from 'react'
import { Link } from 'react-router-dom'

const Navbar = () => {
  return (
    <nav className="bg-blue-600 text-white shadow-lg">
      <div className="container mx-auto px-4 py-3">
        <div className="flex justify-between items-center">
          <Link to="/" className="text-xl font-bold flex items-center">
            🍻 BoozeBuddies
          </Link>
          <div className="space-x-4">
            <Link to="/" className="hover:text-blue-200">Products</Link>
            <Link to="/order" className="hover:text-blue-200">Order</Link>
            <Link to="/register" className="hover:text-blue-200">Register</Link>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar