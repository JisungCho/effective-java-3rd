package ch02.item08;

/**
 * Item 8 올바른 예: {@link AutoCloseable} + try-with-resources 로
 * 명시적으로 자원을 회수한다.
 *
 * <p>클라이언트가 {@code close()} 를 호출하면 (또는 try-with-resources 블록을
 * 벗어나면) 즉시 자원이 해제된다. finalizer 와 달리 "언제 해제되는지" 가 명확하다.
 *
 * <p>핵심 {@code close()} 구현은 학습자가 직접 완성했다.
 */
public final class AutoCloseableRoom implements AutoCloseable {

    private final String name;
    private boolean closed = false;

    public AutoCloseableRoom(String name) {
        this.name = name;
    }

    /**
     * 자원을 사용한다 (시뮬레이션).
     *
     * @throws IllegalStateException 이미 닫힌 뒤에 호출하면
     */
    public void use() {
        if (closed) {
            throw new IllegalStateException("이미 닫힌 자원은 사용할 수 없습니다: " + name);
        }
        // 실제 자원 사용 로직 (여기서는 아무 일도 안 함 — 상태만 추적)
    }

    public boolean isClosed() {
        return closed;
    }

    public String getName() {
        return name;
    }

    /**
     * 자원을 해제한다.
     *
     * <p>try-with-resources 를 사용하면 이 메서드가 블록을 벗어날 때 자동으로 호출된다.
     * 이것이 Item 8 의 핵심 — "자원 해제 시점을 명확하게" 만드는 장치다.
     *
     * <p>요구사항:
     * <ul>
     *   <li>(1) 이미 닫혀 있으면 아무 일도 하지 않는다 (idempotent — 여러 번 close 해도 안전).</li>
     *   <li>(2) {@code closed} 플래그를 {@code true} 로 설정한다.</li>
     * </ul>
     */
    @Override
    public void close() {
        if (closed) {
            return;   // 이미 닫혀 있으면 아무 일도 하지 않는다 (idempotent)
        }
        closed = true;
    }
}