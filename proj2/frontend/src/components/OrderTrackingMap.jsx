import React, { useEffect, useRef } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Fix for default marker icons in Leaflet with Webpack/Vite
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

const OrderTrackingMap = ({ 
  deliveryLat, 
  deliveryLng, 
  currentLat, 
  currentLng,
  merchantLat,
  merchantLng 
}) => {
  const mapRef = useRef(null)
  const mapInstanceRef = useRef(null)
  const markersRef = useRef({})

  useEffect(() => {
    // Initialize map if not already created
    if (!mapInstanceRef.current && mapRef.current) {
      // Default center (Raleigh, NC) if no coordinates
      const centerLat = currentLat || deliveryLat || merchantLat || 35.7796
      const centerLng = currentLng || deliveryLng || merchantLng || -78.6382
      
      mapInstanceRef.current = L.map(mapRef.current).setView([centerLat, centerLng], 13)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19,
      }).addTo(mapInstanceRef.current)
    }

    const map = mapInstanceRef.current
    if (!map) return

    // Clear existing markers
    Object.values(markersRef.current).forEach(marker => {
      map.removeLayer(marker)
    })
    markersRef.current = {}

    // Add merchant marker (pickup point)
    if (merchantLat && merchantLng) {
      const merchantMarker = L.marker([merchantLat, merchantLng], {
        icon: L.divIcon({
          className: 'custom-marker',
          html: `<div style="background-color: #3B82F6; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
          iconSize: [24, 24],
          iconAnchor: [12, 12],
        }),
      }).addTo(map)
      merchantMarker.bindPopup('<b>🍽️ Restaurant (Pickup)</b>')
      markersRef.current.merchant = merchantMarker
    }

    // Add delivery address marker (drop-off point)
    if (deliveryLat && deliveryLng) {
      const deliveryMarker = L.marker([deliveryLat, deliveryLng], {
        icon: L.divIcon({
          className: 'custom-marker',
          html: `<div style="background-color: #10B981; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
          iconSize: [24, 24],
          iconAnchor: [12, 12],
        }),
      }).addTo(map)
      deliveryMarker.bindPopup('<b>📍 Delivery Address</b>')
      markersRef.current.delivery = deliveryMarker
    }

    // Add current position marker (driver/order location)
    if (currentLat && currentLng) {
      const currentMarker = L.marker([currentLat, currentLng], {
        icon: L.divIcon({
          className: 'custom-marker',
          html: `<div style="background-color: #EF4444; width: 28px; height: 28px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3); animation: pulse 2s infinite;"></div>`,
          iconSize: [28, 28],
          iconAnchor: [14, 14],
        }),
      }).addTo(map)
      currentMarker.bindPopup('<b>🚗 Current Position</b>')
      markersRef.current.current = currentMarker

      // If we have both merchant and delivery points, draw a route line
      if (merchantLat && merchantLng && deliveryLat && deliveryLng) {
        const routeLine = L.polyline(
          [[merchantLat, merchantLng], [currentLat, currentLng], [deliveryLat, deliveryLng]],
          { color: '#EF4444', dashArray: '10, 5', weight: 3, opacity: 0.7 }
        ).addTo(map)
        markersRef.current.route = routeLine
      }
    }

    // Fit map bounds to show all markers
    const markers = Object.values(markersRef.current).filter(m => m.getLatLng)
    if (markers.length > 0) {
      const group = new L.featureGroup(markers)
      map.fitBounds(group.getBounds().pad(0.2))
    }

    // Cleanup function
    return () => {
      // Keep map instance alive, just remove markers on unmount if needed
    }
  }, [deliveryLat, deliveryLng, currentLat, currentLng, merchantLat, merchantLng])

  return (
    <>
      <style>{`
        @keyframes pulse {
          0% {
            transform: scale(1);
            opacity: 1;
          }
          50% {
            transform: scale(1.1);
            opacity: 0.8;
          }
          100% {
            transform: scale(1);
            opacity: 1;
          }
        }
      `}</style>
      <div className="w-full h-96 rounded-lg overflow-hidden border border-gray-300 shadow-sm">
        <div ref={mapRef} style={{ width: '100%', height: '100%' }} />
      </div>
    </>
  )
}

export default OrderTrackingMap

