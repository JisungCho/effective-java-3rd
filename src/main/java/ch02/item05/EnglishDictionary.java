package ch02.item05;

import java.util.List;
import java.util.Set;

/**
 * 영어 단어를 담은 사전 구현체.
 *
 * <p>SpellChecker 와 달리 이 클래스는 자체적으로 단어 집합을 들고 있는
 * "자원(resource)"이므로, 어떤 생성자 주입 패턴과도 무관하게 단순한 데이터 캐리어다.
 */
public final class EnglishDictionary implements Dictionary {

    private final Set<String> words;

    public EnglishDictionary(Set<String> words) {
        this.words = Set.copyOf(words);
    }

    @Override
    public boolean contains(String word) {
        return words.contains(word);
    }

    @Override
    public List<String> suggestions(String typo) {
        return words.stream()
                .filter(candidate -> isLevenshteinClose(candidate, typo))
                .limit(3)
                .toList();
    }

    private static boolean isLevenshteinClose(String candidate, String typo) {
        if (Math.abs(candidate.length() - typo.length()) > 1) {
            return false;
        }
        int diff = 0;
        int min = Math.min(candidate.length(), typo.length());
        for (int i = 0; i < min && diff <= 1; i++) {
            if (candidate.charAt(i) != typo.charAt(i)) {
                diff++;
            }
        }
        return diff <= 1;
    }
}