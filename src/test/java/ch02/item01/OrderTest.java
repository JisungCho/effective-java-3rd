package ch02.item01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Item 1: 정적 팩터리 메서드 - Order")
class OrderTest {

    @Test
    @DisplayName("이름 있는 팩터리: pending/paid 는 의미를 명확히 전달한다")
    void namedFactoryMethods() {
        Order pending = Order.pending("ORD-1", 10_000L);
        Order paid = Order.paid("ORD-2", 25_000L);

        assertThat(pending.getStatus()).isEqualTo(Order.Status.PENDING);
        assertThat(paid.getStatus()).isEqualTo(Order.Status.PAID);
    }

    @Test
    @DisplayName("from 팩터리: 문자열을 Order 로 변환한다")
    void fromConverter() {
        Order order = Order.from("ORD-3, 30000, PAID");

        assertThat(order.getOrderId()).isEqualTo("ORD-3");
        assertThat(order.getAmount()).isEqualTo(30_000L);
        assertThat(order.getStatus()).isEqualTo(Order.Status.PAID);
    }

    @Test
    @DisplayName("amount 가 음수이면 예외를 던진다")
    void negativeAmount() {
        assertThatThrownBy(() -> Order.pending("ORD-X", -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    @DisplayName("TODO(human): 캐싱된 특수 인스턴스를 반환한다")
    void cachedSingletonInstance() {
        // given: OrderCancelled.getInstance() 같은 정적 팩터리가
        //        항상 동일한 캐시된 인스턴스를 반환해야 한다.
        //        사용자가 직접 구현할 예정.
    }
}