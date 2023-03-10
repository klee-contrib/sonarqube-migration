package io.github.kbuntrock.dto.migration;

import io.github.kbuntrock.dto.IssueResolution;

import java.util.List;

/**
 * @author Kévin Buntrock
 */
public class IssueMigration {

    public IssueMigration() {

    }

    public IssueMigration(String destinationKey, String originKey, IssueResolution resolution, List<Comment> comments, String originRule) {
        this.destinationKey = destinationKey;
        this.originKey = originKey;
        this.resolution = resolution;
        this.comments = comments;
        this.originRule = originRule;
    }

    private String destinationKey;
    private String originKey;
    private IssueResolution resolution;

    private List<Comment> comments;

    private String originRule;

    public String getDestinationKey() {
        return destinationKey;
    }

    public void setDestinationKey(String destinationKey) {
        this.destinationKey = destinationKey;
    }

    public String getOriginKey() {
        return originKey;
    }

    public void setOriginKey(String originKey) {
        this.originKey = originKey;
    }

    public IssueResolution getResolution() {
        return resolution;
    }

    public void setResolution(IssueResolution resolution) {
        this.resolution = resolution;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getOriginRule() {
        return originRule;
    }

    public void setOriginRule(String originRule) {
        this.originRule = originRule;
    }
}
