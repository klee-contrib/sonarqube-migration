package io.github.klee.sonar.dto.migration;

import java.util.List;

/**
 * @author Kévin Buntrock
 */
public class LanguageSearchResult {

    List<Language> languages;

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }
}
