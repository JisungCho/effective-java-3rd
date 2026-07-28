package ch02.item04;

/**
 * Item 4 — 잘못된 예 (2): abstract 클래스로 인스턴스화를 막으려 한 시도.
 *
 * <p>많은 사람이 "abstract면 new 를 못 하니까 인스턴스화가 막히겠지?"라고 착각하지만,
 * 하위 클래스(여기서는 {@link SneakySubclass})를 하나 만들으면 그 객체로
 * 인스턴스화할 수 있다. 즉 abstract 는 "인스턴스화 방지"가 아니라
 * "반드시 상속해서 쓰라"는 의미일 뿐이다.
 */
public abstract class AbstractUtilityAttempt {

    public static String whisper(String text) {
        return text.toLowerCase();
    }

    /** 상속하면 쓸 수 있는 인스턴스 메서드 (이것 때문에 하위 클래스가 더 권장됨). */
    public abstract String decorate(String text);
}