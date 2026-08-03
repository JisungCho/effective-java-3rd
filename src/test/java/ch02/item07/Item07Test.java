package ch02.item07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Item 7 검증용 테스트.
 *
 * <p>두 가지를 검증한다:
 * <ol>
 *   <li>{@link Stack} 의 pop() 이 "기능적으로" 올바른가 — 넣은 원소를 올바른 순서로 꺼내는가</li>
 *   <li>{@link Stack} 의 pop() 이 "Item 7 답게" 다 쓴 참조를 해제하는가 — 꺼낸 자리가 null 이 되는가</li>
 * </ol>
 *
 * <p>이 테스트를 통과하지 못한다면, pop() 구현을 다시 살펴봐야 한다.
 * 특히 "어떤 인덱스의 원소를 꺼내야 하는가"와 "어떤 자리를 null 로 만들어야 하는가"를
 * 놓치기 쉽다.
 */
@DisplayName("Item 7: 다 쓴 객체 참조를 해제하라")
class Item07Test {

    // ========================================================
    // (1) 기능 정확성: pop() 이 올바른 원소를 올바른 순서로 꺼내는가
    // ========================================================
    @Nested
    @DisplayName("Stack 기본 동작 — LIFO")
    class StackBasicTest {

        @Test
        @DisplayName("push 한 원소를 pop 으로 꺼낼 수 있다")
        void pop_returnsPushedElement() {
            Stack stack = new Stack();
            stack.push("first");
            stack.push("second");

            Object popped = stack.pop();

            // LIFO: 마지막에 넣은 "second" 가 나와야 한다
            assertThat(popped)
                    .as("pop() 은 마지막에 push 한 원소를 반환해야 한다")
                    .isEqualTo("second");
        }

        @Test
        @DisplayName("여러 번 push/pop 할 때 LIFO 순서를 지킨다")
        void pop_respectsLifoOrder() {
            Stack stack = new Stack();
            stack.push(1);
            stack.push(2);
            stack.push(3);

            assertThat(stack.pop()).isEqualTo(3);
            assertThat(stack.pop()).isEqualTo(2);
            assertThat(stack.pop()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 스택에서 pop 하면 EmptyStackException")
        void pop_onEmpty_throwsEmptyStackException() {
            Stack stack = new Stack();
            assertThatThrownBy(stack::pop)
                    .isInstanceOf(EmptyStackException.class);
        }

        @Test
        @DisplayName("size 는 push/pop 에 맞게 변한다")
        void size_reflectsPushPop() {
            Stack stack = new Stack();
            assertThat(stack.size()).isEqualTo(0);

            stack.push("a");
            stack.push("b");
            assertThat(stack.size()).isEqualTo(2);

            stack.pop();
            assertThat(stack.size()).isEqualTo(1);
        }
    }

    // ========================================================
    // (2) Item 7 핵심: 다 쓴 참조를 해제했는가
    // ========================================================
    @Nested
    @DisplayName("Stack — pop() 후 참조 해제 여부 (Item 7 핵심)")
    class StackReferenceReleaseTest {

        @Test
        @DisplayName("pop 한 뒤 해당 슬롯은 null 이 된다")
        void pop_nullsOutSlot() {
            Stack stack = new Stack();
            stack.push("keep");
            stack.push("gone");      // 이 원소가 pop 될 것

            int topIndex = stack.size() - 1;   // pop 직전 꼭대기 인덱스 = 1
            stack.pop();

            // pop 직전 꼭대기였던 자리(인덱스 1)는 null 이어야 GC 가 회수할 수 있다.
            assertThat(stack.elementAt(topIndex))
                    .as("pop() 후 해당 슬롯은 null 이어야 한다 (다 쓴 참조 해제)")
                    .isNull();
        }

        @Test
        @DisplayName("pop 해도 남아있는 원소는 그대로 살아있어야 한다")
        void pop_doesNotCorruptOtherElements() {
            Stack stack = new Stack();
            stack.push("a");
            stack.push("b");
            stack.push("c");

            stack.pop();   // "c" 제거

            // "a", "b" 는 여전히 살아있어야 한다
            assertThat(stack.elementAt(0)).isEqualTo("a");
            assertThat(stack.elementAt(1)).isEqualTo("b");
        }
    }

    // ========================================================
    // (3) 대조군: LeakyStack 은 일부러 참조를 남긴다
    // ========================================================
    @Nested
    @DisplayName("LeakyStack — 참조를 해제하지 않는 대조군")
    class LeakyStackTest {

        @Test
        @DisplayName("LeakyStack 은 pop 후에도 슬롯이 원소를 참조한다 (메모리 누수)")
        void leakyStack_keepsReferenceAfterPop() {
            LeakyStack stack = new LeakyStack();
            stack.push("gone");
            int topIndex = stack.size() - 1;
            stack.pop();

            // LeakyStack 은 일부러 참조를 남긴다 — 이것이 "메모리 누수"의 모습이다.
            assertThat(stack.elementAt(topIndex))
                    .as("LeakyStack 은 참조를 해제하지 않는다 (대조군)")
                    .isEqualTo("gone");
        }
    }
}