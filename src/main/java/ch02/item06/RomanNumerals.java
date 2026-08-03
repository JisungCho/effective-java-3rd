package ch02.item06;

import java.util.regex.Pattern;

/**
 * Item 6 올바른 예: 정규표현식 Pattern 을 미리 컴파일해 static final 로 재사용한다.
 *
 * <p>"불필요한 객체 생성을 피하라"의 대표 사례.
 * {@link String#matches(String)} 는 호출될 때마다 내부에서 {@link Pattern#compile(String)}
 * 을 실행한다. 반복 호출이 잦은 코드 경로에서는 Pattern 을 한 번만 컴파일해서
 * static final 필드에 보관하고 재사용해야 한다.
 *
 * <p>핵심 Pattern 캐싱 부분은 학습자가 직접 완성했다.
 */
public final class RomanNumerals {

    /**
     * 클래스 로딩 시 정규식을 한 번만 컴파일해 캐싱한다.
     * Pattern 은 불변이므로 static final 로 보관하면 스레드 안전하게 재사용할 수 있다.
     * 반면 {@link String#matches(String)} 는 매 호출마다 Pattern.compile() 을 반복한다.
     */
    private static final Pattern ROMAN = Pattern.compile("^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

    /**
     * 입력 문자열이 로마 숫자 표기(예: "MMXXIV")에 맞는지 검사한다.
     *
     * <p>내부적으로는 (학습자가 추가할) 캐싱된 {@code ROMAN} Pattern 을 재사용한다.
     * 덕분에 {@link BadRomanNumerals#isRomanNumeral(String)} 와 "기능은 같지만
     * 객체 생성 비용은 다른" 비교가 성립한다.
     *
     * @param input 검사할 문자열. null 이면 NPE 가 발생한다.
     * @return 로마 숫자 표기에 맞으면 true
     */
    public static boolean isRomanNumeral(String input) {
        return ROMAN.matcher(input).matches();
    }
}