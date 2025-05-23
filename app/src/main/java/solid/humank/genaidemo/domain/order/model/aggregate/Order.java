package solid.humank.genaidemo.domain.order.model.aggregate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import solid.humank.genaidemo.domain.common.annotations.AggregateRoot;
import solid.humank.genaidemo.domain.common.service.DomainEventPublisherService;
import solid.humank.genaidemo.domain.common.valueobject.Money;
import solid.humank.genaidemo.domain.common.valueobject.OrderId;
import solid.humank.genaidemo.domain.common.valueobject.OrderItem;
import solid.humank.genaidemo.domain.common.valueobject.OrderStatus;
import solid.humank.genaidemo.domain.order.model.events.OrderCreatedEvent;
import solid.humank.genaidemo.domain.order.model.events.OrderItemAddedEvent;
import solid.humank.genaidemo.utils.Preconditions;

/**
 * 訂單聚合根
 * 封裝訂單相關的業務規則和行為
 */
@AggregateRoot
public class Order {
    private final OrderId id;
    private final String customerId;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;
    private Money effectiveAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 建立訂單
     * 
     * @param customerId 客戶ID
     * @param shippingAddress 配送地址
     */
    public Order(String customerId, String shippingAddress) {
        this(OrderId.generate(), customerId, shippingAddress);
    }

    /**
     * 建立訂單
     * 
     * @param orderId 訂單ID
     * @param customerId 客戶ID
     * @param shippingAddress 配送地址
     */
    public Order(OrderId orderId, String customerId, String shippingAddress) {
        Preconditions.requireNonNull(orderId, "訂單ID不能為空");
        Preconditions.requireNonEmpty(customerId, "客戶ID不能為空");
        Preconditions.requireNonEmpty(shippingAddress, "配送地址不能為空");

        this.id = orderId;
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.totalAmount = Money.zero();
        this.effectiveAmount = Money.zero();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        // 發布領域事件
        DomainEventPublisherService.publishEvent(new OrderCreatedEvent(
            this.id,
            this.customerId,
            Money.zero(),
            List.of()
        ));
    }

    /**
     * 建立訂單
     * 
     * @param orderId 訂單ID字符串
     */
    public Order(String orderId) {
        this(OrderId.of(orderId), "customer-123", "台北市信義區");
    }

    /**
     * 添加訂單項
     * 
     * @param productId 產品ID
     * @param productName 產品名稱
     * @param quantity 數量
     * @param price 價格
     */
    public void addItem(String productId, String productName, int quantity, Money price) {
        // 檢查訂單狀態
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items to an order that is not in CREATED state");
        }

        // 創建訂單項
        OrderItem item = new OrderItem(productId, productName, quantity, price);
        items.add(item);

        // 更新總金額
        totalAmount = totalAmount.add(item.getSubtotal());
        effectiveAmount = totalAmount;
        updatedAt = LocalDateTime.now();

        // 發布領域事件
        DomainEventPublisherService.publishEvent(new OrderItemAddedEvent(
            this.id,
            productId,
            quantity,
            price
        ));
    }

    /**
     * 提交訂單
     */
    public void submit() {
        // 檢查訂單項
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot submit an order with no items");
        }

        // 更新狀態
        status = OrderStatus.PENDING;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 確認訂單
     */
    public void confirm() {
        // 檢查狀態轉換
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Cannot confirm an order in " + status + " state");
        }

        // 更新狀態
        status = OrderStatus.CONFIRMED;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 標記為已付款
     */
    public void markAsPaid() {
        // 檢查狀態轉換
        if (!status.canTransitionTo(OrderStatus.PAID)) {
            throw new IllegalStateException("Cannot mark as paid an order in " + status + " state");
        }

        // 更新狀態
        status = OrderStatus.PAID;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 發貨
     */
    public void ship() {
        // 檢查狀態轉換
        if (!status.canTransitionTo(OrderStatus.SHIPPING)) {
            throw new IllegalStateException("Cannot ship an order in " + status + " state");
        }

        // 更新狀態
        status = OrderStatus.SHIPPING;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 送達
     */
    public void deliver() {
        // 檢查狀態轉換
        if (!status.canTransitionTo(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("Cannot deliver an order in " + status + " state");
        }

        // 更新狀態
        status = OrderStatus.DELIVERED;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 取消訂單
     */
    public void cancel() {
        // 檢查狀態
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an order that is already delivered or cancelled");
        }

        // 更新狀態
        status = OrderStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

    /**
     * 處理訂單
     * 執行訂單處理流程
     */
    public void process() {
        // 檢查狀態
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有處於建立狀態的訂單可以處理");
        }

        // 提交訂單
        submit();
    }

    /**
     * 應用折扣
     * 
     * @param discountedAmount 折扣後金額
     */
    public void applyDiscount(Money discountedAmount) {
        // 檢查折扣金額
        if (discountedAmount.isGreaterThan(totalAmount)) {
            throw new IllegalArgumentException("折扣金額不能大於訂單總金額");
        }

        // 更新有效金額
        effectiveAmount = totalAmount.subtract(discountedAmount);
        updatedAt = LocalDateTime.now();
    }

    // Getters

    public OrderId getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Money getEffectiveAmount() {
        return effectiveAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // 重寫 equals 和 hashCode 方法

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", items=" + items.size() +
                '}';
    }
}