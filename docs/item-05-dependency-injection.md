# Item 5. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

> 본 문서는 책의 개념 설명을 따라가되, **예시 코드는 이 프로젝트에 작성한 소스**(`Dictionary`, `EnglishDictionary`, `KoreanDictionary`, `BadSpellCheckerStatic`, `BadSpellCheckerSingleton`, `SpellChecker`)로 대체한다.

## 핵심 요약

많은 클래스가 하나 이상의 **자원(resource)**에 의존한다. 예를 들어 맞춤법 검사기는 **사전(dictionary)**에 의존한다. 이때 클래스가 "사용하는 자원에 따라 동작이 달라진다면", **정적 유틸리티 클래스(Item 4)**나 **싱글턴(Item 3)**으로 구현하면 안 된다.

| 구현 방식 | 자원 교체 | 테스트 용이성 | 해당 아이템 |
|---|---|---|---|
| 정적 유틸리티 (잘못됨) | ✗ (static final 고정) | ✗ | Item 4의 잘못된 적용 |
| 싱글턴 (잘못됨) | ✗ (인스턴스 필드 초기화 고정) | ✗ | Item 3의 잘못된 적용 |
| **의존 객체 주입 (정답)** | **O** (생성자로 받기) | **O** | **Item 5** |

**정답**: 클래스가 필요로 하는 자원을 (구체 타입이 아니라) **인터페이스 타입으로** 받아들이고, **생성자에서 주입**받기.

---

## 잘못된 예 (1): 정적 유틸리티로 구현한 SpellChecker

`BadSpellCheckerStatic.java`

```java
public final class BadSpellCheckerStatic {

    private static final Dictionary DICTIONARY =
            new EnglishDictionary(Set.of("hello", "world", "spell", "checker"));

    private BadSpellCheckerStatic() {
        throw new AssertionError("BadSpellCheckerStatic 은 인스턴스화할 수 없습니다.");
    }

    public static boolean isValid(String word) {
        return DICTIONARY.contains(word);
    }
}
```

겉보기엔 Item 4의 유틸리티 클래스 패턴을 그대로 따랐다 (`final class` + `private` 생성자 + `static` 메서드). 하지만 **치명적 차이**가 있다:

| | MathUtility (Item 4, 올바른 예) | BadSpellCheckerStatic (잘못된 예) |
|---|---|---|
| `static final` 필드가 담는 것 | 상수 (π, e 같은 불변값) | **교체 가능한 자원 (사전)** |
| 교체 가능? | 교체 자체가 무의미 | 영어 ↔ 한국어로 교체 필요 |
| 테스트 | 상태 없음 → 순수 함수 테스트 | 사전을 mock으로 대체해야 하는데 불가능 |

**왜 문제인가**: 자원(사전)을 `static final`로 못박아버리면,
1. 한국어 검사를 원하면 클래스를 통째로 복사해야 한다
2. 테스트에서 가짜 사전(mock dictionary)을 끼워넣을 수 없다
3. 다른 사전 구현체로 교체하려면 소스를 직접 고쳐야 한다

**구분선**: 자원이 (a) **불변의 상수**라면 → Item 4 패턴이 정답. (b) **교체될 수 있는 자원**이라면 → Item 5의 DI가 정답.

---

## 잘못된 예 (2): 싱글턴으로 구현한 SpellChecker

`BadSpellCheckerSingleton.java`

```java
public final class BadSpellCheckerSingleton {

    public static final BadSpellCheckerSingleton INSTANCE = new BadSpellCheckerSingleton();

    private final Dictionary dictionary =
            new EnglishDictionary(Set.of("hello", "world", "spell", "checker"));

    private BadSpellCheckerSingleton() { }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }
}
```

Item 3의 `public static final INSTANCE` 패턴을 가져왔다. 하지만 여전히 **자원을 인스턴스 필드 초기화에서 직접 생성**한다.

싱글턴은 **"정확히 하나여야 할 때"** 쓰는 도구다. SpellChecker는 싱글턴으로 만들 이유가 없다 — 영어 검사용 인스턴스, 한국어 검사용 인스턴스가 동시에 필요할 수 있기 때문이다. 싱글턴이라는 제약이 의존 자원의 유연성을 짓밟는다.

---

## 올바른 예: 의존 객체 주입 (생성자 주입)

`SpellChecker.java` (학습자가 직접 작성한 부분 포함)

```java
public final class SpellChecker {

    private final Dictionary dictionary;

    public SpellChecker(Dictionary dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }

    public List<String> suggestions(String typo) {
        return dictionary.suggestions(typo);
    }
}
```

### 세 가지 핵심 장점

