package ch02.item07;

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * Item 7 잘못된 예: 다 쓴 객체 참조를 해제하지 않는 스택.
 *
 * <p>메모리 누수(memory leak)의 대표적인 사례.
 * {@code pop()} 은 size 만 줄일 뿐, 배열 슬롯이 여전히 꺼낸 객체를 참조한다.
 * 그래서 GC 가 그 객체를 회수하지 못한다.
 *
 * <p>스택이 커졌다가 줄어들면, 줄어든 만큼의 객체가 "다 썼는데도" 살아있게 된다.
 * 최대 크기만큼의 객체가 쓰레기로 쌓이는 셈이다.
 *
 * <p>이 클래스는 {@link Stack} 과 "기능은 같지만 메모리 관리는 다른"
 * 대조군 역할을 한다.
 */
public final class LeakyStack {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private Object[] elements;
    private int size = 0;

    public LeakyStack() {
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
     * <p>문제점: {@code elements[--size]} 가 반환된 뒤에도
     * 해당 배열 슬롯은 여전히 꺼낸 객체를 가리키고 있다.
     * 이 참조가 남아있는 한 GC 는 그 객체를 회수하지 못한다.
     *
     * @return 스택 꼭대기의 원소
     * @throws EmptyStackException 스택이 비어 있을 때
     */
    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];   // ← 참조를 해제하지 않는다 (메모리 누수)
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
     * <p>{@code pop()} 이 참조를 해제했는지 외부에서 관찰하기 위한 용도로만 쓴다.
     *
     * @param index 배열 인덱스 (0 이상)
     * @return 해당 슬롯의 값. 비어 있으면 null
     */
    Object elementAt(int index) {
        return elements[index];
    }

    /**
     * 용량이 꽉 차면 배열을 2배로 늘린다.
     * 늘린 배열의 남는 슬롯은 여전히 null 이지만,
     * 줄어들 때(null 처리 없이) 제거된 원소들이 그대로 남는다.
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}