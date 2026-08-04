package ch02.item08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Item 8 검증용 테스트.
 *
 * <p>세 가지를 검증한다:
 * <ol>
 *   <li>try-with-resources 로 {@code close()} 가 자동 호출되는가</li>
 *   <li>{@code close()} 를 두 번 호출해도 안전한가 (idempotent)</li>
 *   <li>닫힌 뒤에 {@code use()} 를 호출하면 예외가 발생하는가</li>
 * </ol>
 *
 * <p>이 테스트를 통과하지 못한다면, close() 구현을 다시 살펴봐야 한다.
 */
@DisplayName("Item 8: finalizer 와 cleaner 사용을 피하라")
class Item08Test {

    // ========================================================
    // (1) try-with-resources: close() 자동 호출
    // ========================================================
    @Nested
    @DisplayName("try-with-resources — 블록을 벗어나면 close() 가 자동 호출된다")
    class TryWithResourcesTest {

        @Test
        @DisplayName("정상적으로 사용한 뒤 블록을 벗어나면 자원이 닫힌다")
        void autoClose_onNormalExit() {
            AutoCloseableRoom room;
            try (AutoCloseableRoom r = new AutoCloseableRoom("room-1")) {
                room = r;
                r.use();
                assertThat(r.isClosed())
                        .as("블록 안에 있는 동안은 아직 닫히지 않아야 한다")
                        .isFalse();
            }

            assertThat(room.isClosed())
                    .as("try-with-resources 블록을 벗어나면 close() 가 자동 호출되어야 한다")
                    .isTrue();
        }

        @Test
        @DisplayName("블록 안에서 예외가 발생해도 close() 는 반드시 호출된다")
        void autoClose_onException() {
            AutoCloseableRoom room = new AutoCloseableRoom("room-2");
            try (room) {   // Java 9+ — effectively final 변수를 try-with-resources 에 직접 전달
                throw new RuntimeException("자원 사용 중 오류 발생");
            } catch (RuntimeException ignored) {
                // 예외는 의도적으로 발생시킨 것 — close() 호출 여부가 관심사
            }

            assertThat(room.isClosed())
                    .as("예외가 발생해도 close() 는 호출되어야 한다 (try-with-resources 의 핵심)")
                    .isTrue();
        }
    }

    // ========================================================
    // (2) idempotent: close() 를 여러 번 호출해도 안전
    // ========================================================
    @Nested
    @DisplayName("close() 는 여러 번 호출해도 안전하다 (idempotent)")
    class IdempotentCloseTest {

        @Test
        @DisplayName("close() 를 두 번 호출해도 예외가 발생하지 않는다")
        void close_calledTwice_isSafe() {
            AutoCloseableRoom room = new AutoCloseableRoom("room-3");

            room.close();
            // 두 번째 close — 예외 없이 그냥 무시되어야 함
            room.close();

            assertThat(room.isClosed()).isTrue();
        }
    }

    // ========================================================
    // (3) 닫힌 뒤 사용 방지
    // ========================================================
    @Nested
    @DisplayName("닫힌 자원은 더 이상 사용할 수 없다")
    class UseAfterCloseTest {

        @Test
        @DisplayName("close() 후에 use() 를 호출하면 IllegalStateException")
        void use_afterClose_throws() {
            AutoCloseableRoom room = new AutoCloseableRoom("room-4");
            room.close();

            assertThatThrownBy(room::use)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("room-4");
        }
    }
}