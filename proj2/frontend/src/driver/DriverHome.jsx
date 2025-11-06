import React, { useEffect, useState } from "react";

/**
 * DriverHome.jsx
 * Home page for drivers in the Food Delivery App
 *
 * - Toggle online/offline
 * - See available orders (mock data)
 * - Accept orders, start pickup, complete delivery
 * - Simple status badges and actions
 * - Minimal inline styles so this can be dropped into the project quickly
 */

const styles = {
    container: { padding: 20, fontFamily: "Inter, Arial, sans-serif", maxWidth: 1100, margin: "0 auto" },
    header: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 },
    title: { fontSize: 24, fontWeight: 700 },
    statusButton: isOnline => ({
        padding: "8px 14px",
        borderRadius: 8,
        cursor: "pointer",
        border: "none",
        color: "white",
        background: isOnline ? "#16a34a" : "#ef4444",
    }),
    columns: { display: "grid", gridTemplateColumns: "1fr 420px", gap: 16 },
    panel: { background: "#fff", borderRadius: 8, padding: 12, boxShadow: "0 1px 3px rgba(0,0,0,0.06)" },
    sectionTitle: { fontSize: 16, marginBottom: 8, fontWeight: 600 },
    list: { display: "flex", flexDirection: "column", gap: 10 },
    orderCard: { border: "1px solid #e5e7eb", padding: 12, borderRadius: 8, display: "flex", justifyContent: "space-between", alignItems: "center" },
    meta: { display: "flex", flexDirection: "column", gap: 4 },
    actions: { display: "flex", gap: 8, alignItems: "center" },
    btn: { padding: "6px 10px", borderRadius: 6, cursor: "pointer", border: "none" },
    acceptBtn: { background: "#2563eb", color: "white" },
    neutralBtn: { background: "#f3f4f6" },
    mapPlaceholder: { height: 260, background: "#f8fafc", borderRadius: 8, display: "flex", alignItems: "center", justifyContent: "center", color: "#9ca3af" },
    smallBadge: bg => ({ background: bg, color: "white", padding: "4px 8px", borderRadius: 999, fontSize: 12 }),
    empty: { color: "#6b7280", fontStyle: "italic" }
};

function formatDistance(km) {
    return `${km.toFixed(1)} km`;
}

function uuid() {
    return Math.random().toString(36).slice(2, 9);
}

