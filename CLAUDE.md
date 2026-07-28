# Effective Java 3rd Edition 학습 프로젝트

## 목적
이펙티브 자바 3판(90개 아이템)을 처음부터 끝까지 학습. 각 아이템마다 직접 코드를 작성하고 JUnit/AssertJ로 검증한다.

## 환경
- Java 17 (sourceCompatibility = targetCompatibility = 17)
- Gradle (build.gradle), `application` 플러그인 활성화
- 테스트: JUnit Jupiter 5.10.2, AssertJ 3.25.3

## 자주 쓰는 명령
```bash
# 전체 테스트
./gradlew test

# 특정 아이템 테스트만 실행
./gradlew test --tests "ch02.item01.*"

# 특정 main 클래스 실행 (예: Item 1 데모)
./gradlew run -PmainClass=ch02.item01.Order
```

## 패키지 / 문서 구조
- 소스: `src/main/java/chXX/itemYY/`  (예: `ch02/item01/`)
- 테스트: `src/test/java/chXX/itemYY/`
- 학습 노트: `docs/item-XX-{slug}.md`  (예: `docs/item-01-static-factory-methods.md`)

## 학습 워크플로우 (중요)
- **한 번에 한 아이템씩만 진행**한다. 여러 아이템을 한 번에 다루지 않는다.
- 학습 노트(`docs/item-XX-*.md`)는 **책의 개념 설명 + 프로젝트 소스 코드 예시**를 함께 담는다.
  - 일반적 JDK 예시보다 이 프로젝트에 작성한 코드를 우선 예시로 사용.
- 각 아이템 진행 순서: 개념 요약 → 예제 코드 → Learn by Doing → 테스트 → md 정리 → 다음 아이템.
- 모든 예제 클래스는 `final` (상속 금지, Item 19와 정합).
- **Learn by Doing 과제 설계 원칙**: TODO(human)은 단순 알고리즘/로직 구현이 아니라 **해당 아이템의 핵심 개념을 직접 코드로 체득하는 과제**여야 한다. (예: Item 4의 "private 생성자 + AssertionError 작성", Item 3의 "싱글턴 상태 누적 관찰")

## 진행 상황
- [x] Item 1: 생성자 대신 정적 팩터리 메서드를 고려하라 (ch02/item01) — 완료
- [x] Item 2: 생성자에 매개변수가 많을 때는 빌더를 고려하라 (ch02/item02) — 완료
- [x] Item 3: private 생성자나 열거 타입으로 싱글턴임을 보증하라 (ch02/item03) — 완료
- [x] Item 4: 인스턴스화를 막으려거든 private 생성자를 사용하라 (ch02/item04) — 완료
- [ ] Item 5: 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라 — 다음
- [ ] Item 6 ~ 90: 미진행

## 빌드 관련 참고 (Windows)
- `build.gradle`에 UTF-8 인코딩과 `junit-platform-launcher` 의존성이 설정되어 있어야 함 (Gradle 9 + 한글 조합)

## 메모리 시스템과의 관계
- 세부 학습 컨벤션과 사용자 프로필은 `C:\Users\jisung\.claude\projects\C--2026project-effective-java\memory\` 참고.
- CLAUDE.md는 저장소에 공유되는 컨텍스트, memory는 사용자 단위 컨텍스트.