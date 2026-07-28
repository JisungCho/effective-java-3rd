package ch02.item04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Item 4: 인스턴스화를 막으려거든 private 생성자를 사용하라")
class Item04Test {

    // ========================================================
    // 올바른 유틸리티 클래스 — MathUtility
    // ========================================================
    @Nested
    @DisplayName("올바른 예 (1): MathUtility")
    class MathUtilityTest {

        @Test
        @DisplayName("gcd: 최대공약수 계산")
        void gcd() {
            assertThat(MathUtility.gcd(12, 8)).isEqualTo(4);
            assertThat(MathUtility.gcd(54, 24)).isEqualTo(6);
            assertThat(MathUtility.gcd(7, 0)).isEqualTo(7);
        }

        @Test
        @DisplayName("gcd: 음수는 절댓값으로 정규화")
        void gcd_negativeInput() {
            assertThat(MathUtility.gcd(-12, 8)).isEqualTo(4);
        }

        @Test
        @DisplayName("isPrime: 소수 판별")
        void isPrime_trueCases() {
            assertThat(MathUtility.isPrime(2)).isTrue();
            assertThat(MathUtility.isPrime(3)).isTrue();
            assertThat(MathUtility.isPrime(7)).isTrue();
            assertThat(MathUtility.isPrime(97)).isTrue();
        }

        @Test
        @DisplayName("isPrime: 합성수·경계값은 false")
        void isPrime_falseCases() {
            assertThat(MathUtility.isPrime(0)).isFalse();
            assertThat(MathUtility.isPrime(1)).isFalse();
            assertThat(MathUtility.isPrime(-5)).isFalse();
            assertThat(MathUtility.isPrime(4)).isFalse();   // 2가 아닌 짝수
            assertThat(MathUtility.isPrime(35)).isFalse();  // 5 * 7
        }

        @Test
        @DisplayName("리플렉션으로 private 생성자 강제 호출 → AssertionError로 차단")
        void privateConstructorDefendedByAssertionError() throws NoSuchMethodException {
            Constructor<MathUtility> constructor =
                    MathUtility.class.getDeclaredConstructor();
            constructor.setAccessible(true);  // private 우회는 가능하지만

            // 생성자 본문의 throw AssertionError 가 런타임 차단
            // newInstance() 는 원인 예외를 InvocationTargetException 으로 감쌈
            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasRootCauseInstanceOf(AssertionError.class);
        }
    }

    // ========================================================
    // 올바른 유틸리티 클래스 — PasswordValidator (사용자 실습 결과)
    // ========================================================
    @Nested
    @DisplayName("올바른 예 (2): PasswordValidator")
    class PasswordValidatorTest {

        @Test
        @DisplayName("isLongEnough: 최소 길이 검증")
        void isLongEnough() {
            assertThat(PasswordValidator.isLongEnough("abc123", 6)).isTrue();
            assertThat(PasswordValidator.isLongEnough("ab", 6)).isFalse();
            assertThat(PasswordValidator.isLongEnough(null, 1)).isFalse();
        }

        @Test
        @DisplayName("hasSpecialChar: 특수문자 포함 검증")
        void hasSpecialChar() {
            assertThat(PasswordValidator.hasSpecialChar("abc!23")).isTrue();
            assertThat(PasswordValidator.hasSpecialChar("abc123")).isFalse();
            assertThat(PasswordValidator.hasSpecialChar(null)).isFalse();
        }

        @Test
        @DisplayName("리플렉션으로 private 생성자 강제 호출 → AssertionError로 차단")
        void privateConstructorDefendedByAssertionError() throws NoSuchMethodException {
            Constructor<PasswordValidator> constructor =
                    PasswordValidator.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasRootCauseInstanceOf(AssertionError.class);
        }
    }

    // ========================================================
    // 잘못된 예 — 인스턴스화가 "막히지 않는" 유틸리티 클래스들
    // ========================================================
    @Nested
    @DisplayName("잘못된 예: 인스턴스화가 가능해진 유틸리티 클래스들")
    class WrongExamplesTest {

        @Test
        @DisplayName("기본 생성자 자동 생성 → new 가 컴파일/런타임 모두 통과해버림")
        void accidentalInstantiation_compilesAndRuns() {
            // 생성자를 명시하지 않았기 때문에 컴파일러가 public 기본 생성자를 만들어버림.
            // → 의도치 않게 인스턴스가 생성됨.
            AccidentalInstantiation instance = new AccidentalInstantiation();

            assertThat(instance).isNotNull();
            assertThat(AccidentalInstantiation.shout("hi")).isEqualTo("HI");
        }

        @Test
        @DisplayName("abstract 클래스는 하위 클래스로 우회 인스턴스화 가능")
        void abstractUtility_canBeInstantiatedViaSubclass() {
            // abstract 자체는 new AbstractUtilityAttempt() 를 막지만,
            // 하위 클래스 SneakySubclass 를 만들면 그 객체로 인스턴스화 가능.
            AbstractUtilityAttempt instance = new SneakySubclass();

            assertThat(instance).isNotNull();
            assertThat(instance.decorate("hi")).isEqualTo("[hi]");
            assertThat(AbstractUtilityAttempt.whisper("HI")).isEqualTo("hi");
        }
    }
}