package ch02.item06;

/**
 * Item 6 잘못된 예: 매번 String.matches() 를 호출해 Pattern 을 반복 컴파일한다.
 *
 * <p>{@link String#matches(String)} 는 구현상 매 호출마다 {@link java.util.regex.Pattern#compile(String)}
 * 을 거친다. 즉 호출 횟수만큼 Pattern 객체가 새로 만들어지고, 곧바로 GC 대상이 된다.
 *
 * <p>이 클래스는 {@link RomanNumerals} 와 "기능은 같지만 객체 생성 비용이 다른"
 * 대조군 역할을 한다. 두 클래스를 함께 두고 성능을 비교하면 재사용의 가치가 보인다.
 */
public final class BadRomanNumerals {

    /** 책에 등장하는 로마 숫자 검증 정규식. */
    private static final String ROMAN_PATTERN =
            "^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$";

    private BadRomanNumerals() {
        throw new AssertionError("BadRomanNumerals 은 인스턴스화할 수 없습니다.");
    }

    /**
     * 입력 문자열이 로마 숫자 표기에 맞는지 검사한다.
     *
     * <p>문제점: 매 호출마다 {@code ROMAN_PATTERN} 문자열을 새로 컴파일한다.
     * 반면 {@link RomanNumerals#isRomanNumeral(String)} 은 컴파일 결과를 재사용한다.
     *
     * @param input 검사할 문자열. null 이면 NPE 가 발생한다.
     * @return 로마 숫자 표기에 맞으면 true
     */
    public static boolean isRomanNumeral(String input) {
        return input.matches(ROMAN_PATTERN);
    }
}