package solid.humank.genaidemo.domain.payment.events;

import java.time.Instant;
import java.util.UUID;

import solid.humank.genaidemo.domain.common.events.DomainEvent;
import solid.humank.genaidemo.domain.common.valueobject.Money;

/**
 * 支付請求事件
 */
public class PaymentRequestedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final String eventType;
    private final UUID paymentId;
    private final String orderId;
    private final Money amount;
    
    public PaymentRequestedEvent(UUID paymentId, String orderId, Money amount) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.eventType = "PaymentRequested";
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
    }
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public String getEventType() {
        return eventType;
    }
    
    public UUID getPaymentId() {
        return paymentId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public Money getAmount() {
        return amount;
    }
}