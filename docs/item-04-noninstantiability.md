# Item 4. 인스턴스화를 막으려거든 private 생성자를 사용하라

> 본 문서는 책의 개념 설명을 따라가되, **예시 코드는 이 프로젝트에 작성한 소스**(`MathUtility`, `PasswordValidator`, `AccidentalInstantiation`, `AbstractUtilityAttempt`, `SneakySubclass`)로 대체한다.

## 핵심 요약

정적 메서드와 정적 필드만을 담은 **유틸리티 클래스**(`java.lang.Math`, `java.util.Collections`, `java.util.Arrays` 등)는 인스턴스를 만들 목적으로 만든 게 아니다. 그런데 이런 클래스에 **생성자를 하나도 선언하지 않으면**, 컴파일러가 자동으로 **public 기본 생성자**를 만들어버려서 사용자가 `new`로 인스턴스를 만들 수 있게 된다.

인스턴스화를 막는 방법과 흔한 오해:

| 방법 | 인스턴스화 차단? | 비고 |
|---|---|---|
| (X) 생성자를 아예 안 쓴다 | ✗ | 컴파일러가 public 기본 생성자 자동 생성 |
| (X) `abstract class`로 선언 | ✗ | 하위 클래스를 만들면 `new` 가능 |
| (O) `private` 생성자 명시 | **O** | 외부에서 `new` 불가 |
| (O) 위 + 본문에서 `throw AssertionError` | **O** | 내부 실수 호출까지 차단 (이중 방어) |

---

## 잘못된 예 (1): 생성자를 하나도 선언하지 않은 경우

`AccidentalInstantiation.java`

```java
public class AccidentalInstantiation {

    // 생성자를 하나도 선언하지 않음
    // → 컴파일러가 public AccidentalInstantiation() {} 를 자동 생성

    public static String shout(String text) {
        return text.toUpperCase();
    }
}
```

- 자바 컴파일러 규칙: **"클래스에 생성자가 하나도 선언되어 있지 않으면 public 기본 생성자를 자동으로 넣는다."**
- 결과: 다음 코드가 **컴파일 에러 없이 통과**해버린다.

```java
AccidentalInstantiation instance = new AccidentalInstantiation();  // ← 만들어져 버림
```

- 의도치 않은 인스턴스가 만들어지면:
  1. 사용자가 "이 클래스는 정적 메서드만 쓰는 건데?"라는 API 의도를 놓침
  2. 불필요한 객체 생성으로 메모리/GC 낭비
  3. "인스턴스 메서드가 없는데 왜 만들었지?"라는 혼란

### 자주 하는 오해: "private 멤버만 있으면 되지 않을까?"

아니다. **생성자를 명시적으로 쓰지 않았다**는 사실이 문제지, 멤버의 접근 제어와는 무관하다. 생성자를 단 하나도 쓰지 않으면 무조건 public 기본 생성자가 생긴다.

---

## 잘못된 예 (2): `abstract class`로 막으려는 시도

`AbstractUtilityAttempt.java`

```java
public abstract class AbstractUtilityAttempt {

    public static String whisper(String text) {
        return text.toLowerCase();
    }

    public abstract String decorate(String text);
}
```

많은 사람이 "abstract면 `new`를 못 하니까 인스턴스화가 막히겠지?"라고 착각한다. 틀렸다:

```java
AbstractUtilityAttempt x = new AbstractUtilityAttempt();   // ← 컴파일 에러 (abstract)
```

이 줄은 막힌다. 하지만 **하위 클래스를 하나 만들면 그 객체로 인스턴스화할 수 있다**:

`SneakySubclass.java`

```java
public class SneakySubclass extends AbstractUtilityAttempt {

    @Override
    public String decorate(String text) {
        return "[" + text + "]";
    }
}
```

```java
AbstractUtilityAttempt instance = new SneakySubclass();   // ← 만들어져 버림
instance.decorate("hi");   // → "[hi]"
```

- `abstract`는 "인스턴스화 방지"가 아니라 **"반드시 상속해서 쓰라"**는 의미일 뿐이다.
- 상속을 의도한 게 아니라면 abstract로 막는 건 **오히려 상속을 유도하는 부정 신호**가 된다 (Item 19 참고).

---

## 올바른 해법: `private` 생성자 + `AssertionError`

`MathUtility.java`

```java
public final class MathUtility {

    private MathUtility() {
        throw new AssertionError("MathUtility는 인스턴스화할 수 없습니다.");
    }

    public static int gcd(int a, int b) { ... }
    public static boolean isPrime(int n) { ... }
}
```