| 특징 | 효과 | 근거 아이템 |
|---|---|---|
| 매개변수 타입이 `Dictionary` (인터페이스) | EnglishDictionary/KoreanDictionary/가짜 사전 모두 주입 가능 | Item 5 핵심 |
| `private final` 필드 | 한 번 주입 후 변경 불가 → 불변, 스레드 안전 | Item 17 |
| `Objects.requireNonNull` | null 주입 즉시 NPE로 차단 → 불변식 강제 | 관용적 방어 |

### 자원을 교체하며 쓰기

```java
// 영어 검사
SpellChecker en = new SpellChecker(
        new EnglishDictionary(Set.of("hello", "world")));
en.isValid("hello");   // true

// 한국어 검사 — 같은 SpellChecker 클래스!
SpellChecker ko = new SpellChecker(
        new KoreanDictionary(Set.of("안녕", "세계")));
ko.isValid("안녕");    // true

// 테스트 — 항상 true를 반환하는 가짜 사전
Dictionary fakeAlwaysValid = new Dictionary() {
    public boolean contains(String word) { return true; }
    public List<String> suggestions(String typo) { return List.of(); }
};
SpellChecker testChecker = new SpellChecker(fakeAlwaysValid);
testChecker.isValid("아무거나");   // 항상 true
```

동일한 `SpellChecker` 클래스 하나로 영어/한국어/테스트용 사전을 모두 처리한다. 이것이 자원을 "직접 명시하지 않는" 코드의 힘이다.

---

## 왜 인터페이스 타입으로 받아야 하는가

```java
// (X) 구체 클래스에 의존
public SpellChecker(EnglishDictionary dictionary) { ... }
// → KoreanDictionary 는 주입 불가능. 다른 클래스를 만들어야 한다.

// (O) 인터페이스에 의존
public SpellChecker(Dictionary dictionary) { ... }
// → EnglishDictionary, KoreanDictionary, 테스트용 스텁 모두 OK.
```

이것이 **의존성 역전 원칙(Dependency Inversion Principle, SOLID의 D)**이다:

- (X) 상위 모듈(SpellChecker)이 하위 모듈(EnglishDictionary)에 직접 의존
- (O) 양쪽 모두 추상(Dictionary 인터페이스)에 의존 → 하위 모듈을 자유롭게 교체 가능

---

## `Objects.requireNonNull`이 하는 일

```java
public SpellChecker(Dictionary dictionary) {
    this.dictionary = Objects.requireNonNull(dictionary);
}
```

| 단계 | 동작 |
|---|---|
| 1. 인자가 null이 아님 | 인자를 그대로 반환 → 필드에 할당 |
| 2. 인자가 null임 | `NullPointerException` 발생 |

한 줄로 **(1) null 검사 + (2) 필드 할당**을 동시에 끝낸다. 검사만 하고 할당을 빼먹는 실수를 원천 차단하는 관용구다. 메시지를 넣으면 디버깅도 편하다:

```java
Objects.requireNonNull(dictionary, "dictionary must not be null");
```

---

## 변형: 팩토리 주입 (Supplier<T>)

생성자가 호출되는 시점이 아니라, **메서드가 호출될 때마다 새 자원 인스턴스**가 필요하다면 자원 팩토리를 주입한다. Java 8+의 `Supplier<T>`가 표준 관용구다.

```java
public final class SpellCheckerFactory {

    private final Supplier<Dictionary> dictionaryFactory;

    public SpellCheckerFactory(Supplier<Dictionary> dictionaryFactory) {
        this.dictionaryFactory = Objects.requireNonNull(dictionaryFactory);
    }

    public SpellChecker newChecker() {
        return new SpellChecker(dictionaryFactory.get());
    }
}
```

사용 예:

```java
// 매번 새 사전을 만들어야 하는 상황
SpellCheckerFactory factory =
        new SpellCheckerFactory(() -> loadDictionaryFromDb());

SpellChecker c1 = factory.newChecker();  // DB에서 새로 로드
SpellChecker c2 = factory.newChecker();  // 다시 DB에서 새로 로드
```

- `Supplier<Dictionary>`도 결국 **인터페이스 타입으로 받는다**는 원칙을 그대로 따른다.
- 팩토리 자체를 mock으로 대체하면 테스트에서 자원 생성 시점까지 제어할 수 있다.

---

## Item 3, 4, 5의 관계 — 한눈에 비교

