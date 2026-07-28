package ch02.item05;

import java.util.List;
import java.util.Objects;

/**
 * Item 5 올바른 예: 의존 객체 주입(DI) 패턴을 적용한 맞춤법 검사기.
 *
 * <p>"자원을 직접 명시하지 말고 의존 객체 주입을 사용하라"는 원칙을 코드로 체득.
 * 이 클래스는 어떤 언어의 사전을 쓸지 스스로 결정하지 않고,
 * 생성자를 통해 외부에서 {@link Dictionary}를 받아 사용한다.
 *
 * <p>주의: suggestions(String) 외의 핵심 부분(필드·생성자·isValid)은 학습자가 직접 완성한다.
 */
public final class SpellChecker {

    public List<String> suggestions(String typo) {
        return dictionary.suggestions(typo);
    }

    // TODO(human): 아래 세 가지를 모두 추가하세요.
    //   (1) Dictionary dictionary 필드를 final 로 선언
    //   (2) Dictionary 를 매개변수로 받는 public 생성자
    //       - java.util.Objects.requireNonNull(dictionary) 로 null 검사
    //   (3) public boolean isValid(String word) 메서드
    //       - dictionary.contains(word) 결과를 반환
    //
    // 목표: SpellChecker 는 Dictionary 의 "구체 타입(EnglishDictionary/KoreanDictionary)"이 아니라
    //       Dictionary "인터페이스"만 알아야 한다. 그래야 영어/한국어 사전을 자유롭게 교체할 수 있다.

    private final Dictionary dictionary;

    public SpellChecker(Dictionary dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) {
        return dictionary.contains(word);
    }
}