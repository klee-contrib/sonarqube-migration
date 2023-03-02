package io.github.kbuntrock;

import ch.qos.logback.classic.Level;
import com.auth0.jwt.JWT;
import io.github.kbuntrock.dto.IssueResolution;
import io.github.kbuntrock.dto.StatusAuthentification;
import io.github.kbuntrock.http.JsonBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author Kévin Buntrock
 */
@Component
@Command(name = "sonarqubeCommand")
public class SonarqubeCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SonarqubeCommand.class);


    @Option(names = {"-d", "--debug"}, description = "Turn on debug logs", required = false)
    boolean debug;

    @Option(names = {"-o", "--output"}, description = "Output file", required = false)
    String outputFilePath;

    // token = 437ce2bb9a4076f56bfd739cefe777572adb4d82
    @Option(names = {"-s", "--server"}, description = "SonarQube server", required = true)
    String sonarqubeServer;
    @Option(names = {"-t", "--token"}, description = "SonarQube token", required = true)
    String sonarqubeToken;

    @Option(names = {"-p", "--project"}, description = "SonarQube project id", required = true)
    String projectId;


    @Override
    public Integer call() throws Exception {

        try {
            if (debug) {
                ((ch.qos.logback.classic.Logger) LOGGER).setLevel(Level.DEBUG);
            }
            connect();
            listerIssuesAMigrer();
        } catch (final Exception ex) {
            LOGGER.error("error", ex);
            return -1;
        }
        return 0;
    }

    private void connect() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(sonarqubeServer + "/api/authentication/validate")).GET().header("Authorization", "Bearer " + sonarqubeToken) //  'Authorization' => "Bearer $token",
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<Supplier<StatusAuthentification>> response = client.send(request, JsonBodyHandler.forType(StatusAuthentification.class));
        if (!String.valueOf(response.statusCode()).startsWith("2")) {
            LOGGER.error("Connexion au serveur sonarqube impossible. Code erreur {}", response.statusCode());
            throw new RuntimeException("Connexion impossible");
        }
        StatusAuthentification statusAuthentification = response.body().get();
        if (statusAuthentification == null || !statusAuthentification.valid()) {
            LOGGER.error("Authentification au serveur sonarqube invalide. Status : {}", statusAuthentification.valid());
            throw new RuntimeException("Authentification invalide");
        }
        LOGGER.info("Connexion au serveur sonarqube ok.", statusAuthentification.valid());
    }

    //io.cassiopee:cassiopee
    //?componentKeys=fr.minint.sie:sie-app
    private void listerIssuesAMigrer() throws URISyntaxException, IOException, InterruptedException {
        // http://pic-jenkins.part.klee.lan.net:9000/api/issues/search?
        // componentKeys=fr.minint.sie%3Asie-app
        // &s=FILE_LINE
        // &resolutions=FIXED%2CWONTFIX
        // &ps=100&organization=default-organization
        // &facets=severities%2Ctypes%2Cresolutions
        // &additionalFields=_all
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://pic-jenkins.part.klee.lan.net:9000/api/issues/search?componentKeys=fr.minint.sie:sie-app" +
                        "&resolutions=FALSE-POSITIVE,WONTFIX&additionalFields=_all&severities=MAJOR")).GET()
                .header("Cookie", "JWT-SESSION=xxx")
                //.header("X-XSRF-TOKEN", "u8drhsinkmb923qranbdir9kb4")
               // .header("Authorization", "Bearer " + sonarqubeToken) //  'Authorization' => "Bearer $token",
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseS = response.body();
        LOGGER.info("Requete OK : {}", responseS);
    }

    private String extractXsrfToken() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJBWWFYV1dFTmtGRVdkaUVpeGRldyIsInN1YiI6ImtidW50cm9jayIsImlhdCI6MTY3NzU3NjQ2MiwiZXhwIjoxNjc3OTQ5MzUzLCJsYXN0UmVmcmVzaFRpbWUiOjE2Nzc1NzY0NjI2MDQsInhzcmZUb2tlbiI6InU4ZHJoc2lua21iOTIzcXJhbmJkaXI5a2I0In0.yb5TqSfry_1niMp2z2esnTWqhmtmgRnqc9G1aNGhwSI";
        return JWT.decode(jwt).getClaim("xsrfToken").asString();
    }

}