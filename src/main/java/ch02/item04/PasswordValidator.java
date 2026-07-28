package ch02.item04;

/**
 * Item 4 실습용 유틸리티 클래스: 비밀번호 검증기.
 *
 * <p>정적 검증 메서드만 제공하므로 인스턴스를 만들 이유가 없다.
 * 사용자는 {@code new PasswordValidator()}가 불가능하도록
 * <strong>private 생성자 + AssertionError</strong> 패턴을 직접 완성한다.
 */
public final class PasswordValidator {

    private PasswordValidator() {
        throw new AssertionError("PasswordValidator는 인스턴스화할 수 없습니다.");
    }


    /**
     * 비밀번호가 최소 길이를 만족하는지 검증한다.
     */
    public static boolean isLongEnough(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    /**
     * 비밀번호에 특수문자가 하나 이상 포함되어 있는지 검증한다.
     */
    public static boolean hasSpecialChar(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}