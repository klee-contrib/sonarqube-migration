package io.github.kbuntrock.util;

import io.github.kbuntrock.dto.migration.Rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kévin Buntrock
 */
public final class RuleUtils {

    private static Map<String, String> normalizationMap = new HashMap();
    private static Map<String, Rule> rulesByKey = new HashMap();
    private static Map<String, String> rulesKeyEquivalence = new HashMap();

    static {
        // Console logging should not be used
        rulesKeyEquivalence.put("typescript:S2228", "typescript:S106");
        rulesKeyEquivalence.put("typescript:S106", "typescript:S2228");
    }

    public static void initRulesByName(Collection<Rule> rules) {
        rulesByKey.clear();
        for (Rule rule : rules) {
            rulesByKey.put(rule.getKey(), rule);
        }
    }

    public static boolean equivalent(String rule1, String rule2) {
        boolean strictlyEqual = rule1.equals(rule2);
        if (strictlyEqual) {
            return true;
        }
        String ruleEquivalence = rulesKeyEquivalence.get(rule1);
        if (ruleEquivalence != null && ruleEquivalence.equals(rule2)) {
            return true;
        }
        String r1 = normalizeRule(rule1);
        String r2 = normalizeRule(rule2);
        boolean normalizedNameEqual = r1.equals(r2);
        if (normalizedNameEqual) {
            return true;
        }
        Rule o1 = rulesByKey.get(rule1);
        Rule o2 = rulesByKey.get(rule2);
        return rulesByKey.get(rule1).getName().equals(rulesByKey.get(rule2).getName());
    }

    private static String normalizeRule(String rule) {
        String normalized = normalizationMap.get(rule);
        if (normalized == null) {
            normalized = rule;
            if (rule.startsWith("squid:S")) {
                normalized = rule.replaceFirst("squid:S0*", "normalized-");
            }
            if (rule.startsWith("java:S")) {
                normalized = rule.replaceFirst("java:S0*", "normalized-");
            }
            normalizationMap.put(rule, normalized);
        }
        return normalized;
    }
}
