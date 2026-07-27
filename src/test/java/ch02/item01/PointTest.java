package ch02.item01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item 1: 정적 팩터리 메서드 - Point (캐싱)")
class PointTest {

    @Test
    @DisplayName("origin() 은 항상 동일한 캐싱된 인스턴스를 반환한다")
    void cachedOrigin() {
        Point a = Point.origin();
        Point b = Point.origin();

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("of(...) 는 매번 새 인스턴스를 반환한다")
    void newInstanceEveryTime() {
        Point a = Point.of(0, 0);
        Point b = Point.of(0, 0);

        assertThat(a).isNotSameAs(b);
        assertThat(a.getX()).isEqualTo(b.getX());
        assertThat(a.getY()).isEqualTo(b.getY());
    }
}