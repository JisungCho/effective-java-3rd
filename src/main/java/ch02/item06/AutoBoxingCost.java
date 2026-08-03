package ch02.item06;

/**
 * Item 6 보조 예제: 오토박싱(auto-boxing)이 만드는 숨은 객체.
 *
 * <p>원시 타입(long)과 래퍼 타입(Long)을 섞어 쓰면, 컴파일러가
 * 눈에 보이지 않게 Long 인스턴스를 계속 새로 만든다.
 * "불필요한 객체 생성"의 대표적인 함정이다.
 *
 * <p>두 메서드는 기능이 같다. 차이는 누적 변수의 타입뿐이다.
 */
public final class AutoBoxingCost {

    private AutoBoxingCost() {
        throw new AssertionError("AutoBoxingCost 는 인스턴스화할 수 없습니다.");
    }

    /**
     * 원시 타입 long 으로 누적 — 불필요한 객체 생성이 없다.
     *
     * @param n 0 이상의 정수
     * @return 0 부터 n 까지의 합
     */
    public static long sumPrimitive(long n) {
        long sum = 0;
        for (long i = 0; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    /**
     * 래퍼 타입 Long 으로 누적 — 매 반복마다 Long 인스턴스가 새로 생성된다.
     *
     * <p>{@code sum += i} 에서 {@code i}(long) 가 {@code Long} 으로 오토박싱되고,
     * 더한 결과를 다시 오토언박싱하는 과정이 매 반복마다 일어난다.
     * n 이 크면 Long 객체가 n 개 쓰레기로 쌓이고 GC 압박이 커진다.
     *
     * <p>주의: Integer 캐시(-128~127)와 달리 Long 은 i 가 커질수록
     * 캐시 적중률이 떨어져 오토박싱 비용이 그대로 드러난다.
     *
     * @param n 0 이상의 정수
     * @return 0 부터 n 까지의 합
     */
    public static long sumBoxed(Long n) {
        Long sum = 0L;
        for (long i = 0; i <= n; i++) {
            sum += i;  // i 가 long → Long 으로 오토박싱 , 원시값 -> 객체 당연히 객체 생성
        }
        return sum;
    }
}