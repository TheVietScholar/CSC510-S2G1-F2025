//Need to figoure out imports and packages
package com.boozebuddies.delivery;

public class Delivery {

	private Long deliveryId;
	private Order order;
	private Driver driver;
	private DeliveryStatus status;
	// TODO need to figure out what to do with current location
	// Put Integer for now
	// private Integer currentLocation;
	// private String cancellationReason;

	public Delivery(Long deliveryId, Order order) {
		this.deliveryId = deliveryId;
		this.order = order;
		this.status = DeliveryStatus.PENDING;
	}

	// === Getters and Setters ===
	public Long getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(Long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public DeliveryStatus getStatus() {
		return status;
	}

	public void setStatus(DeliveryStatus status) {
		this.status = status;
	}

	// public Location getCurrentLocation() {
	// return currentLocation;
	// }

	// public void setCurrentLocation(Integer currentLocation) {
	// this.currentLocation = currentLocation;
	// }

	// public String getCancellationReason() {
	// return cancellationReason;
	// }

	// public void setCancellationReason(String cancellationReason) {
	// this.cancellationReason = cancellationReason;
	// }

	// === Core Functions ===
	public void assignDriver(Driver driver) {
		if (this.status != DeliveryStatus.PENDING) {
			throw new IllegalStateException("Driver can only be assigned when delivery is pending.");
		}
		setDriver(driver);
		setStatus(DeliveryStatus.ASSIGNED);
	}

	public void startDelivery() {
		if (status == DeliveryStatus.ASSIGNED) {
			status = DeliveryStatus.IN_TRANSIT;
		} else {
			throw new IllegalStateException("Delivery must be assigned before starting.");
		}
	}

	// public void updateLocation(Integer newLocation) {
	// this.currentLocation = newLocation;
	// }

	public void markDelivered() {
		if (status == DeliveryStatus.IN_TRANSIT) {
			setStatus(DeliveryStatus.DELIVERED);
		} else {
			throw new IllegalStateException("Cannot mark delivery as delivered unless it's in transit.");
		}
	}

	public void cancelDelivery(String reason) {
		if (status == DeliveryStatus.DELIVERED) {
			throw new IllegalStateException("Cannot cancel a completed delivery.");
		}
		setStatus(DeliveryStatus.CANCELLED);
		// this.cancellationReason = reason;
	}

	public boolean canBeCancelled() {
		return status != DeliveryStatus.DELIVERED && status != DeliveryStatus.CANCELLED;
	}

	public boolean onTheWay() {
		return status == DeliveryStatus.IN_TRANSIT;
	}

	public String getDeliverySummary() {
		return "Delivery #" + deliveryId + " - " + status + " | Driver: "
				+ (driver != null ? driver.getDriverId() : "Unassigned") + " | Order: "
				+ (order != null ? order.getOrderId() : "N/A");
	}

	@Override
	public String toString() {
		return "Delivery{" + "deliveryId=" + getDeliveryId() + ", order=" + getOrder() + ", driver=" + getDriver()
				+ ", status=" + getStatus() + '\'' + '}';
	}
}
