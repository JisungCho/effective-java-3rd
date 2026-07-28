package ch02.item05;

import java.util.List;

/**
 * Item 5 의존 대상 자원(사전)의 추상 타입.
 *
 * <p>SpellChecker 는 이 인터페이스만 바라보기 때문에,
 * 영어 사전이든 한국어 사전이든 테스트용 가짜 사전이든
 * 무엇이든 생성자로 주입받아 교체할 수 있다.
 * Item 5의 핵심인 "구체 클래스가 아닌 인터페이스를 의존 대상으로 삼기"를 담는다.
 */
public interface Dictionary {

    boolean contains(String word);

    List<String> suggestions(String typo);
}