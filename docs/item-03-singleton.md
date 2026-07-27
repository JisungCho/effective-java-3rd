# Item 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라

> 본 문서는 책의 개념 설명을 따라가되, **예시 코드는 이 프로젝트에 작성한 소스**(`DatabaseConnectionPublicFinal`, `DatabaseConnectionFactory`, `DatabaseConnectionEnum`)로 대체한다.

## 핵심 요약

싱글턴은 인스턴스가 하나뿐인 클래스를 말한다. 싱글턴을 만드는 방법은 크게 세 가지이며, **원소가 하나인 열거 타입**이 가장 권장된다.

| 방법 | 구현 | 장점 | 단점 |
|---|---|---|---|
| 1. public static final 필드 | `public static final INSTANCE = new ...` | 간결함 | API 변경 어려움, 직렬화 까다로움 |
| 2. 정적 팩터리 | `public static X getInstance()` | 유연성 (캐싱, 제네릭 등) | 리플렉션 공격에 취약, 직렬화 코드 필요 |
| 3. 열거 타입 (권장) | `enum X { INSTANCE }` | 모든 우회 차단, 직렬화 자동 | enum 특성 상속 불가 |

---

## 방법 1 — public static final 필드

`DatabaseConnectionPublicFinal.java`

```java
public final class DatabaseConnectionPublicFinal {

    public static final DatabaseConnectionPublicFinal INSTANCE = new DatabaseConnectionPublicFinal();

    private DatabaseConnectionPublicFinal() {
    }

    public void query(String sql) {
        System.out.println("[public-final] execute: " + sql);
    }
}
```

- `public static final` 필드가 인스턴스를 직접 노출
- 생성자가 `private` 이고 `public static final` 필드가 초기화를 담당 → 컴파일 타임에 보장
- **장점**: 간결. 싱글턴이라는 사실이 API에 명확히 드러남

### 단점 1: API 를 변경하지 않고는 캐싱/지연 초기화 등으로 전환 불가

**"API"** = 외부에서 보이는 부분. 위 코드에서는 `public static final INSTANCE` 필드 자체가 API 의 일부가 됨.

동료가 이렇게 쓰고 있다고 가정:

```text
DatabaseConnectionPublicFinal.INSTANCE.query("SELECT 1");
```

나중에 "이 객체 초기화가 무거우니 처음 `query()` 할 때 만들자" (lazy init) 고 결정하면:

```text
// eager (기존)
public final class DatabaseConnectionPublicFinal {
    public static final DatabaseConnectionPublicFinal INSTANCE = new DatabaseConnectionPublicFinal();  // ← 클래스 로딩 시 즉시 생성
    private DatabaseConnectionPublicFinal() { }
}

// lazy (변경)
public final class DatabaseConnectionPublicFinal {
    private static DatabaseConnectionPublicFinal instance;                                            // ← private 으로 숨김, 아직 null
    private DatabaseConnectionPublicFinal() { }
    public static DatabaseConnectionPublicFinal getInstance() {                                       // ← 새로 추가
        if (instance == null) instance = new DatabaseConnectionPublicFinal();
        return instance;
    }
}
```

→ `public static final INSTANCE` 가 사라졌으니 동료 코드가 **컴파일 에러**로 깨짐 → "API가 바뀌었다".
반대로 방법 2(정적 팩터리)는 `getInstance()` 만 public 으로 두고 내부 구현 (eager/lazy/cached) 을 자유롭게 바꿀 수 있음.

#### eager vs lazy 호출 타임라인

```text
[eager]
T0: 프로그램 시작
T1: 클래스 참조 → JVM 이 클래스 로딩 → INSTANCE = new ... 즉시 실행 (이미 존재)
T2: INSTANCE.query() → 만들어진 인스턴스 사용

[lazy]
T0: 프로그램 시작
T1: 클래스 참조 → JVM 이 클래스 로딩 → instance 필드는 null (new 아직 안 함)
T2: getInstance() 처음 호출 → if (instance == null) 통과 → 이때 new 실행
T3: getInstance() 두 번째 호출 → 이미 만들어져 있음 → 같은 인스턴스 반환
```

#### 언제 lazy 가 유용?

- 인스턴스 생성이 무거움 (DB 연결, 큰 캐시 빌드, 파일 읽기)
- 그런데 실제로 그 객체가 사용되지 않을 수도 있음 (선택적 기능)
- → 프로그램 시작부터 만들어두는 것보다 "필요할 때" 만드는 게 자원 절약

반대로 eager 가 유용한 경우: 가벼운 객체 + 거의 항상 쓰임 (예: `Boolean.TRUE`, `Collections.EMPTY_LIST`).

---

### 단점 2: 리플렉션으로 private 생성자 강제 호출 가능

자바 리플렉션 API 는 런타임에 클래스 구조를 들여다보고 조작할 수 있는 강력한 도구. `private` 생성자를 강제로 꺼낼 수 있음:

