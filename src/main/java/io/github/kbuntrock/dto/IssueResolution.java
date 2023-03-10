package io.github.kbuntrock.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kévin Buntrock
 */
public enum IssueResolution {
    FALSE_POSITIVE("FALSE-POSITIVE"),
    WONTFIX("WONTFIX"),
    FIXED("FIXED"),
    REMOVED("REMOVED");

    private static final Map<String, IssueResolution> map = new HashMap<>();

    static {
        for (IssueResolution ir : IssueResolution.values()) {
            map.put(ir.getLabel(), ir);
        }
    }

    private String label;

    IssueResolution(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static IssueResolution fromLabel(String label) {
        return map.get(label);
    }
}
