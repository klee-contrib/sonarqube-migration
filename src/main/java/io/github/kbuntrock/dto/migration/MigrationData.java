package io.github.kbuntrock.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.kbuntrock.dto.IssueResolution;

import java.util.*;

/**
 * @author Kévin Buntrock
 */
public class MigrationData {

    private List<Issue> originIssues = new ArrayList<>();

    private Set<Rule> rules = new HashSet<>();

    @JsonIgnore
    private Map<String, List<Issue>> destinationIssuesByComponent = new HashMap<>();

    @JsonIgnore
    private List<Issue> destinationIssues = new ArrayList<>();

    @JsonIgnore
    private Map<IssueResolution, List<IssueMigration>> issuesToTransfer = new HashMap<>();

    @JsonIgnore
    private List<Issue> nonTransferableIssues = new ArrayList<>();

    public void addOrifinIssues(List<Issue> originIssues) {
        if (originIssues != null) {
            this.originIssues.addAll(originIssues);
        }
    }

    public List<Issue> getOriginIssues() {
        return originIssues;
    }

    public void addDestinationIssues(List<Issue> destinationIssues) {
        if (destinationIssues != null) {
            this.destinationIssues.addAll(destinationIssues);
            for (Issue issue : destinationIssues) {
                if (issue.getComponent() != null) {
                    List<Issue> issues = this.destinationIssuesByComponent.computeIfAbsent(issue.getComponent(), x -> new ArrayList<>());
                    issues.add(issue);
                } else {
                    System.err.println("Issue key = " + issue.getKey() + " - hash = " + issue.getHash() + " has no component. Mapping is not implemented yet for such an issue.");
                }

            }
        }
    }

    public Map<String, List<Issue>> getDestinationIssuesByComponent() {
        return destinationIssuesByComponent;
    }

    public void addIssueMigration(IssueMigration issueMigration) {
        List<IssueMigration> issues = this.issuesToTransfer.computeIfAbsent(issueMigration.getResolution(), x -> new ArrayList<>());
        issues.add(issueMigration);
    }

    public void addNonTransferableIssue(Issue issue) {
        nonTransferableIssues.add(issue);
    }

    public List<Issue> getNonTransferableIssues() {
        return nonTransferableIssues;
    }

    public Map<IssueResolution, List<IssueMigration>> getIssuesToTransfer() {
        return issuesToTransfer;
    }

    public List<Issue> getDestinationIssues() {
        return destinationIssues;
    }

    public void clearDestinationIssues() {
        destinationIssuesByComponent.clear();
        issuesToTransfer.clear();
        nonTransferableIssues.clear();
    }

    public Set<Rule> getRules() {
        return rules;
    }
}