```text
import java.lang.reflect.Constructor;

Constructor<DatabaseConnectionPublicFinal> c =
        DatabaseConnectionPublicFinal.class.getDeclaredConstructor();   // private 생성자 획득
c.setAccessible(true);                                                  // 접근 제어 무시
DatabaseConnectionPublicFinal second = c.newInstance();                 // 두 번째 인스턴스!

System.out.println(second == DatabaseConnectionPublicFinal.INSTANCE);   // false → 싱글턴 깨짐
```

- `private` 는 "일반적인 호출을 막는 장치"일 뿐, `setAccessible(true)` 라는 공식 우회 명령이 존재
- 보안에 민감한 코드에서는 이 경로가 실제 위협
- **방법 1 코드만으로는 이걸 막을 수 없음** — 추가 방어 코드 (생성자 안에서 "이미 INSTANCE 가 있으면 예외" 같은) 를 직접 넣어야 함

```text
// 수동 방어 예시
private DatabaseConnectionPublicFinal() {
    if (INSTANCE != null) {
        throw new IllegalStateException("이미 인스턴스가 존재합니다");
    }
}
```

→ enum 은 JVM 자체가 막아줘서 이런 코드조차 필요 없음.

---

### 단점 3: 직렬화하려면 `implements Serializable` 만으로는 부족 → `readResolve` 추가 필요

이 클래스를 파일로 저장/복원하려면 `Serializable` 구현이 필요한데, 그것만으론 부족:

```text
public final class DatabaseConnectionPublicFinal implements Serializable {
    public static final DatabaseConnectionPublicFinal INSTANCE = new DatabaseConnectionPublicFinal();
    private DatabaseConnectionPublicFinal() { }
}
```

저장했다가 다시 읽기:

```text
// 저장
ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("db.dat"));
out.writeObject(DatabaseConnectionPublicFinal.INSTANCE);

// 다시 읽기
ObjectInputStream in = new ObjectInputStream(new FileInputStream("db.dat"));
DatabaseConnectionPublicFinal restored = (DatabaseConnectionPublicFinal) in.readObject();

// 비교
System.out.println(restored == DatabaseConnectionPublicFinal.INSTANCE);   // false → 싱글턴 깨짐
```

**왜 false?** → 역직렬화 과정에서 JVM 은 **생성자를 부르지 않고** 특별한 경로로 새 객체를 만듦. 그래서 `restored`는 `INSTANCE`와 다른 메모리의 두 번째 객체.

**해결책** — `readResolve()` 라는 JVM 과의 약속된 메서드를 추가:

```java
public final class DatabaseConnectionPublicFinal implements Serializable {
    public static final DatabaseConnectionPublicFinal INSTANCE = new DatabaseConnectionPublicFinal();
    private DatabaseConnectionPublicFinal() { }

    // 역직렬화 끝날 때 JVM 이 이 메서드를 부름
    // "새로 만든 객체 말고 이 객체로 대체해" 라고 알려주는 것
    private Object readResolve() {
        return INSTANCE;
    }
}
```

- `readResolve()` 는 일반 메서드처럼 부르는 게 아니라, JVM 이 역직렬화 완료 시점에 자동으로 부름 (magic method)
- **방법 1, 2 에서는 이걸 직접 넣어야 함** → enum 은 JVM 이 이름 기반으로 기존 상수를 찾아 자동 처리

---

## 방법 2 — 정적 팩터리 메서드

`DatabaseConnectionFactory.java`

```java
public final class DatabaseConnectionFactory {

    private static final DatabaseConnectionFactory INSTANCE = new DatabaseConnectionFactory();

    private DatabaseConnectionFactory() {
    }

    public static DatabaseConnectionFactory getInstance() {
        return INSTANCE;
    }

    public void query(String sql) {
        System.out.println("[factory] execute: " + sql);
    }
}
```

- 인스턴스는 `private static final` 으로 숨기고, `getInstance()` 정적 메서드로만 노출
- **장점**:
  - 언제든지 내부 구현 변경 가능 (캐싱, 스레드별 인스턴스, 제네릭 팩터리 등)
  - 정적 팩터리를 `Supplier<DatabaseConnectionFactory>` 같은 메서드 참조로 사용 가능
- **단점**:
  - 여전히 리플렉션 공격에 취약
  - 직렬화 시 `readResolve` 구현 필요 (안 하면 역직렬화마다 새 인스턴스 탄생)

### 직렬화 문제 시나리오

```text
// Serializable 만 구현한 상태에서
ObjectOutputStream out = ...;
out.writeObject(DatabaseConnectionFactory.getInstance());  // 직렬화

// 역직렬화
ObjectInputStream in = ...;
DatabaseConnectionFactory deserialized = (DatabaseConnectionFactory) in.readObject();
// → deserialized != INSTANCE  (새 인스턴스 생김 → 싱글턴 깨짐)

// 해결: readResolve 추가
private Object readResolve() {
    return INSTANCE;   // 역직렬화 시 기존 인스턴스 반환
}
```

---

