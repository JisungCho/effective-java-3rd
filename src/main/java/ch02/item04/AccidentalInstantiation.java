package ch02.item04;

/**
 * Item 4 — 잘못된 예 (1): 생성자를 아예 선언하지 않은 유틸리티 클래스.
 *
 * <p>컴파일러가 자동으로 <strong>public 기본 생성자</strong>를 만들어버리기 때문에,
 * 의도치 않게 인스턴스화가 가능해진다. 이 클래스는 "무엇이 잘못인지"를 보여주는
 * 반면교본(反面 교本)이므로 실무에서 따라 하면 안 된다.
 */
public class AccidentalInstantiation {

    // 생성자를 하나도 선언하지 않음 → 컴파일러가 public AccidentalInstantiation() {} 를 자동 생성
    // 결과: new AccidentalInstantiation() 이 컴파일 에러 없이 통과됨

    public static String shout(String text) {
        return text.toUpperCase();
    }
}