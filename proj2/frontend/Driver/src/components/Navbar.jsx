import React from 'react'
import { Link } from 'react-router-dom'

const Navbar = () => {
  return (
    <nav className="bg-blue-600 text-white shadow-lg">
      <div className="container mx-auto px-4 py-3">
        <div className="flex justify-between items-center">
          <Link to="/" className="text-xl font-bold flex items-center">
            🍻 BoozeBuddies Driver
          </Link>
          <div className="space-x-4">
            <Link to="/order" className="hover:text-blue-200">Order</Link>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar