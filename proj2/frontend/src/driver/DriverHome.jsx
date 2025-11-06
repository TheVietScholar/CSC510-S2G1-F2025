import React, { use, useEffect, useState } from "react"
import { LogOut,  RefreshCw } from 'lucide-react'
import {drivers, orders} from "../services/api"

const DriverHome = ({ user, onLogout }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [isOnline, setIsOnline] = useState(false);
    const [availableOrders, setAvailableOrders] = useState([]);
    const [assignedOrders, setAssignedOrders] = useState([]);
    const [driverLocation, setDriverLocation] = useState({ lat: 37.7749, lng: -122.4194 });
    const [filter, setFilter] = useState("all");

    const driverId = user.id

    useEffect(() => {
        // Fetch initial available orders from server
        fetchAvailableOrders();
        fetchDriverProfile();
    }, [driverId]);

    const fetchDriverProfile = async () => {
        try {
            setLoading(true)
            setError('')
            const response = await drivers.getMyProfile()
            console.log('My Driver Profile response:', response)

            if(response.data && response.data.data) {
            }

        } catch(err) {

        } finally {

        }
    }

    const fetchAvailableOrders = async () => {
        try {
            setLoading(true)
            setError('')
            const response = await orders.getAvailableForDriver(driverLocation.lat, driverLocation.lng, 10)
            console.log('Available Orders response:', response)

            if (response.data && response.data.data) {
                setAvailableOrders(response.data.data)
            } else if (Array.isArray(response.data)) {
                setAvailableOrders(response.data)
            } else {
                setError('Unexpected response format while fetching available orders.')
            }
        } catch (err) {
            console.error('Error fetching available orders:', err)
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
            const response = await drivers.updateAvailability(isOnline)
            console.log('Driver availability updated:', response)
        } catch (err){
            setError(response.data)
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
                            {filteredAvailable.map(order => (
                                <div key={order.id}>
                                    <div>
                                        <div style={{ fontWeight: 700 }}>{order.restaurant}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{order.items.join(", ")}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6, alignItems: "center" }}>
                                            <div >{formatDistance(order.distanceKm)}</div>
                                            <div >{order.etaMin} min</div>
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
                                            onClick={() => alert(`Order details:\nRestaurant: ${order.restaurant}\nCustomer: ${order.customer}\nItems: ${order.items.join(", ")}`)}
                                        >
                                            Details
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div>
                        <div>Your Assigned Deliveries</div>
                        <div>
                            {assignedOrders.length === 0 && <div>You have no assigned deliveries.</div>}
                            {assignedOrders.map(order => (
                                <div key={order.id} >
                                    <div>
                                        <div style={{ fontWeight: 700 }}>{order.restaurant} → {order.customer}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{order.items.join(", ")}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6 }}>
                                            <div >
                                                {order.status.replace("_", " ")}
                                            </div>
                                            <div style={{ color: "#6b7280", fontSize: 13 }}>{order.distanceKm ? formatDistance(order.distanceKm) : ""}</div>
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
                                        <button onClick={() => alert(`Contact ${order.customer}`)}>
                                            Contact
                                        </button>
                                    </div>
                                </div>
                            ))}
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
                                <div style={{ fontWeight: 700 }}>{driverLocation.lat.toFixed(4)}, {driverLocation.lng.toFixed(4)}</div>
                            </div>
                            <div>
                                <button
                                    onClick={() =>
                                        setDriverLocation({ lat: driverLocation.lat + (Math.random() - 0.5) * 0.01, lng: driverLocation.lng + (Math.random() - 0.5) * 0.01 })
                                    }
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