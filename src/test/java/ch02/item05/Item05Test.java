package ch02.item05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Item 5: 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라")
class Item05Test {

    // ========================================================
    // 올바른 예: SpellChecker (의존 객체 주입)
    // ========================================================
    @Nested
    @DisplayName("올바른 예: SpellChecker (생성자 주입)")
    class SpellCheckerTest {

        @Test
        @DisplayName("영어 사전을 주입하면 영어 단어를 검증한다")
        void englishDictionary_isInjected() {
            Dictionary english = new EnglishDictionary(Set.of("hello", "world", "spell"));
            SpellChecker checker = new SpellChecker(english);

            assertThat(checker.isValid("hello")).isTrue();
            assertThat(checker.isValid("world")).isTrue();
            assertThat(checker.isValid("unknown")).isFalse();
        }

        @Test
        @DisplayName("한국어 사전을 주입하면 한국어 단어를 검증한다 — 같은 SpellChecker 클래스, 다른 동작")
        void koreanDictionary_isInjected_interchangeably() {
            Dictionary korean = new KoreanDictionary(Set.of("안녕", "세계", "맞춤법"));
            SpellChecker checker = new SpellChecker(korean);

            assertThat(checker.isValid("안녕")).isTrue();
            assertThat(checker.isValid("세계")).isTrue();
            assertThat(checker.isValid("알수없음")).isFalse();
        }

        @Test
        @DisplayName("같은 SpellChecker 타입으로 두 언어를 동시에 사용할 수 있다")
        void sameClass_servesMultipleLanguages() {
            SpellChecker englishChecker = new SpellChecker(
                    new EnglishDictionary(Set.of("hello")));
            SpellChecker koreanChecker = new SpellChecker(
                    new KoreanDictionary(Set.of("안녕")));

            assertThat(englishChecker.isValid("hello")).isTrue();
            assertThat(englishChecker.isValid("안녕")).isFalse();

            assertThat(koreanChecker.isValid("안녕")).isTrue();
            assertThat(koreanChecker.isValid("hello")).isFalse();
        }

        @Test
        @DisplayName("null 사전을 주입하면 즉시 NullPointerException 발생")
        void nullDictionary_rejectedAtConstruction() {
            assertThatThrownBy(() -> new SpellChecker(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("테스트용 가짜 사전을 자유롭게 끼워넣을 수 있다")
        void fakeDictionary_canBeSubstituted() {
            Dictionary fakeAlwaysValid = new Dictionary() {
                @Override
                public boolean contains(String word) {
                    return true;  // 무조건 통과시키는 스텁
                }

                @Override
                public java.util.List<String> suggestions(String typo) {
                    return java.util.List.of();
                }
            };

            SpellChecker checker = new SpellChecker(fakeAlwaysValid);

            assertThat(checker.isValid("아무거나")).isTrue();
            assertThat(checker.isValid("zzzzz")).isTrue();
        }
    }

    // ========================================================
    // 잘못된 예 (1): 정적 유틸리티 — 사전 교체 불가
    // ========================================================
    @Nested
    @DisplayName("잘못된 예 (1): BadSpellCheckerStatic — 자원이 static final로 고정")
    class BadSpellCheckerStaticTest {

        @Test
        @DisplayName("영어 사전 한 종류로만 동작한다")
        void onlyEnglishDictionary_isAvailable() {
            assertThat(BadSpellCheckerStatic.isValid("hello")).isTrue();
            assertThat(BadSpellCheckerStatic.isValid("spell")).isTrue();
            assertThat(BadSpellCheckerStatic.isValid("unknown")).isFalse();
        }

        @Test
        @DisplayName("한국어 단어는 검증할 수 없다 — 클래스를 통째로 복사하지 않는 한")
        void korean_isNotSupported() {
            assertThat(BadSpellCheckerStatic.isValid("안녕")).isFalse();
        }
    }

    // ========================================================
    // 잘못된 예 (2): 싱글턴 — 사전 교체 불가
    // ========================================================
    @Nested
    @DisplayName("잘못된 예 (2): BadSpellCheckerSingleton — 자원이 인스턴스 필드 초기화로 고정")
    class BadSpellCheckerSingletonTest {

        @Test
        @DisplayName("인스턴스는 단 하나지만 여전히 영어 사전으로만 동작한다")
        void singleton_onlyEnglishDictionary() {
            BadSpellCheckerSingleton checker = BadSpellCheckerSingleton.INSTANCE;

            assertThat(checker.isValid("hello")).isTrue();
            assertThat(checker.isValid("안녕")).isFalse();
        }
    }
}