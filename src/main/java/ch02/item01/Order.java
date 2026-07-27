package ch02.item01;

import java.util.Objects;

public final class Order {

    private final String orderId;
    private final long amount;
    private final Status status;

    private Order(String orderId, long amount, Status status) {
        this.orderId = Objects.requireNonNull(orderId);
        if (amount < 0) {
            throw new IllegalArgumentException("amount는 음수일 수 없습니다: " + amount);
        }
        this.amount = amount;
        this.status = status;
    }

    public static Order pending(String orderId, long amount) {
        return new Order(orderId, amount, Status.PENDING);
    }

    public static Order paid(String orderId, long amount) {
        return new Order(orderId, amount, Status.PAID);
    }

    public static Order from(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("형식: orderId,amount,status");
        }
        return new Order(
                parts[0].trim(),
                Long.parseLong(parts[1].trim()),
                Status.valueOf(parts[2].trim().toUpperCase())
        );
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Order{" + orderId + ", amount=" + amount + ", status=" + status + '}';
    }

    public enum Status {
        PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    }
}