이중 방어가 작동한다:

| 위협 | 어디서 차단? | 메커니즘 |
|---|---|---|
| 외부에서 `new MathUtility()` | 컴파일 타임 | `private` → 다른 클래스에서 호출 시 컴파일 에러 |
| 리플렉션으로 강제 호출 | 런타임 | 생성자 본문의 `throw AssertionError` |
| 클래스 내부에서 실수 호출 | 런타임 | 동일 (`AssertionError`) |

### 왜 하필 `AssertionError`인가?

- 자바에서 `Error` 계열은 **"프로그램 논리상 절대 일어나면 안 되는 상태"**를 나타낸다.
- `private` 생성자가 호출됐다는 건 곧 **프로그래머의 명백한 실수**이므로, `RuntimeException`이나 `IllegalStateException`보다 `AssertionError`가 의미가 더 정확하다.
- `Exception`은 호출자가 잡을 수 있지만, `Error`는 **잡지 않는 것이 관례**라서 "이건 복구 대상이 아닌 버그"라는 신호가 선명하다.
- JDK 표준 라이브러리(`java.util.Collections`, `java.lang.Math` 등)도 같은 관례를 따른다.

### 왜 `final class`와 짝을 이루는가?

```java
public final class MathUtility {   // ← final
    private MathUtility() { ... }
}
```

- `final`이면 **상속 자체가 불가능** → "이 클래스를 확장하라"는 길도 봉쇄.
- `private` 생성자이면 **인스턴스화도 불가능** → "이 클래스를 new 하라"는 길도 봉쇄.
- 둘이 합쳐지면 "이 클래스는 오직 정적 멤버 모음"이라는 의도가 API 수준에서 명확히 드러난다 (Item 19와 정합).

---

## 리플렉션 공격 시나리오

`private`는 **컴파일 타임 검사**일 뿐, 런타임에 `setAccessible(true)`로 우회할 수 있다 (Item 3에서도 다룬 주제).

```java
Constructor<MathUtility> c = MathUtility.class.getDeclaredConstructor();
c.setAccessible(true);                  // private 우회는 성공
c.newInstance();                        // ← AssertionError 발생!
```

- `private`만 있었다면 우회에 성공했을 것이다.
- 하지만 생성자 본문의 `throw AssertionError`가 런타임까지 차단한다.
- 이것이 "왜 생성자 본문에서 예외를 던져야 하는가"의 결정적 이유다.

이 동작은 이 프로젝트의 테스트로 검증했다:

```java
@Test
@DisplayName("리플렉션으로 private 생성자 강제 호출 → AssertionError로 차단")
void privateConstructorDefendedByAssertionError() throws NoSuchMethodException {
    Constructor<MathUtility> constructor =
            MathUtility.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertThatThrownBy(constructor::newInstance)
            .isInstanceOf(InvocationTargetException.class)
            .hasRootCauseInstanceOf(AssertionError.class);
}
```

> 참고: 리플렉션의 `Constructor.newInstance()`는 원인 예외를 `InvocationTargetException`으로 감싼다. 그래서 `AssertionError`는 **root cause**로 들어간다.

---

## Before / After: 세 상황 비교

### 선언부

```text
// 잘못된 예 (1): 생성자 미선언 → public 기본 생성자 자동 생성
public class X {
    public static String f() { ... }
}

// 잘못된 예 (2): abstract → 하위 클래스로 우회 가능
public abstract class X {
    public static String f() { ... }
    public abstract String g();
}

// 올바른 예: private 생성자 + AssertionError + final
public final class X {
    private X() { throw new AssertionError(); }
    public static String f() { ... }
}
```

### 호출부

```text
// 잘못된 예 (1) — 의도치 않게 new 통과
new AccidentalInstantiation();               // OK (위험)

// 잘못된 예 (2) — 하위 클래스로 우회
new SneakySubclass();                         // OK (위험)

// 올바른 예 — 외부에서 new 시도
new MathUtility();                            // 컴파일 에러
// 리플렉션으로 우회 시도
constructor.setAccessible(true);
constructor.newInstance();                    // AssertionError
```

---

## 이 프로젝트 실습 결과 — PasswordValidator

학습자가 직접 Item 4의 핵심 패턴을 적용한 유틸리티 클래스.

`PasswordValidator.java`

