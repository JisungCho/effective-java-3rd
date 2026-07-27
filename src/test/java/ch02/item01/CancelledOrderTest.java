package ch02.item01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item 1: 정적 팩터리 메서드 - CancelledOrder (캐싱)")
class CancelledOrderTest {

    @Test
    @DisplayName("cancelled(...) 는 매개변수와 무관하게 항상 동일한 인스턴스를 반환한다")
    void cachedSingleton() {
        CancelledOrder a = CancelledOrder.cancelled("ORD-1", 10_000L);
        CancelledOrder b = CancelledOrder.cancelled("ORD-999", 5L);

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("캐싱된 인스턴스의 상태는 고정되어 있다")
    void fixedState() {
        CancelledOrder order = CancelledOrder.cancelled("WHATEVER", 0L);

        assertThat(order.getReason()).isEqualTo("사용자 요청에 의한 취소");
    }
}