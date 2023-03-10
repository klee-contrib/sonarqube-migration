package io.github.kbuntrock.dto.configuration;

/**
 * Configuration d'un serveur / project
 *
 * @author Kévin Buntrock
 */
public class ServerConfiguration {

    private String host;
    private String projectId;
    private String token;
    private String jwt;
    private String xsrfToken;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (host.endsWith("/")) {
            this.host = host.substring(0, host.length() - 1);
        } else {
            this.host = host;
        }
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
