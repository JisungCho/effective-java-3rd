# Item 1. 생성자 대신 정적 팩터리 메서드를 고려하라

> 클래스의 인스턴스를 반환하는 단순한 정적 메서드. (디자인 패턴의 Factory Method 와는 다름)

본 문서는 책의 개념 설명을 그대로 따라가되, **예시 코드는 이 프로젝트에 작성한 소스**(`Order.java`, `Point.java`, `CancelledOrder.java`)로 대체한다.

---

## 핵심 요약

정적 팩터리 메서드는 public 생성자 대신 (또는 함께) 사용할 수 있는 강력한 대안이다. 장단점을 비교해 선택해야 한다.

---

## 5가지 장점

### 1. 이름을 가질 수 있다

- 생성자 이름은 클래스명과 동일 → 시그니처(매개변수 타입/순서)로만 구분
- 정적 팩터리는 **의도를 드러내는 이름** 사용 가능

**개선 전** — public 생성자만 사용할 때 (호출부에서 상태를 직접 지정해야 함)

```java
// 선언부
public Order(String orderId, long amount, Status status) { ... }

// 호출부 — 매개변수만으로 의도를 파악하기 어렵다
Order o1 = new Order("ORD-1", 10_000L, Status.PENDING);
Order o2 = new Order("ORD-2", 25_000L, Status.PAID);
```

**개선 후** — `Order.java` 의 정적 팩터리

```java
// 선언부: private 생성자 + 이름 있는 팩터리
private Order(String orderId, long amount, Status status) { ... }

public static Order pending(String orderId, long amount) {
    return new Order(orderId, amount, Status.PENDING);
}

public static Order paid(String orderId, long amount) {
    return new Order(orderId, amount, Status.PAID);
}

// 호출부 — 이름 자체가 의도를 드러낸다
Order o1 = Order.pending("ORD-1", 10_000L);
Order o2 = Order.paid("ORD-2", 25_000L);
```

- 생성자로 만들려면 매번 `new Order("ORD-1", 10_000L, Status.PENDING)` 처럼 `Status`까지 외부에서 결정해야 함
- 팩터리 이름 자체가 "이 주문은 PENDING 상태로 만들어진다"를 드러냄
- 도메인 언어(ubiquitous language)를 코드에 직접 녹일 수 있음

### 2. 호출될 때마다 인스턴스를 새로 만들지 않아도 된다

- 미리 만들어둔 인스턴스 캐싱/재사용 → 불필요한 객체 생성 회피 (Item 6과 연관)
- 동일한 인스턴스 보장 → `==` 비교 가능

**개선 전** — 매번 새 인스턴스를 만드는 팩터리

```java
public static Point origin() {
    return new Point(0, 0);   // 호출될 때마다 새 객체 생성
}

// 호출부
Point a = Point.origin();
Point b = Point.origin();
assertThat(a).isNotSameAs(b);   // 매번 다른 인스턴스 → 메모리 낭비
```

**개선 후** — `Point.java` 단순 `static final` 캐싱

```java
public final class Point {

    private static final Point ORIGIN = new Point(0, 0);   // 클래스 로드 시 1회 생성

    private Point(double x, double y) { ... }

    public static Point origin() {
        return ORIGIN;        // 항상 동일 인스턴스 반환
    }
}

// 호출부
Point a = Point.origin();
Point b = Point.origin();
assertThat(a).isSameAs(b);   // 동일 인스턴스 → == 비교 가능, 메모리 절약
```

- `private static final` 필드는 **클래스가 처음 로드될 때 단 한 번** 초기화되며, JVM이 스레드 안전성을 보장 (별도 `synchronized` 불필요)
- 인스턴스 통제(instance-controlled): 언제 어느 인스턴스를 살려 죽일지 통제 가능
- **참고 — lazy holder 패턴은 언제 쓰나?**
  - 초기화 비용이 크고 (DB 연결, 파일 읽기) 잘 쓰이지 않을 수 있을 때
  - `Point(0, 0)`처럼 초기화가 가벼운 객체는 단순 `static final`이 더 적합 → 이 프로젝트도 단순 방식을 채택

**프로젝트 예시 2** — `CancelledOrder.java` 싱글턴 캐싱 (TODO, 학습자 구현)

- `cancelled(orderId, refundAmount)` 팩터리가 매개변수와 무관하게 항상 동일 인스턴스를 반환
- JDK의 `Boolean.valueOf(boolean)`과 동일한 패턴

### 3. 반환 타입의 하위 타입 객체를 반환할 수 있다

- 구현 클래스를 공개하지 않고도 객체 반환 가능 → API를 작게 유지
- 자바 8 이후 인터페이스에 public 정적 메서드 가능 → 더 자연스러운 API 설계

**개선 전** — 구체 클래스를 public 으로 노출해야 하는 구조

```text
// 각 상태별 클래스가 모두 public 노출
public class PendingOrder { ... }
public class PaidOrder { ... }

// 호출부가 구체 타입에 직접 의존 → 구현 교체 어려움
PendingOrder o1 = new PendingOrder("ORD-1", 10_000L);
PaidOrder    o2 = new PaidOrder("ORD-2", 25_000L);
```

**개선 후** — 인터페이스 타입으로 반환, 구현체는 비공개

