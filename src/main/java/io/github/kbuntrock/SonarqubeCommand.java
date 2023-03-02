package io.github.kbuntrock;

import com.auth0.jwt.JWT;
import io.github.kbuntrock.dto.StatusAuthentification;
import io.github.kbuntrock.http.JsonBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.Arrays;
import java.util.List;


/**
 * @author Kévin Buntrock
 */
@ShellComponent
public class SonarqubeCommand extends AbstractShellComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SonarqubeCommand.class);

    @Autowired
    private ComponentFlow.Builder componentFlowBuilder;



    // token = 437ce2bb9a4076f56bfd739cefe777572adb4d82
    String sonarqubeServer;
    String sonarqubeToken;

    String projectId;

    @ShellMethod("Add two integers together.")
    public int add(int a, int b) {
        return a + b;
    }

    @ShellMethod(key = "test", value = "String input", group = "Components")
    public String stringInput() {
        StringInput component = new StringInput(getTerminal(), "Enter value", "myvalue");
//        component.setResourceLoader(getResourceLoader());
//        component.setTemplateExecutor(getTemplateExecutor());
//        if (mask) {
//            component.setMaskCharater('*');
//        }
        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        return "Got value " + context.getResultValue();
    }

    @ShellMethod(key = "flow showcase1", value = "Showcase", group = "Flow")
    public void showcase1() {
        Map<String, String> single1SelectItems = new HashMap<>();
        single1SelectItems.put("key1", "value1");
        single1SelectItems.put("key2", "value2");
        List<SelectItem> multi1SelectItems = Arrays.asList(SelectItem.of("key1", "value1"),
                SelectItem.of("key2", "value2"), SelectItem.of("key3", "value3"));
        ComponentFlow flow = componentFlowBuilder.clone().reset()
                .withStringInput("field1")
                .name("Field1")
                .defaultValue("defaultField1Value")
                .and()
                .withStringInput("field2")
                .name("Field2")
                .and()
                .withConfirmationInput("confirmation1")
                .name("Confirmation1")
                .and()
                .withPathInput("path1")
                .name("Path1")
                .and()
                .withSingleItemSelector("single1")
                .name("Single1")
                .selectItems(single1SelectItems)
                .and()
                .withMultiItemSelector("multi1")
                .name("Multi1")
                .selectItems(multi1SelectItems)
                .and()
                .build();
        flow.run();
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