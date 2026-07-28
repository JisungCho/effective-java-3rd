package ch02.item05;

import java.util.List;
import java.util.Set;

/**
 * 한국어 단어를 담은 사전 구현체.
 *
 * <p>EnglishDictionary 와 같은 Dictionary 인터페이스를 구현하므로
 * SpellChecker 에 그대로 주입할 수 있다 — 이것이 Item 5의 힘이다.
 */
public final class KoreanDictionary implements Dictionary {

    private final Set<String> words;

    public KoreanDictionary(Set<String> words) {
        this.words = Set.copyOf(words);
    }

    @Override
    public boolean contains(String word) {
        return words.contains(word);
    }

    @Override
    public List<String> suggestions(String typo) {
        return words.stream()
                .filter(candidate -> candidate.contains(typo) || typo.contains(candidate))
                .limit(3)
                .toList();
    }
}