## 방법 3 — 원소가 하나인 열거 타입 (권장)

`DatabaseConnectionEnum.java`

```java
public enum DatabaseConnectionEnum {

    INSTANCE;

    private int queryCount = 0;

    public void query(String sql) {
        queryCount++;
        System.out.println("[enum#" + queryCount + "] execute: " + sql);
    }

    public int getQueryCount() {
        return queryCount;
    }
}
```

- enum 상수 `INSTANCE` 단 하나만 선언 → 컴파일러가 자동으로 싱글턴 보장
- **장점** (방법 1, 2가 갖지 못한):
  - **직렬화 자동 처리** — JVM이 enum의 직렬화/역직렬화를 특별 취급. `readResolve` 불필요
  - **리플렉션 공격 방어** — `Constructor.newInstance()` 가 enum에 대해 예외 발생
  - 코드가 가장 간결
- **단점**:
  - enum 외의 다른 클래스를 상속할 수 없음 (enum은 암묵적으로 `final extends Enum`)
  - 지연 초기화(lazy init)가 어려움 — 클래스 로딩 시점에 상수가 모두 생성됨

### 리플렉션 공격 시나리오

```text
Constructor<DatabaseConnectionEnum> c =
    DatabaseConnectionEnum.class.getDeclaredConstructor(String.class, int.class);
c.setAccessible(true);
c.newInstance("INSTANCE", 0);   // → IllegalArgumentException: Cannot reflectively create enum objects
```

→ JVM 자체가 막음.

---

## Before / After: 세 방법 비교

### 선언부

```text
// 방법 1: public static final
public final class X {
    public static final X INSTANCE = new X();
    private X() { }
}

// 방법 2: 정적 팩터리
public final class X {
    private static final X INSTANCE = new X();
    private X() { }
    public static X getInstance() { return INSTANCE; }
}

// 방법 3: enum
public enum X {
    INSTANCE;
}
```

### 호출부

```text
// 방법 1
DatabaseConnectionPublicFinal.INSTANCE.query("SELECT 1");

// 방법 2
DatabaseConnectionFactory.getInstance().query("SELECT 1");

// 방법 3
DatabaseConnectionEnum.INSTANCE.query("SELECT 1");
```

---

## 이 코드에서 관찰한 점

### 전역 상태 누적 (DatabaseConnectionEnum)

```text
DatabaseConnectionEnum.INSTANCE.query("SELECT 1");
DatabaseConnectionEnum.INSTANCE.query("SELECT 2");
DatabaseConnectionEnum.INSTANCE.query("SELECT 3");

assertThat(DatabaseConnectionEnum.INSTANCE.getQueryCount()).isEqualTo(3);
```

- `queryCount` 는 `INSTANCE` 의 필드 → 애플리케이션 전체에서 공유
- 이게 싱글턴의 강점(공유 자원 접근)이자 동시에 위험(동시성 문제, 테스트 격리 어려움)

### 책에서 강조한 3가지 방어

| 위협 | 방법 1 (public final) | 방법 2 (factory) | 방법 3 (enum) |
|---|---|---|---|
| 리플렉션 생성자 호출 | 취약 | 취약 | **안전** (JVM 차단) |
| 직렬화/역직렬화 | 별도 `readResolve` 필요 | 별도 `readResolve` 필요 | **자동 안전** |
| 다른 클래스로더 중복 | 취약 | 취약 | 취약 (이건 어쩔 수 없음) |

---

## 핵심 정리

> 대부분의 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다. 단, 싱글턴이 꼭 필요한지 먼저 의심하라 — 전역 상태는 테스트와 동시성을 어렵게 만든다.

---

## 이 프로젝트의 산출물

| 파일 | 다루는 핵심 |
|---|---|
| `src/main/java/ch02/item03/DatabaseConnectionPublicFinal.java` | 방법 1: public static final |
| `src/main/java/ch02/item03/DatabaseConnectionFactory.java` | 방법 2: 정적 팩터리 |
| `src/main/java/ch02/item03/DatabaseConnectionEnum.java` | 방법 3: enum (권장) + 가변 상태 |
| `src/test/java/ch02/item03/DatabaseConnectionTest.java` | 세 방법의 동일 인스턴스 보장 + 상태 누적 |

---

## Java 17 시대의 관점

- **record** (Java 14+)는 싱글턴을 만들 수 없습니다 (의도된 제한). 싱글턴이 필요하면 enum 을 쓰는 메시지.
- **enum 싱글턴 + DI 프레임워크** (Spring 등) — Spring은 기본적으로 빈을 싱글턴 스코프로 관리하지만, 이는 enum 싱글턴과는 다른 메커니즘 (컨테이너가 관리). 둘을 섞어 쓰면 혼란 → 보통은 Spring 빈에 맡기는 것을 권장
- enum 싱글턴은 무상태(stateless)로 설계하는 것이 가장 안전. 상태를 두려면 `AtomicInteger`, `ConcurrentHashMap` 같은 동시성 지원 컬렉션 사용 (Item 81)