package ch02.item05;

import java.util.Set;

/**
 * 잘못된 예 (1): 정적 유틸리티로 구현한 맞춤법 검사기.
 *
 * <p>Item 4 의 유틸리티 클래스 패턴을 그대로 가져왔지만,
 * - final 클래스
 * - private 생성자
 * - static 메서드/필드
 * 여기서는 의존 자원(사전)을 {@code static final} 필드로 직접 생성한다.
 * 결과적으로 영어 사전 한 종류로 고정되어:
 * <ul>
 *   <li>한국어 검사를 원하면 새로운 클래스를 통째로 복사해야 한다</li>
 *   <li>테스트에서 가짜 사전(mock dictionary)을 끼워넣을 수 없다</li>
 *   <li>다른 사전 구현체로 교체하려면 소스를 직접 고쳐야 한다</li>
 * </ul>
 */
public final class BadSpellCheckerStatic {

    private static final Dictionary DICTIONARY =
            new EnglishDictionary(Set.of("hello", "world", "spell", "checker"));

    private BadSpellCheckerStatic() {
        throw new AssertionError("BadSpellCheckerStatic 은 인스턴스화할 수 없습니다.");
    }

    public static boolean isValid(String word) {
        return DICTIONARY.contains(word);
    }
}