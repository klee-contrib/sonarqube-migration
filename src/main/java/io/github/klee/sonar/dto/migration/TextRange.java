package io.github.klee.sonar.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * @author Kévin Buntrock
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextRange {

    private int startLine;

    private int endLine;

    private int startOffset;

    private int endOffset;

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextRange textRange = (TextRange) o;
        boolean startAndEndLineEquals = startLine == textRange.startLine && endLine == textRange.endLine;
        boolean offsetNearBy = Math.abs(startOffset - textRange.startOffset) <= 5 && Math.abs(endOffset - textRange.endOffset) <= 5;
        return startAndEndLineEquals && offsetNearBy;
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return Objects.hash(startLine, endLine);
    }
}
