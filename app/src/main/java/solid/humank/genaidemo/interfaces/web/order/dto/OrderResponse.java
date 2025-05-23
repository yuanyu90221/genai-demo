package solid.humank.genaidemo.interfaces.web.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import solid.humank.genaidemo.domain.order.model.aggregate.Order;
import solid.humank.genaidemo.domain.common.valueobject.Money;
import solid.humank.genaidemo.domain.common.valueobject.OrderStatus;
import solid.humank.genaidemo.domain.common.valueobject.OrderItem;

/**
 * 訂單響應 DTO
 */
public class OrderResponse {
    private final String orderId;
    private final String customerId;
    private final String shippingAddress;
    private final List<WebOrderItemResponse> items;
    private final Money totalAmount;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public OrderResponse(String orderId, String customerId, String shippingAddress,
                        List<WebOrderItemResponse> items, Money totalAmount, OrderStatus status,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderResponse fromOrder(Order order) {
        List<WebOrderItemResponse> items = order.getItems().stream()
                .map(WebOrderItemResponse::fromOrderItem)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId().toString(),
                order.getCustomerId(),
                order.getShippingAddress(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
    
    public static OrderResponse fromDomain(Order order) {
        return fromOrder(order);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public List<WebOrderItemResponse> getItems() {
        return items;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 介面層訂單項目響應DTO
     */
    public static class WebOrderItemResponse {
        private final String productId;
        private final String productName;
        private final int quantity;
        private final Money price;
        private final Money subtotal;

        private WebOrderItemResponse(String productId, String productName, int quantity, Money price, Money subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }

        public static WebOrderItemResponse fromOrderItem(OrderItem item) {
            return new WebOrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getSubtotal()
            );
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public Money getPrice() {
            return price;
        }

        public Money getSubtotal() {
            return subtotal;
        }
    }
}