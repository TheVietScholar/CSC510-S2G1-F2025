package com.boozebuddies.service;

import com.boozebuddies.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    /**
     * Creates a new order, processes payment, creates a delivery record,
     * and sends notifications.
     *
     * @param order The order to create.
     * @return The created order.
     */
    Order createOrder(Order order);

    /**
     * Retrieves an order by its unique ID.
     *
     * @param id The order ID.
     * @return An Optional containing the order if found, otherwise empty.
     */
    Optional<Order> getOrderById(Long id);

    /**
     * Retrieves all orders placed by a specific user.
     *
     * @param userId The user's ID.
     * @return A list of orders associated with the user.
     */
    List<Order> getOrdersByUser(Long userId);

    /**
     * Retrieves all orders in the system.
     *
     * @return A list of all orders.
     */
    List<Order> getAllOrders();

    /**
     * Cancels an order, processes refund, and sends notifications.
     *
     * @param orderId The ID of the order to cancel.
     * @return The cancelled order.
     */
    Order cancelOrder(Long orderId);

    /**
     * Updates the status of an order (e.g., CONFIRMED, PREPARING, COMPLETED).
     *
     * @param orderId The ID of the order to update.
     * @param status The new status as a string.
     * @return The updated order.
     */
    Order updateOrderStatus(Long orderId, String status);
}