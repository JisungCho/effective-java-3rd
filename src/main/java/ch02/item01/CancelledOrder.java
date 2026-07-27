package ch02.item01;

public final class CancelledOrder {

    private static final String REASON = "사용자 요청에 의한 취소";

    private final String orderId;
    private final long refundAmount;

    private CancelledOrder(String orderId, long refundAmount) {
        this.orderId = orderId;
        this.refundAmount = refundAmount;
    }

    private static final CancelledOrder INSTANCE = new CancelledOrder("ORD-1", 10_000L);

    public static CancelledOrder cancelled(String orderId, long refundAmount) {
        return INSTANCE;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getRefundAmount() {
        return refundAmount;
    }

    public String getReason() {
        return REASON;
    }
}