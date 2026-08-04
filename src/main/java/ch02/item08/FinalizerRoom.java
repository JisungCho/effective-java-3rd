package ch02.item08;

import java.util.ArrayList;
import java.util.List;

/**
 * Item 8 잘못된 예: finalize() 에 자원 회수를 맡긴다.
 *
 * <p>finalize() 는 GC 가 객체를 회수하기 전에 호출되지만,
 * <ul>
 *   <li>(1) <b>언제</b> 호출될지 알 수 없고,</li>
 *   <li>(2) 아예 <b>호출되지 않을 수도</b> 있다.</li>
 * </ul>
 * 따라서 파일 디스크립터, 락, 네이티브 자원처럼
 * "빨리 회수해야 하는" 자원에는 finalizer 를 쓰면 안 된다.
 *
 * <p>이 클래스는 {@link AutoCloseableRoom} 과
 * "기능은 같지만 회수 시점의 예측 가능성이 다른" 대조군 역할을 한다.
 *
 * <p>주의: educational 목적으로만 finalize() 를 사용한다.
 * Java 9 부터 finalize() 는 deprecated 되었다.
 */
public final class FinalizerRoom {

    /** 정적 레지스트리 — 어느 방이 청소되었는지 추적 (테스트용). */
    private static final List<String> CLEANED_ROOMS = new ArrayList<>();

    private final String name;
    private boolean cleaned = false;

    public FinalizerRoom(String name) {
        this.name = name;
    }

    /**
     * 자원을 해제한다 (시뮬레이션). 실제 프로제트라면 파일 닫기, 락 해제 등.
     */
    void cleanUp() {
        if (!cleaned) {
            CLEANED_ROOMS.add(name);
            cleaned = true;
        }
    }

    public boolean isCleaned() {
        return cleaned;
    }

    public String getName() {
        return name;
    }

    /**
     * @return 지금까지 청소된 방 이름 목록 (테스트 관찰용)
     */
    public static List<String> getCleanedRooms() {
        return new ArrayList<>(CLEANED_ROOMS);
    }

    /**
     * finalizer — GC 가 객체를 회수하기 전에 호출될 수도 있고, 아닐 수도 있다.
     * 절대 "반드시 실행되어야 하는" 자원 회수를 여기에 두면 안 된다.
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        try {
            cleanUp();
        } finally {
            super.finalize();
        }
    }
}