```java
public final class PasswordValidator {

    private PasswordValidator() {
        throw new AssertionError("PasswordValidator는 인스턴스화할 수 없습니다.");
    }

    public static boolean isLongEnough(String password, int minLength) { ... }
    public static boolean hasSpecialChar(String password) { ... }
}
```

- 정적 검증 메서드만 제공 → 인스턴스 불필요 → `private` 생성자로 차단
- 리플렉션 공격에도 `AssertionError`로 런타임 차단 (테스트로 검증 완료)
- `MathUtility`와 동일한 이중 방어 패턴을 그대로 적용

---

## 이 코드에서 관찰한 점

### 생성자를 명시하면 기본 생성자는 자동 생성되지 않는다

```text
// MathUtility 처럼 private 생성자를 하나라도 쓰면
public final class X {
    private X() { ... }
}
// → 컴파일러가 기본 생성자를 만들지 않는다
// → "이미 사용자가 생성자를 썼으니 내가 끼워 넣지 않는다"는 규칙
```

이 규칙은 생성자가 public이든 protected든 private든 상관없이 동일하다. 즉 "private라서 기본 생성자가 안 생긴다"가 아니라 **"생성자를 하나라도 선언했기 때문에"** 안 생기는 것이다.

### 인스턴스화 방지 vs 싱글턴(Item 3)

| 항목 | Item 3 (싱글턴) | Item 4 (인스턴스화 금지) |
|---|---|---|
| 목표 | 인스턴스를 **정확히 하나**만 | 인스턴스를 **아예 없게** |
| 생성자 | private (내부에서 한 번 호출) | private (한 번도 호출 안 함) |
| 본문 | 인스턴스 생성 | `throw AssertionError` |
| 정적 멤버 | 인스턴스를 참조 | 인스턴스 없이 동작 |

두 아이템 모두 private 생성자를 쓰지만, **생성자 본문이 하는 일이 정반대**라는 점이 포인트다.

---

## 핵심 정리

> 정적 멤버만 담은 유틸리티 클래스는 인스턴스를 만들 목적이 아니다. 인스턴스화를 막으려면 **`private` 생성자를 명시적으로 선언**하라. 단순히 `private`만 쓰지 말고, **생성자 본문에서 예외(`AssertionError`)를 던져** 클래스 내부에서의 실수 호출까지 차단하는 이중 방어로 만들어라. `abstract`로는 막을 수 없다.

---

## 이 프로젝트의 산출물

| 파일 | 다루는 핵심 |
|---|---|
| `src/main/java/ch02/item04/MathUtility.java` | 올바른 예: private 생성자 + AssertionError + final |
| `src/main/java/ch02/item04/PasswordValidator.java` | 학습자 실습 결과: 동일한 이중 방어 패턴 적용 |
| `src/main/java/ch02/item04/AccidentalInstantiation.java` | 잘못된 예 (1): 기본 생성자 자동 생성으로 인스턴스화 가능 |
| `src/main/java/ch02/item04/AbstractUtilityAttempt.java` | 잘못된 예 (2): abstract는 하위 클래스로 우회 가능 |
| `src/main/java/ch02/item04/SneakySubclass.java` | 잘못된 예 (2)의 증거: abstract 클래스를 new 한 사례 |
| `src/test/java/ch02/item04/Item04Test.java` | 정적 메서드 동작 + 리플렉션 차단 + 잘못된 예 비교 |

---

## Java 17 시대의 관점

- **`record` (Java 14+)** 는 인스턴스화를 막을 수 **없다** — record는 본질적으로 데이터 캐리어(data carrier)이므로 항상 인스턴스를 만들 목적으로 존재한다. 유틸리티 클래스에는 어울리지 않는다.
- **정적 분석 도구(SpotBugs, SonarQube, PMD)** 는 "인스턴스화 가능한 유틸리티 클래스"를 자동으로 경고한다. 이 경고를 끄는 표준 방법이 바로 `private` 생성자 추가다.
- **Java 21+의 패턴 매칭 / sealed 클래스** 시대에도 유틸리티 클래스의 위치는 흔들리지 않는다. 상태가 필요 없는 순수 함수 모음은 여전히 정적 메서드 모음 클래스로 표현하는 것이 가장 단순하다 (함수형 프로그래밍 스타일과도 자연스럽게 어울림).
- **Kotlin의 `object` / `fun interface`** 처럼 언어 차원에서 "유틸리티/싱글턴"을 직접 표현하는 키워드가 자바에는 없다. 그래서 자바에서는 이 "관용구(idiom)"를 외워서 쓰는 수밖에 없다.
