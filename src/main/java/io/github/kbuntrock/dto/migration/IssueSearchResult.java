package io.github.kbuntrock.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Kévin Buntrock
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueSearchResult {

    @JsonProperty("total")
    private int totalIssues;

    @JsonProperty("p")
    private int pageNumber;

    @JsonProperty("ps")
    private int pageSize;

    private List<Issue> issues;

    public int getTotalIssues() {
        return totalIssues;
    }

    public void setTotalIssues(int totalIssues) {
        this.totalIssues = totalIssues;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