export default function DriverHome() {
    const [isOnline, setIsOnline] = useState(false);
    const [availableOrders, setAvailableOrders] = useState([]);
    const [assignedOrders, setAssignedOrders] = useState([]);
    const [driverLocation, setDriverLocation] = useState({ lat: 37.7749, lng: -122.4194 });
    const [filter, setFilter] = useState("all");

    // Mock: seed some available orders when component mounts
    useEffect(() => {
        const seed = [
            {
                id: uuid(),
                restaurant: "Pho Corner",
                customer: "Anna B.",
                items: ["Beef Pho", "Spring Rolls"],
                distanceKm: 1.4,
                etaMin: 12,
                status: "available",
            },
            {
                id: uuid(),
                restaurant: "Taqueria Azul",
                customer: "Jake P.",
                items: ["Carne Asada Tacos x3"],
                distanceKm: 3.2,
                etaMin: 20,
                status: "available",
            },
            {
                id: uuid(),
                restaurant: "Green Bowl",
                customer: "Maya R.",
                items: ["Buddha Bowl"],
                distanceKm: 0.9,
                etaMin: 9,
                status: "available",
            },
        ];
        setAvailableOrders(seed);
    }, []);

    // Simulate receiving a new nearby order when online
    useEffect(() => {
        if (!isOnline) return;
        const timer = setInterval(() => {
            const newOrder = {
                id: uuid(),
                restaurant: ["Sushi House", "Pizza Planet", "Curry Spot"][Math.floor(Math.random() * 3)],
                customer: ["Liam", "Olivia", "Noah", "Emma"][Math.floor(Math.random() * 4)],
                items: ["Assorted Items"],
                distanceKm: +(Math.random() * 5).toFixed(1),
                etaMin: Math.floor(8 + Math.random() * 20),
                status: "available",
            };
            setAvailableOrders(prev => [newOrder, ...prev]);
            // give a small browser notification (if permitted)
            if (window.Notification && Notification.permission === "granted") {
                new Notification("New nearby order", { body: `${newOrder.restaurant} • ${formatDistance(newOrder.distanceKm)}` });
            } else if (window.Notification && Notification.permission !== "denied") {
                Notification.requestPermission();
            }
        }, 20_000); // every 20s while online
        return () => clearInterval(timer);
    }, [isOnline]);

    function toggleOnline() {
        setIsOnline(v => !v);
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
        <div style={styles.container}>
            <div style={styles.header}>
                <div>
                    <div style={styles.title}>Driver Home</div>
                    <div style={{ color: "#6b7280", marginTop: 6 }}>Manage your deliveries and stay on the road</div>
                </div>
                <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                    <div style={{ textAlign: "right" }}>
                        <div style={{ fontSize: 12, color: "#6b7280" }}>Status</div>
                        <div style={{ marginTop: 6 }}>
                            <button style={styles.statusButton(isOnline)} onClick={toggleOnline}>
                                {isOnline ? "Online" : "Offline"}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div style={styles.columns}>
                <div>
                    <div style={{ ...styles.panel, marginBottom: 12 }}>
                        <div style={styles.sectionTitle}>Available Orders</div>
                        <div style={{ display: "flex", gap: 8, marginBottom: 10, alignItems: "center" }}>
                            <select value={filter} onChange={e => setFilter(e.target.value)} style={{ padding: 8, borderRadius: 6 }}>
                                <option value="all">All</option>
                                <option value="nearby">Nearby (&le; 2.5 km)</option>
                                <option value="fast">Fast (&le; 12 min)</option>
                            </select>
                            <div style={{ color: "#6b7280", fontSize: 13 }}>{filteredAvailable.length} result(s)</div>
                        </div>

                        <div style={styles.list}>
                            {filteredAvailable.length === 0 && <div style={styles.empty}>No available orders at the moment.</div>}
                            {filteredAvailable.map(order => (
                                <div key={order.id} style={styles.orderCard}>
                                    <div style={styles.meta}>
                                        <div style={{ fontWeight: 700 }}>{order.restaurant}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{order.items.join(", ")}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6, alignItems: "center" }}>
                                            <div style={styles.smallBadge("#111827")}>{formatDistance(order.distanceKm)}</div>
                                            <div style={styles.smallBadge("#6b7280")}>{order.etaMin} min</div>
                                        </div>
                                    </div>

                                    <div style={styles.actions}>
                                        <button
                                            style={{ ...styles.btn, ...styles.acceptBtn }}
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
                                            style={{ ...styles.btn, ...styles.neutralBtn }}
                                            onClick={() => alert(`Order details:\nRestaurant: ${order.restaurant}\nCustomer: ${order.customer}\nItems: ${order.items.join(", ")}`)}
                                        >
                                            Details
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div style={styles.panel}>
                        <div style={styles.sectionTitle}>Your Assigned Deliveries</div>
                        <div style={styles.list}>
                            {assignedOrders.length === 0 && <div style={styles.empty}>You have no assigned deliveries.</div>}
                            {assignedOrders.map(order => (
                                <div key={order.id} style={styles.orderCard}>
                                    <div style={styles.meta}>
                                        <div style={{ fontWeight: 700 }}>{order.restaurant} → {order.customer}</div>
                                        <div style={{ color: "#6b7280", fontSize: 13 }}>{order.items.join(", ")}</div>
                                        <div style={{ display: "flex", gap: 8, marginTop: 6 }}>
                                            <div style={styles.smallBadge(order.status === "accepted" ? "#2563eb" : order.status === "picking_up" ? "#f59e0b" : order.status === "on_route" ? "#10b981" : "#6b7280")}>
                                                {order.status.replace("_", " ")}
                                            </div>
                                            <div style={{ color: "#6b7280", fontSize: 13 }}>{order.distanceKm ? formatDistance(order.distanceKm) : ""}</div>
                                        </div>
                                    </div>

                                    <div style={styles.actions}>
                                        {order.status === "accepted" && (
                                            <button style={{ ...styles.btn, ...styles.acceptBtn }} onClick={() => startPickup(order.id)}>
                                                Start Pickup
                                            </button>
                                        )}
                                        {order.status === "picking_up" && (
                                            <button style={{ ...styles.btn, background: "#f97316", color: "white" }} onClick={() => markPickedUp(order.id)}>
                                                Mark Picked Up
                                            </button>
                                        )}
                                        {order.status === "on_route" && (
                                            <button style={{ ...styles.btn, background: "#059669", color: "white" }} onClick={() => completeDelivery(order.id)}>
                                                Complete
                                            </button>
                                        )}
                                        <button style={{ ...styles.btn, ...styles.neutralBtn }} onClick={() => alert(`Contact ${order.customer}`)}>
                                            Contact
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <div>
                    <div style={styles.panel}>
                        <div style={styles.sectionTitle}>Map / Navigation</div>
                        <div style={styles.mapPlaceholder}>
                            Map placeholder — integrate your maps provider here
                        </div>
                        <div style={{ marginTop: 12, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                            <div>
                                <div style={{ fontSize: 13, color: "#6b7280" }}>Current location</div>
                                <div style={{ fontWeight: 700 }}>{driverLocation.lat.toFixed(4)}, {driverLocation.lng.toFixed(4)}</div>
                            </div>
                            <div>
                                <button
                                    style={{ ...styles.btn, ...styles.neutralBtn }}
                                    onClick={() =>
                                        setDriverLocation({ lat: driverLocation.lat + (Math.random() - 0.5) * 0.01, lng: driverLocation.lng + (Math.random() - 0.5) * 0.01 })
                                    }
                                >
                                    Update Location
                                </button>
                            </div>
                        </div>
                    </div>

                    <div style={{ ...styles.panel, marginTop: 12 }}>
                        <div style={styles.sectionTitle}>Quick Stats</div>
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
    );
}