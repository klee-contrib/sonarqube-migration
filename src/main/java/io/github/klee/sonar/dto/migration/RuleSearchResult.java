package io.github.klee.sonar.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Kévin Buntrock
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleSearchResult {

    List<Rule> rules;

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