| | Item 3 (싱글턴) | Item 4 (유틸리티) | Item 5 (DI) |
|---|---|---|---|
| 인스턴스 수 | 정확히 1개 | 0개 | N개 (자원마다) |
| 자원 처리 방식 | 인스턴스 필드로 직접 보유 | 정적 필드로 직접 보유 | 생성자로 주입받기 |
| 자원 교체 | ✗ | ✗ | **O** |
| 어울리는 자원 | 교체 불가능한 단일 자원 | 불변의 상수 / 정적 동작 | 언어/환경에 따라 달라지는 자원 |
| 자원의 예 | 설정 저장소(RDB 1개 고정) | `Math.PI` | 맞춤법 사전, 결제 게이트웨이 |

세 아이템은 **"자원을 어떻게 다룰 것인가"**라는 한 질문에 대한 단계적 답이다. Item 3/4는 자원이 "단일하고 불변"이라는 강한 전제가 깰 수 있는 자원에는 어울리지 않고, 그런 자원에는 Item 5가 정답이다.

---

## 이 프로젝트 실습 결과 — SpellChecker

학습자가 직접 Item 5의 핵심을 구현한 클래스.

`SpellChecker.java`

```java
public final class SpellChecker {

    private final Dictionary dictionary;

    public SpellChecker(Dictionary dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }
}
```

세 가지 핵심을 모두 갖췄다:
1. `private final` 필드로 자원을 보관 (불변, Item 17)
2. `Dictionary` **인터페이스**를 생성자 매개변수로 받음 (의존성 역전)
3. `Objects.requireNonNull`로 null 검사

테스트(`Item05Test`)로 검증한 것:
- 영어 사전 주입 시 영어 단어 검증
- 한국어 사전 주입 시 한국어 단어 검증 — **같은 클래스, 다른 동작**
- null 주입 시 NPE
- 익명 클래스로 만든 가짜 사전도 자유롭게 주입 가능

---

## 핵심 정리

> 클래스가 **사용하는 자원에 따라 동작이 달라진다면**, 정적 유틸리티(Item 4)나 싱글턴(Item 3)을 사용하지 말라. 대신 **생성자를 통해 자원을 주입**받아라. 자원은 구체 타입이 아니라 **인터페이스 타입으로** 받아야 구현체를 자유롭게 교체할 수 있다. 불변(`final` 필드)과 짝을 이루면 스레드 안전하고 재사용성 높은 클래스가 된다. 자원 생성 시점이 유동적이면 `Supplier<T>` 팩토리를 주입하는 변형도 가능하다.

---

## 이 프로젝트의 산출물

| 파일 | 다루는 핵심 |
|---|---|
| `src/main/java/ch02/item05/Dictionary.java` | 자원(사전)의 추상 타입 — 인터페이스 |
| `src/main/java/ch02/item05/EnglishDictionary.java` | Dictionary 구현체 (영어) |
| `src/main/java/ch02/item05/KoreanDictionary.java` | Dictionary 구현체 (한국어) — 교체 가능성의 증거 |
| `src/main/java/ch02/item05/BadSpellCheckerStatic.java` | 잘못된 예 (1): 정적 유틸리티 — 자원을 static final로 고정 |
| `src/main/java/ch02/item05/BadSpellCheckerSingleton.java` | 잘못된 예 (2): 싱글턴 — 자원을 인스턴스 필드 초기화로 고정 |
| `src/main/java/ch02/item05/SpellChecker.java` | 학습자 실습 결과: 생성자 주입 + final 필드 + null 검사 |
| `src/test/java/ch02/item05/Item05Test.java` | DI 유연성 검증 + 잘못된 예 한계 시연 |

---

## Java 17 시대의 관점

- **Spring 같은 DI 프레임워크가 없어도 DI는 성립한다.** 이 프로젝트의 `SpellChecker`는 순수 생성자 주입만으로 DI를 실천한다. 프레임워크는 "편의"일 뿐, "필수"가 아니다.
- **`record`로 더 간결하게**: Java 14+의 record는 컴팩트 생성자 + final 필드를 언어 차원에서 지원한다. 단순한 DI 클래스는 record로 줄일 수 있다:
  ```java
  public record SpellChecker(Dictionary dictionary) {
      public SpellChecker {
          Objects.requireNonNull(dictionary);
      }
      // isValid, suggestions 등은 그대로 추가
  }
  ```
- **Spring/Jakarta EE의 `@Inject`, `@Autowired`**는 본질적으로 "생성자로 자원을 넘겨받는다"는 이 Item 5 원칙을 프레임워크가 자동화한 것. 원칙을 모르고 애노테이션만 쓰면 왜 Field Injection이 안 좋은지, 왜 Constructor Injection을 권장하는지 이유가 안 보인다.
- **테스트 용이성이 DI의 가장 큰 실용적 이점**이다. 자원을 직접 명시하면 모킹이 불가능하지만, 생성자 주입이면 가짜 자원(익명 클래스, Mockito mock 등)을 한 줄로 끼워넣을 수 있다.