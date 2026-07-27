package ch02.item02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Item 2: 빌더 패턴 - NutritionFacts")
class NutritionFactsTest {

    @Test
    @DisplayName("필수 매개변수만으로 빌드하면 선택 매개변수는 기본값(0)이다")
    void requiredOnly() {
        NutritionFacts coca = NutritionFacts.builder(240, 8)
                .build();

        assertThat(coca.getServingSize()).isEqualTo(240);
        assertThat(coca.getServings()).isEqualTo(8);
        assertThat(coca.getCalories()).isZero();
        assertThat(coca.getFat()).isZero();
    }

    @Test
    @DisplayName("메서드 체이닝으로 선택 매개변수를 채운다")
    void chainedBuilder() {
        NutritionFacts coca = NutritionFacts.builder(240, 8)
                .calories(100)
                .sodium(35)
                .carbohydrate(27)
                .build();

        assertThat(coca.getCalories()).isEqualTo(100);
        assertThat(coca.getSodium()).isEqualTo(35);
        assertThat(coca.getCarbohydrate()).isEqualTo(27);
    }

    @Test
    @DisplayName("필수 매개변수가 0 이하이면 예외를 던진다")
    void invalidRequired() {
        assertThatThrownBy(() -> NutritionFacts.builder(0, 8))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("calories 가 음수이면 예외를 던진다 (TODO(human) 구현 후 통과)")
    void negativeCaloriesThrows() {
        assertThatThrownBy(() -> NutritionFacts.builder(240, 8).calories(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("완성된 객체는 불변 — setter 가 없다")
    void immutable() {
        NutritionFacts coca = NutritionFacts.builder(240, 8).calories(100).build();

        // 컴파일 타임에 setter 가 없는 것이 증명됨 (리플렉션 사용하지 않는 한)
        assertThat(coca.getCalories()).isEqualTo(100);
    }
}