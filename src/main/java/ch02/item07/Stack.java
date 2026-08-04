package ch02.item07;

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * Item 7 올바른 예: 다 쓴 객체 참조를 해제하는 스택.
 *
 * <p>{@code pop()} 은 원소를 꺼낸 뒤, 그 원소를 가리키던 배열 슬롯을
 * {@code null} 로 덮어쓴다. 그래야 GC 가 "이 객체는 더 쓸모없다"고 판단해
 * 회수할 수 있다.
 *
 * <p>핵심 {@code pop()} 구현은 학습자가 직접 완성했다.
 */
public final class Stack {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private Object[] elements;
    private int size = 0;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * 원소를 스택 꼭대기에 넣는다.
     *
     * @param e 넣을 원소
     */
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    /**
     * 스택 꼭대기에서 원소를 꺼낸다.
     *
     * <p>주의: 단순히 {@code size} 를 줄이는 것만으로는 부족하다.
     * 배열 슬롯이 여전히 꺼낸 원소를 참조하고 있어, GC 가 회수하지 못한다.
     * 원소를 꺼낸 뒤에는 그 슬롯을 반드시 {@code null} 로 덮어써야 한다.
     *
     * @return 스택 꼭대기의 원소
     * @throws EmptyStackException 스택이 비어 있을 때
     */
    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        size--;
        Object result = elements[size];
        elements[size] = null;   // 다 쓴 참조 해제 — GC 가 회수할 수 있게
        return result;
    }

    /**
     * @return 현재 스택에 들어있는 원소 수
     */
    public int size() {
        return size;
    }

    /**
     * (보조) 테스트·검증용 — 특정 배열 슬롯의 현재 값을 반환한다.
     *
     * <p>이 메서드는 {@code pop()} 이 참조를 제대로 해제했는지
     * 외부에서 관찰하기 위한 용도로만 쓴다. 실제 스택 API 라면
     * 내부 배열을 노출하지 않는 것이 맞다 (Item 13, Item 50 참고).
     *
     * @param index 배열 인덱스 (0 이상)
     * @return 해당 슬롯의 값. 비어 있으면 null
     */
    Object elementAt(int index) {
        return elements[index];
    }

    /**
     * 용량이 꽉 차면 배열을 2배로 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}