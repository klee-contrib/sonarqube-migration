package io.github.kbuntrock.dto;

/**
 * @author Kévin Buntrock
 */
public enum IssueResolution {
    FALSE_POSITIVE("FALSE-POSITIVE"),
    WONTFIX("WONTFIX"),
    FIXED("FIXED"),
    REMOVED("REMOVED");

    private String label;

    IssueResolution(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
