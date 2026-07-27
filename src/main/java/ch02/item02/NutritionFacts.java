package ch02.item02;

import java.util.Objects;

public final class NutritionFacts {

    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    private NutritionFacts(Builder builder) {
        this.servingSize = builder.servingSize;
        this.servings = builder.servings;
        this.calories = builder.calories;
        this.fat = builder.fat;
        this.sodium = builder.sodium;
        this.carbohydrate = builder.carbohydrate;
    }

    public static Builder builder(int servingSize, int servings) {
        return new Builder(servingSize, servings);
    }

    public int getServingSize() { return servingSize; }
    public int getServings() { return servings; }
    public int getCalories() { return calories; }
    public int getFat() { return fat; }
    public int getSodium() { return sodium; }
    public int getCarbohydrate() { return carbohydrate; }

    @Override
    public String toString() {
        return "NutritionFacts{serv=" + servingSize + "/" + servings
                + ", cal=" + calories + ", fat=" + fat
                + ", na=" + sodium + ", carb=" + carbohydrate + '}';
    }

    public static final class Builder {

        private final int servingSize; // 필수
        private final int servings; // 필수

        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            if (servingSize <= 0 || servings <= 0) {
                throw new IllegalArgumentException(
                        "servingSize 와 servings 는 양수여야 합니다: "
                                + servingSize + ", " + servings);
            }
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            // TODO(human): calories 검증 + 대입
            if(val < 0){
                throw new IllegalArgumentException("calorie는 양수여야 합니다.");
            }
            this.calories = val;
            return this;
        }

        public Builder fat(int val) {
            this.fat = val;
            return this;
        }

        public Builder sodium(int val) {
            this.sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            this.carbohydrate = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }
}