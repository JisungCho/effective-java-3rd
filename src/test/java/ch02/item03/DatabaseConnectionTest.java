package ch02.item03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item 3: 싱글턴 패턴 비교")
class DatabaseConnectionTest {

    @Test
    @DisplayName("방법 1: public static final 필드 — 동일 인스턴스")
    void publicFinalSingleton() {
        DatabaseConnectionPublicFinal a = DatabaseConnectionPublicFinal.INSTANCE;
        DatabaseConnectionPublicFinal b = DatabaseConnectionPublicFinal.INSTANCE;

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("방법 2: 정적 팩터리 메서드 — 동일 인스턴스")
    void factorySingleton() {
        DatabaseConnectionFactory a = DatabaseConnectionFactory.getInstance();
        DatabaseConnectionFactory b = DatabaseConnectionFactory.getInstance();

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("방법 3: 열거 타입 — 동일 인스턴스")
    void enumSingleton() {
        DatabaseConnectionEnum a = DatabaseConnectionEnum.INSTANCE;
        DatabaseConnectionEnum b = DatabaseConnectionEnum.INSTANCE;

        assertThat(a).isSameAs(b);
    }

    @Test
    @DisplayName("TODO(human): enum 싱글턴의 상태는 전역으로 누적된다")
    void enumStateAccumulates() {
        // given: query() 를 여러 번 호출하면
        DatabaseConnectionEnum.INSTANCE.query("SELECT 1");
        DatabaseConnectionEnum.INSTANCE.query("SELECT 2");
        DatabaseConnectionEnum.INSTANCE.query("SELECT 3");

        // then: queryCount 는 3이어야 한다 (전역 상태)
        assertThat(DatabaseConnectionEnum.INSTANCE.getQueryCount()).isEqualTo(3);
    }
}