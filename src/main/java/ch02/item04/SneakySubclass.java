package ch02.item04;

/**
 * Item 4 — 잘못된 예 (2)의 증거: abstract 클래스는 하위 클래스로 우회 가능.
 *
 * <p>{@link AbstractUtilityAttempt}를 상속하면 얼마든지 인스턴스를 만들 수 있다.
 * 즉 abstract 는 인스턴스화를 "막지 못한다".
 */
public class SneakySubclass extends AbstractUtilityAttempt {

    @Override
    public String decorate(String text) {
        return "[" + text + "]";
    }
}