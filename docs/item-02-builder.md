# Item 2. 생성자에 매개변수가 많을 때는 빌더를 고려하라

> 본 문서는 책의 개념 설명을 따라가되, **예시 코드는 이 프로젝트에 작성한 소스**(`NutritionFacts.java`)로 대체한다.

## 핵심 요약

매개변수가 많은 클래스를 설계할 때 세 가지 대안이 있다. 빌더가 가장 균형 잡힌 선택이다.

| 패턴 | 장점 | 단점 |
|---|---|---|
| 점층적 생성자 | 불변 보장 | 매개변수 많을 때 호출 어려움, 순서 실수 위험 |
| 자바빈즈(setter) | 가독성 | 불변 불가, 일관성 깨짐, 스레드 안전 X |
| 빌더 | 불변 + 가독성 | 코드량 증가 (보통 10~20% 더 큼) |

---

## 점층적 생성자 (개선 전)

```text
public class NutritionFacts {
    public NutritionFacts(int servingSize, int servings) { ... }
    public NutritionFacts(int servingSize, int servings, int calories) { ... }
    public NutritionFacts(int servingSize, int servings, int calories, int fat) { ... }
    public NutritionFacts(int servingSize, int servings, int calories, int fat,
                          int sodium) { ... }
    public NutritionFacts(int servingSize, int servings, int calories, int fat,
                          int sodium, int carbohydrate) { ... }
}

// 호출부 — 매개변수 6개짜리 생성자. 무엇이 35이고 27인지 한눈에 안 보임
new NutritionFacts(240, 8, 100, 0, 35, 27);
```

문제:
- 매개변수 순서를 바꿔 넣어도 컴파일러가 잡지 못함 (예: `sodium`과 `carbohydrate` 자리 바꿈)
- 보통 0을 넣어 스킵 → 의미 없는 매개변수 다수
- 매개변수 추가될 때마다 새로운 생성자 오버로딩 필요

매개변수 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다.

## 자바빈즈 (개선 전)

```text
NutritionFacts coca = new NutritionFacts();
coca.setServingSize(240);
coca.setServings(8);
cocal.setCalories(100);
coca.setSodium(35);
coca.setCarbohydrate(27);
```

문제:
- `set` 호출 도중 객체가 불완전한 상태로 노출 → 다른 스레드가 반쪽짜리 객체를 볼 수 있음 -> 이미 new하는 순간 객체가 존재하는 상태
- 불변 객체로 만들 수 없음 (setter 가 공개되어 있음)
- 일관성 검사 어려움 (어느 시점에 검증할 것인가?)

## 빌더 (개선 후) — `NutritionFacts.java`

```java
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

    // ... getters ...

    public static final class Builder {
        private final int servingSize; // 필수인자 
        private final int servings; // 필수인자
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

        public Builder calories(int val) { this.calories = val; return this; }
        public Builder fat(int val) { this.fat = val; return this; }
        public Builder sodium(int val) { this.sodium = val; return this; }
        public Builder carbohydrate(int val) { this.carbohydrate = val; return this; }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }
}
```

### 호출부

```java
NutritionFacts coca = NutritionFacts.builder(240, 8)
        .calories(100)
        .sodium(35)
        .carbohydrate(27)
        .build();
```

---

## 이 코드에서 볼 수 있는 빌더 패턴의 핵심 특징

### 1. 필수 vs 선택 매개변수의 분리

- **필수** (`servingSize`, `servings`): `Builder` 생성자로 강제
  - 빌더 생성 시점에 반드시 제공되어야 함 → 컴파일 타임 강제
- **선택** (`calories`, `fat`, `sodium`, `carbohydrate`): 체이닝 메서드
  - 생략하면 기본값 0 사용 → 유연성

### 2. 불변 객체

- `NutritionFacts`는 `final` 클래스, 모든 필드가 `final`, setter 없음
- `build()` 가 호출되는 순간에야 비로소 객체가 만들어짐 → 그 이전에는 빌더가 가변 상태만 들고 있음
- 여러 스레드가 동시에 빌더를 쓰지 않는 한 완성 객체는 스레드 안전

### 3. 메서드 체이닝 (fluent API)

- 각 `calories(...)`, `fat(...)` 등은 `this` 를 반환 → 연쇄 호출
- 매개변수 이름이 메서드명으로 드러남 → 가독성 향상

---

## 빌더 패턴의 단점

1. **코드량 증가** — 보통 10~20% 더 큼. 매개변수 4개 이상일 때 가치가 시작됨
2. **객체 생성 비용** — 빌더 인스턴스가 추가로 필요. 성능 극도로 민감한 코드에서는 고려
3. **한 번 빌드해야 사용 가능** — 빌더를 쓰는 동안에는 아직 `NutritionFacts` 객체가 없음

---

## 핵심 정리

> 생성자나 정적 팩터리가 처리해야 할 매개변수가 많다면 빌더 패턴을 선택하는 게 더 낫다. 매개변수 중 다수가 선택적이거나 같은 타입이면 특히 더 그렇다. 빌더는 점층적 생성자보다 읽고 쓰기 쉽고, 자바빈즈보다 안전하다.

---

## 이 프로젝트의 산출물

| 파일 | 다루는 핵심 |
|---|---|
| `src/main/java/ch02/item02/NutritionFacts.java` | 중첩 `Builder` 클래스, 메서드 체이닝, 검증 |
| `src/test/java/ch02/item02/NutritionFactsTest.java` | 필수/선택 매개변수, 검증, 불변성 테스트 |

---

## Java 17 시대의 관점

- **record** (Java 14+)는 모든 필드가 필수인 compact 생성자를 가짐 → 빌더 없이도 간결. 단, 선택 매개변수가 많거나 검증 로직이 복잡하면 빌더가 여전히 유리
- **Lombok `@Builder`** — 보일러플레이트를 자동 생성. 단, 검증 로직이 복잡하면 직접 짠 빌더가 더 안전
- 빌더를 record 와 결합하면 불변 + 가독성 둘 다 잡을 수 있음