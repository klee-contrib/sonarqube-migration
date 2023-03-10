package io.github.klee.sonar.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Kévin Buntrock
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    private String key;

    private String login;

    private String markdown;

    private String createdAt;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