```text
public interface Order {
    static Order pending(String orderId, long amount) { return new PendingOrder(...); }
    static Order paid(String orderId, long amount)    { return new PaidOrder(...);    }
}

// 패키지 내부 구현체
final class PendingOrder implements Order { ... }
final class PaidOrder    implements Order { ... }

// 호출부 — 구체 타입을 몰라도 됨 → 구현 자유롭게 교체 가능
Order o1 = Order.pending("ORD-1", 10_000L);
Order o2 = Order.paid("ORD-2", 25_000L);
```

- 본 프로젝트 예시는 한 클래스 안에서 상태 필드로 처리하지만, 상태별 행동이 커지면 이 패턴으로 넘어가는 것이 자연스럽다

### 4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다

- 반환 타입의 하위 타입이기만 하면 어떤 클래스든 반환 가능
- 성능/메모리 최적화에 유리

**프로젝트 적용 관점**

`Order.from(csv)`가 csv 포맷(단순 vs 복합)에 따라 `SimpleOrder` 또는 `CompositeOrder`를 반환하도록 확장할 수 있다. 본 프로젝트 예시에서는 한 클래스만 사용하지만, 반환형을 `Order` 인터페이스로 두면 구현체를 자유롭게 바꿀 수 있다.

### 5. 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다

- 서비스 제공자 프레임워크(service provider framework)의 기반
- 대표 사례: JDBC (`Connection`, `DriverManager`)

이 패턴은 본 프로젝트의 단일 도메인 예시에서는 직접 다루지 않는다. 향후 결제 게이트웨이 연동 같은 확장 포인트에서 등장할 수 있다.

---

## 2가지 단점

### 1. 상속하려면 public이나 protected 생성자가 필요하다

- 정적 팩터리만 제공하면 하위 클래스를 만들 수 없음
- 하지만 이는 **컴포지션(composition)을 유도** (Item 18) → 장점이 되기도 함

**개선 전** — 상속이 가능한 구조 (의도치 않은 확장 위험)

```text
public class Order { ... }       // final 없음 → 누구나 상속 가능
public class Point { ... }

// 외부에서 이렇게 오버라이딩 할 수 있음 → 예측 불가능한 동작
class PromoOrder extends Order {
    @Override public long getAmount() { return super.getAmount() / 2; }  // 위험!
}
```

**개선 후** — `final` + private 생성자로 상속 원천 차단

```text
public final class Order {            // final → 상속 불가
    private Order(...) { ... }        // private → 외부 new 도 불가
    public static Order pending(...) { ... }
}

public final class Point { ... }
public final class CancelledOrder { ... }

// 상속 시도 자체가 컴파일 에러 → 안전
class PromoOrder extends Order { }   // ❌ 컴파일 오류
```

- 상속을 원천 차단 → 정적 팩터리의 단점1을 의식한 설계
- Item 18 관점에서는 오히려 장점

### 2. 프로그래머가 찾기 어렵다

- 생성자처럼 API 문서에 명확히 드러나지 않음
- 보완: **명명 규칙(neoteric conventions)** 을 따르면 됨

---

## 정적 팩터리 메서드 명명 규칙 (프로젝트 예시 매핑)

| 이름 | 용도 | 본 프로젝트 예시 |
|---|---|---|
| `from` | 단일 매개변수 타입 변환 | `Order.from(String csv)` |
| `of` | 여러 값을 받아 집계 | `Point.of(double x, double y)` |
| `valueOf` | (느슨한) 변환 | (사용 안 함) |
| `getInstance` | 인스턴스 반환 (캐싱 가능) | (CancelledOrder 확장 가능) |
| `newInstance` | 매번 새 인스턴스 | (사용 안 함) |
| `getType` | 다른 클래스 타입 객체 반환 | (해당 없음) |
| `newType` | 다른 클래스 새 인스턴스 | (해당 없음) |

**프로젝트에서 추가로 사용한 비규칙적 이름**: `pending`, `paid`, `origin`
- 도메인 의미를 직접 드러내기 위해 명명 규칙을 따르지 않은 사례
- 도메인에 특화된 이름이 규칙적 이름보다 가독성이 좋을 때 사용

---

## 핵심 정리

> 정적 팩터리 메서드와 public 생성자는 각자 쓰임새가 있다. **대체로 정적 팩터리가 유리한 경우가 더 많으므로** 무작정 public 생성자를 만드는 습관을 고치자.

---

## 이 프로젝트의 산출물

| 파일 | 다루는 핵심 |
|---|---|
| `src/main/java/ch02/item01/Order.java` | 이름 있는 팩터리, `from` 변환 팩터리 |
| `src/main/java/ch02/item01/Point.java` | 단순 `static final` 캐싱, `of` 팩터리 |
| `src/main/java/ch02/item01/CancelledOrder.java` | 싱글턴 캐싱 (TODO, 학습자 구현) |
| `src/test/java/ch02/item01/OrderTest.java` | 검증 |
| `src/test/java/ch02/item01/PointTest.java` | 동일 인스턴스 / 새 인스턴스 검증 |

---

## Java 17 시대의 관점

- **record** (Java 14+)는 기본적으로 public 생성자만 제공하지만, 정적 팩터리를 추가하면 더 깔끔한 API를 만들 수 있음
- **sealed** (Java 17)와 결합하면 정적 팩터리가 반환할 하위 타입 집합을 명확히 통제 가능 → Item 23의 태그 클래스 대체와 연관
- 팩터리 메서드의 반환형으로 **var** 를 쓰면 가독성이 좋아지는 경우도 있음