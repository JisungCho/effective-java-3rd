package ch02.item06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Item 6: 불필요한 객체 생성을 피하라")
class Item06Test {

    // ========================================================
    // 올바른 예: RomanNumerals (Pattern 캐싱)
    // ========================================================
    @Nested
    @DisplayName("올바른 예: RomanNumerals — Pattern 을 static final 로 재사용")
    class RomanNumeralsTest {

        @Test
        @DisplayName("올바른 로마 숫자 표기를 true 로 인식한다")
        void validRomanNumerals_areRecognized() {
            assertThat(RomanNumerals.isRomanNumeral("I")).isTrue();
            assertThat(RomanNumerals.isRomanNumeral("IV")).isTrue();
            assertThat(RomanNumerals.isRomanNumeral("XLII")).isTrue();      // 42
            assertThat(RomanNumerals.isRomanNumeral("MMXXIV")).isTrue();    // 2024
            assertThat(RomanNumerals.isRomanNumeral("MCMLXXVII")).isTrue();  // 1977
        }

        @Test
        @DisplayName("잘못된 표기를 false 로 거른다")
        void invalidRomanNumerals_areRejected() {
            assertThat(RomanNumerals.isRomanNumeral("")).isFalse();
            assertThat(RomanNumerals.isRomanNumeral("IIII")).isFalse();   // 4는 IV
            assertThat(RomanNumerals.isRomanNumeral("VV")).isFalse();      // V 는 반복 불가
            assertThat(RomanNumerals.isRomanNumeral("1234")).isFalse();
            assertThat(RomanNumerals.isRomanNumeral("ABC")).isFalse();
            assertThat(RomanNumerals.isRomanNumeral("help")).isFalse();
        }
    }

    // ========================================================
    // 잘못된 예 vs 올바른 예: 기능은 같아야 한다
    // ========================================================
    @Nested
    @DisplayName("두 구현은 기능상 동일하다 — 객체 생성 비용만 다르다")
    class EquivalenceTest {

        @Test
        @DisplayName("같은 입력에 대해 RomanNumerals 와 BadRomanNumerals 는 같은 결과를 낸다")
        void bothImplementations_produceSameResult() {
            String[] samples = {"I", "IV", "XLII", "MMXXIV", "MCMLXXVII", "IIII", "VV", "1234", "ABC", "help", ""};

            for (String sample : samples) {
                assertThat(RomanNumerals.isRomanNumeral(sample))
                        .as("입력 '%s' 에서 두 구현이 다르다!", sample)
                        .isEqualTo(BadRomanNumerals.isRomanNumeral(sample));
            }
        }
    }

    // ========================================================
    // 성능 비교: 캐싱의 가치 체감
    // ========================================================
    @Nested
    @DisplayName("성능 비교 — Pattern 캐싱이 매번 컴파일보다 압도적으로 빠르다")
    class PerformanceTest {

        /**
         * 주의: JUnit 의 단순 시간 측정은 JMH 만큼 정확하지 않다(JIT 워밍업·GC 영향).
         * 다만 두 구현의 차이가 압도적이라 교육 목적의 체감용으로는 충분하다.
         * 정밀한 벤치마크는 JMH 를 써야 한다.
         */
        @Test
        @DisplayName("동일 입력을 수만 번 검사할 때 RomanNumerals 가 BadRomanNumerals 보다 현저히 빠르다")
        void cachedPattern_isMuchFaster() {
            String input = "MCMLXXVII";   // 1977, 정규식이 조금 복잡한 케이스
            int iterations = 100_000;

            // (1) 워밍업 — JIT 가 핫 경로를 컴파일하도록 미리 돌려둔다
            for (int i = 0; i < 5_000; i++) {
                RomanNumerals.isRomanNumeral(input);
                BadRomanNumerals.isRomanNumeral(input);
            }

            // (2) 측정: Pattern 캐싱 버전
            long cachedStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                RomanNumerals.isRomanNumeral(input);
            }
            long cachedNanos = System.nanoTime() - cachedStart;

            // (3) 측정: 매번 컴파일 버전
            long repeatedStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                BadRomanNumerals.isRomanNumeral(input);
            }
            long repeatedNanos = System.nanoTime() - repeatedStart;

            System.out.printf(
                    "[Item 6 성능] Pattern 캐싱: %,d ns | 매번 컴파일: %,d ns | 비율: %.1f배%n",
                    cachedNanos, repeatedNanos, repeatedNanos / (double) cachedNanos);

            // 어설션: 매번 컴파일이 캐싱보다 "느려야" 한다 (역전은 Item 6 원칙이 깨졌다는 뜻).
            // 환경에 따라 오차가 크므로 "같거나 느림" 정도로만 단정.
            assertThat(repeatedNanos)
                    .as("매번 컴파일(BadRomanNumerals)이 Pattern 캐싱(RomanNumerals)보다 느려야 한다")
                    .isGreaterThanOrEqualTo(cachedNanos);
        }
    }

    // ========================================================
    // 오토박싱: 숨은 객체 생성
    // ========================================================
    @Nested
    @DisplayName("오토박싱 — long vs Long 은 결과가 같아도 비용이 다르다")
    class AutoBoxingTest {

        @Test
        @DisplayName("원시 타입 누적과 래퍼 타입 누적은 같은 결과를 낸다")
        void primitiveAndBoxed_produceSameResult() {
            long n = 10_000;

            long primitive = AutoBoxingCost.sumPrimitive(n);
            long boxed = AutoBoxingCost.sumBoxed(n);

            assertThat(boxed).isEqualTo(primitive);          // 0+1+...+10000 = 50,005,000
            assertThat(primitive).isEqualTo(50_005_000L);
        }

        @Test
        @DisplayName("Long 누적은 long 누적보다 눈에 띄게 느리다 — 매 반복마다 Long 인스턴스 생성")
        void boxedAccumulation_isSlower() {
            long n = 1_000_000;

            long primStart = System.nanoTime();
            AutoBoxingCost.sumPrimitive(n);
            long primNanos = System.nanoTime() - primStart;

            long boxedStart = System.nanoTime();
            AutoBoxingCost.sumBoxed(n);
            long boxedNanos = System.nanoTime() - boxedStart;

            System.out.printf(
                    "[Item 6 오토박싱] long 누적: %,d ns | Long 누적: %,d ns | 비율: %.1f배%n",
                    primNanos, boxedNanos, boxedNanos / (double) primNanos);

            assertThat(boxedNanos)
                    .as("Long 누적은 매 반복마다 Long 객체를 새로 만들어 long 누적보다 느려야 한다")
                    .isGreaterThan(primNanos);
        }
    }
}