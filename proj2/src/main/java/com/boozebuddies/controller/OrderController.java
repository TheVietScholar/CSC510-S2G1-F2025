package com.boozebuddies.controller;

import com.boozebuddies.dto.ApiResponse;
import com.boozebuddies.dto.OrderDTO;
import com.boozebuddies.dto.CreateOrderRequest;
import com.boozebuddies.entity.Order;
import com.boozebuddies.entity.User;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.OrderItem;
import com.boozebuddies.model.OrderStatus;
import com.boozebuddies.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Create a new order
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        try {
            // Convert CreateOrderRequest to Order entity
            Order order = convertToEntity(createOrderRequest);
            Order createdOrder = orderService.createOrder(order);
            OrderDTO orderDTO = convertToDTO(createdOrder);
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            OrderDTO orderDTO = convertToDTO(orderOpt.get());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    /**
     * Get all orders for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUser(userId);
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs, "User orders retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve user orders: " + e.getMessage()));
        }
    }

    /**
     * Get all orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(orderDTOs, "All orders retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long orderId) {
        try {
            Order cancelledOrder = orderService.cancelOrder(orderId);
            OrderDTO orderDTO = convertToDTO(cancelledOrder);
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            OrderDTO orderDTO = convertToDTO(updatedOrder);
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Convert CreateOrderRequest to Order entity
     */
    private Order convertToEntity(CreateOrderRequest createOrderRequest) {
        Order order = new Order();
        
        // Set user (you would fetch from repository in real implementation)
        User user = new User();
        user.setId(createOrderRequest.getUserId());
        order.setUser(user);
        
        // Set merchant (you would fetch from repository in real implementation)
        Merchant merchant = new Merchant();
        merchant.setId(createOrderRequest.getMerchantId());
        order.setMerchant(merchant);
        
        order.setDeliveryAddress(createOrderRequest.getDeliveryAddress());
        order.setSpecialInstructions(createOrderRequest.getSpecialInstructions());
        
        // Convert order items
        if (createOrderRequest.getItems() != null) {
            List<OrderItem> orderItems = createOrderRequest.getItems().stream()
                    .map(itemRequest -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        // Set product (you would fetch from repository)
                        // orderItem.setProduct(productRepository.findById(itemRequest.getProductId()).orElseThrow());
                        orderItem.setQuantity(itemRequest.getQuantity());
                        orderItem.setUnitPrice(itemRequest.getUnitPrice());
                        return orderItem;
                    })
                    .collect(Collectors.toList());
            order.setItems(orderItems);
        }
        
        return order;
    }

    /**
     * Convert Order entity to OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .merchantId(order.getMerchant() != null ? order.getMerchant().getId() : null)
                .driverId(order.getDriver() != null ? order.getDriver().getId() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                // Note: You might want to convert order items to OrderItemDTOs as well
                .build();
    }
}