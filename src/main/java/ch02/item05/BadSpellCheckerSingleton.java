package ch02.item05;

import java.util.Set;

/**
 * 잘못된 예 (2): 싱글턴으로 구현한 맞춤법 검사기.
 *
 * <p>Item 3 의 싱글턴 패턴(public static final INSTANCE)을 가져왔지만,
 * 여전히 의존 자원(사전)을 인스턴스 필드 초기화에서 직접 생성한다.
 * 싱글턴은 "정확히 하나"여야 할 때 쓰는 도구인데,
 * 맞춤법 검사기처럼 "언어마다 사전이 다를 수 있는" 자원을 들고 있으면
 * 싱글턴이라는 제약이 오히려 발목을 잡는다.
 */
public final class BadSpellCheckerSingleton {

    public static final BadSpellCheckerSingleton INSTANCE = new BadSpellCheckerSingleton();

    private final Dictionary dictionary =
            new EnglishDictionary(Set.of("hello", "world", "spell", "checker"));

    private BadSpellCheckerSingleton() { }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }
}