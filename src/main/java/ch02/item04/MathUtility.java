package ch02.item04;

/**
 * Item 4: 인스턴스화를 막으려거든 private 생성자를 사용하라.
 *
 * <p>정적 메서드만 제공하는 유틸리티 클래스의 올바른 형태.
 * 인스턴스를 만들 의도가 없으므로 {@code private} 생성자로 인스턴스화를 차단한다.
 */
public final class MathUtility {

    /**
     * 인스턴스화 방지용 private 생성자.
     *
     * <p>단순히 {@code private}으로만 선언하면, 클래스 내부의 실수로
     * {@code new MathUtility()}를 호출하는 것까지는 막지 못한다.
     * 그래서 생성자 본문에서 예외를 던져 "의도치 않은 호출"도 즉시 실패시킨다.
     */
    private MathUtility() {
        throw new AssertionError("MathUtility는 인스턴스화할 수 없습니다.");
    }

    /**
     * 두 정수의 최대공약수(GCD)를 유클리드 호제법으로 계산한다.
     *
     * <p>정적 메서드이므로 {@code MathUtility.gcd(12, 8)} 형태로만 사용한다.
     */
    public static int gcd(int a, int b) {
        // 절댓값으로 정규화하여 음수 입력도 처리
        a = Math.abs(a);
        b = Math.abs(b);

        while (b != 0) {
            int temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    /**
     * 주어진 정수가 소수인지 판별한다.
     *
     * <p>소수: 2 이상의 자연수 중 1과 자기 자신만을 약수로 갖는 수.
     * {@code n <= 1}에 대해서는 {@code false}를 반환한다 (수학적으로 "소수가 아님").
     *
     * <p>시간 복잡도 O(√n) — {@code √n}까지만 홀수 약수를 검사한다.
     */
    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        } else if (n == 2) {
            return true;
        } else if (n % 2 == 0) {
            // 2가 아닌 짝수는 모두 합성수
            return false;
        } else {
            // 홀수 약수만 검사: √n 까지 (i * i <= n)
            for (int i = 3; i * i <= n; i += 2) {
                if (n % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}