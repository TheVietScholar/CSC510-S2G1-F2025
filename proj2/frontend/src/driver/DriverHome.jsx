import React, { use, useEffect, useState } from "react"
import { LogOut,  RefreshCw } from 'lucide-react'
import {drivers, orders} from "../services/api"

const DriverHome = ({ user, onLogout }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [isOnline, setIsOnline] = useState(false);
    const [availableOrders, setAvailableOrders] = useState([]);
    const [assignedOrders, setAssignedOrders] = useState([]);
    const [driverLocation, setDriverLocation] = useState(null);
    const [filter, setFilter] = useState("all");

    const driverId = user.id

    useEffect(() => {
        // Fetch driver profile first to get location, then fetch orders
        fetchDriverProfile();
    }, [driverId]);

    useEffect(() => {
        // Fetch available orders when driver location is available
        if (driverLocation) {
            fetchAvailableOrders();
        }
    }, [driverLocation]);

    const fetchDriverProfile = async () => {
        try {
            setLoading(true)
            setError('')
            
            // Verify token exists before making request
            const token = localStorage.getItem('bb_token')
            if (!token) {
                setError('Authentication token missing. Please log in again.')
                return
            }
            
            // Get user ID from user prop or localStorage
            if (!user?.id) {
                setError('User ID missing. Please log in again.')
                return
            }
            
            const response = await drivers.getMyProfile(user.id)

            const driverData = response.data?.data || response.data;
            if (driverData) {
                // Update driver location from profile
                if (driverData.currentLatitude != null && driverData.currentLongitude != null) {
                    setDriverLocation({ 
                        lat: driverData.currentLatitude, 
                        lng: driverData.currentLongitude 
                    });
                } else {
                    // Default to Raleigh if driver location not set in database
                    setDriverLocation({ lat: 35.7800, lng: -78.6380 });
                }
                // Update online status if available
                if (driverData.isAvailable !== undefined) {
                    setIsOnline(driverData.isAvailable);
                }
            }

        } catch(err) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to fetch driver profile';
            const statusCode = err.response?.status;
            
            if (statusCode === 403) {
                setError('Access forbidden. Please ensure you are logged in as a driver and have the correct permissions.');
            } else if (statusCode === 401) {
                setError('Authentication failed. Please log in again.');
                // Clear invalid token
                localStorage.removeItem('bb_token');
                localStorage.removeItem('bb_refresh_token');
            } else {
                setError(errorMessage);
            }
            
            // Set default location on error
            setDriverLocation({ lat: 35.7800, lng: -78.6380 });
        } finally {
            setLoading(false);
        }
    }

    const fetchAvailableOrders = async () => {
        if (!driverLocation) {
            return;
        }
        try {
            setLoading(true)
            setError('')
            const response = await orders.getAvailableForDriver(driverLocation.lat, driverLocation.lng, 10)

            if (response.data && response.data.data) {
                setAvailableOrders(response.data.data)
            } else if (Array.isArray(response.data)) {
                setAvailableOrders(response.data)
            } else {
                setError('Unexpected response format while fetching available orders.')
            }
        } catch (err) {
            setError('Failed to fetch available orders. Please try again later.')
        } finally {
            setLoading(false)
        }

    }

    const handleToggleOnline = async () => {
        try{
            setLoading(true)
            setError('')
            setIsOnline(!isOnline)
            await drivers.updateAvailability(isOnline)
        } catch (err){
            setError(err.response?.data?.message || 'Failed to update availability')
        } finally {
            setLoading(false);
        }
    }

    function acceptOrder(orderId) {
        const order = availableOrders.find(o => o.id === orderId);
        if (!order) return;
        const accepted = { ...order, status: "accepted", acceptedAt: Date.now() };
        setAvailableOrders(prev => prev.filter(o => o.id !== orderId));
        setAssignedOrders(prev => [accepted, ...prev]);
    }

    function startPickup(orderId) {
        setAssignedOrders(prev => prev.map(o => (o.id === orderId ? { ...o, status: "picking_up", startedAt: Date.now() } : o)));
    }

    function markPickedUp(orderId) {
        setAssignedOrders(prev => prev.map(o => (o.id === orderId ? { ...o, status: "on_route", pickedUpAt: Date.now() } : o)));
    }

    function completeDelivery(orderId) {
        setAssignedOrders(prev => prev.filter(o => o.id !== orderId));
        // in a real app you'd update server and trigger earnings, etc.
        alert("Delivery completed. Good job!");
    }

    const filteredAvailable = availableOrders.filter(o => {
        if (filter === "all") return true;
        if (filter === "nearby") return o.distanceKm <= 2.5;
        if (filter === "fast") return o.etaMin <= 12;
        return true;
    });

    function formatDistance(km) {
        if (km == null) return "N/A";
        if (km < 1) return `${(km * 1000).toFixed(0)}m`;
        return `${km.toFixed(1)}km`;
    }

    return (
        <div>
            <div>
                <div>
                    <div>Driver Home</div>
                    <div style={{ color: "#6b7280", marginTop: 6 }}>Manage your deliveries and stay on the road</div>
                </div>
                <button
              onClick={onLogout}
              className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-red-700 transition duration-200 flex items-center"
            >
              <LogOut className="w-5 h-5 mr-2" />
              Logout
            </button>
            <button
              onClick={fetchAvailableOrders}
              className="bg-gray-700 text-white px-4 py-3 rounded-lg font-semibold hover:bg-gray-600 transition duration-200 flex items-center"
            >
              <RefreshCw className="w-5 h-5 mr-2" />
              Refresh
            </button>
                <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                    <div style={{ textAlign: "right" }}>
                        <div style={{ fontSize: 12, color: "#6b7280" }}>Status</div>
                        <div style={{ marginTop: 6 }}>
                            <button onClick={handleToggleOnline}>
                                {isOnline ? "Online" : "Offline"}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <div>
                    <div style={{ marginBottom: 12 }}>
                        <div>Available Orders</div>
                        <div style={{ display: "flex", gap: 8, marginBottom: 10, alignItems: "center" }}>
                            <select value={filter} onChange={e => setFilter(e.target.value)} style={{ padding: 8, borderRadius: 6 }}>
                                <option value="all">All</option>
                                <option value="nearby">Nearby (&le; 2.5 km)</option>
                                <option value="fast">Fast (&le; 12 min)</option>
                            </select>
                            <div style={{ color: "#6b7280", fontSize: 13 }}>{filteredAvailable.length} result(s)</div>
                        </div>

                        <div>
                            {filteredAvailable.length === 0 && <div>No available orders at the moment.</div>}
                            {filteredAvailable.map(order => {
                                const itemNames = order.items && Array.isArray(order.items) 
                                    ? order.items
                                        .filter(item => item != null) // Filter out null items
                                        .map(item => item.productName || item.name || 'Unknown')
                                        .join(", ")
                                    : "No items";
                                return (
                                <div key={order.id}>
                                    <div>
                                        <div style={{ fontWeight: 700 }}>{order.merchantName || "Unknown Merchant"}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{itemNames}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6, alignItems: "center" }}>
                                            <div>{order.distanceKm != null ? formatDistance(order.distanceKm) : "N/A"}</div>
                                            <div>{order.etaMin != null ? `${order.etaMin} min` : "N/A"}</div>
                                        </div>
                                    </div>

                                    <div>
                                        <button
                                            onClick={() => {
                                                if (!isOnline) {
                                                    if (!window.confirm("You are currently offline. Go online to accept orders?")) return;
                                                    setIsOnline(true);
                                                }
                                                acceptOrder(order.id);
                                            }}
                                        >
                                            Accept
                                        </button>
                                        <button
                                            onClick={() => {
                                                const details = `Order #${order.id}\n` +
                                                    `Restaurant: ${order.merchantName || "Unknown"}\n` +
                                                    `Customer: ${order.customerName || "Unknown"}\n` +
                                                    `Address: ${order.deliveryAddress || "N/A"}\n` +
                                                    `Items: ${itemNames}\n` +
                                                    `Total: $${order.totalAmount || "0.00"}\n` +
                                                    `Distance: ${order.distanceKm != null ? formatDistance(order.distanceKm) : "N/A"}\n` +
                                                    `ETA: ${order.etaMin != null ? `${order.etaMin} min` : "N/A"}`;
                                                alert(details);
                                            }}
                                        >
                                            Details
                                        </button>
                                    </div>
                                </div>
                            )})}
                        </div>
                    </div>

                    <div>
                        <div>Your Assigned Deliveries</div>
                        <div>
                            {assignedOrders.length === 0 && <div>You have no assigned deliveries.</div>}
                            {assignedOrders.map(order => {
                                const itemNames = order.items && Array.isArray(order.items)
                                    ? order.items
                                        .filter(item => item != null) // Filter out null items
                                        .map(item => item.productName || item.name || 'Unknown')
                                        .join(", ")
                                    : "No items";
                                return (
                                <div key={order.id} >
                                    <div>
                                        <div style={{ fontWeight: 700 }}>{order.merchantName || "Unknown"} → {order.customerName || "Unknown"}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{itemNames}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6 }}>
                                            <div >
                                                {order.status ? order.status.replace("_", " ") : "Unknown"}
                                            </div>
                                            <div style={{ color: "#6b7280", fontSize: 13 }}>{order.distanceKm != null ? formatDistance(order.distanceKm) : ""}</div>
                                        </div>
                                    </div>

                                    <div >
                                        {order.status === "accepted" && (
                                            <button onClick={() => startPickup(order.id)}>
                                                Start Pickup
                                            </button>
                                        )}
                                        {order.status === "picking_up" && (
                                            <button onClick={() => markPickedUp(order.id)}>
                                                Mark Picked Up
                                            </button>
                                        )}
                                        {order.status === "on_route" && (
                                            <button onClick={() => completeDelivery(order.id)}>
                                                Complete
                                            </button>
                                        )}
                                        <button onClick={() => alert(`Contact ${order.customerName || "Customer"}`)}>
                                            Contact
                                        </button>
                                    </div>
                                </div>
                            )})}
                        </div>
                    </div>
                </div>

                <div>
                    <div>
                        <div>Map / Navigation</div>
                        <div>
                            Map placeholder — integrate your maps provider here
                        </div>
                        <div style={{ marginTop: 12, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                            <div>
                                <div style={{ fontSize: 13, color: "#6b7280" }}>Current location</div>
                                <div style={{ fontWeight: 700 }}>
                                    {driverLocation ? `${driverLocation.lat.toFixed(4)}, ${driverLocation.lng.toFixed(4)}` : 'Loading...'}
                                </div>
                            </div>
                            <div>
                                <button
                                    onClick={async () => {
                                        if (navigator.geolocation) {
                                            navigator.geolocation.getCurrentPosition(
                                                async (position) => {
                                                    const newLocation = {
                                                        lat: position.coords.latitude,
                                                        lng: position.coords.longitude
                                                    };
                                                    setDriverLocation(newLocation);
                                                    // Update location on server
                                                    try {
                                                        await drivers.updateLocation(newLocation.lat, newLocation.lng);
                                                    } catch (err) {
                                                        // Silently handle location update errors
                                                    }
                                                },
                                                (error) => {
                                                    alert('Unable to get your location. Please enable location services.');
                                                }
                                            );
                                        } else {
                                            alert('Geolocation is not supported by your browser.');
                                        }
                                    }}
                                >
                                    Update Location
                                </button>
                            </div>
                        </div>
                    </div>

                    <div>
                        <div>Quick Stats</div>
                        <div style={{ display: "flex", gap: 12 }}>
                            <div style={{ flex: 1, padding: 12, background: "#f8fafc", borderRadius: 8 }}>
                                <div style={{ fontSize: 12, color: "#6b7280" }}>Earnings (today)</div>
                                <div style={{ fontWeight: 700, fontSize: 18 }}>$0.00</div>
                            </div>
                            <div style={{ flex: 1, padding: 12, background: "#f8fafc", borderRadius: 8 }}>
                                <div style={{ fontSize: 12, color: "#6b7280" }}>Completed</div>
                                <div style={{ fontWeight: 700, fontSize: 18 }}>0</div>
                            </div>
                        </div>
                        <div style={{ marginTop: 12, fontSize: 13, color: "#6b7280" }}>
                            Tip: Toggle online to receive orders. Accept orders you want to pick up.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default DriverHome