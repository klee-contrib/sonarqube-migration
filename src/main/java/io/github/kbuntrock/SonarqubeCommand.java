package io.github.kbuntrock;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kbuntrock.dto.IssueResolution;
import io.github.kbuntrock.dto.configuration.MigrationConfiguration;
import io.github.kbuntrock.dto.configuration.ServerConfiguration;
import io.github.kbuntrock.dto.configuration.StatusAuthentification;
import io.github.kbuntrock.dto.migration.*;
import io.github.kbuntrock.http.JsonBodyHandler;
import io.github.kbuntrock.http.ObjectMapperHolder;
import io.github.kbuntrock.util.HttpUtils;
import io.github.kbuntrock.util.RuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * @author Kévin Buntrock
 */
@ShellComponent
public class SonarqubeCommand extends AbstractShellComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SonarqubeCommand.class);

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperHolder.INSTANCE.get();

    private static final String DATA_DIRECTORY_PATH = "data";
    private static final String CONFIGURATION_FILE_PATH = DATA_DIRECTORY_PATH + "/configuration.json";
    private static final String ISSUES_FILE_PATH = DATA_DIRECTORY_PATH + "/issues.json";
    private static final String TRANSFERABLE_ISSUES_FILE_PATH = DATA_DIRECTORY_PATH + "/transferable-issues.json";
    private static final String NON_TRANSFERABLE_ISSUES_FILE_PATH = DATA_DIRECTORY_PATH + "/non-transferable-issues.json";
    public static final String GROUP_CONFIGURATION = "Configuration";
    public static final String GROUP_MIGRATION = "Migration";
    public static final String GROUP_ADVANCED_USAGE = "Migration advanced usage";

    private static final HttpClient httpClient = HttpClient.newBuilder().sslContext(HttpUtils.getAcceptAllSslContext()).build();

    @Autowired
    private ComponentFlow.Builder componentFlowBuilder;

    private MigrationConfiguration migrationConfiguration;

    private MigrationData migrationData;


    @EventListener(ApplicationStartedEvent.class)
    public void runAfterStartup() throws IOException {
        System.out.println("Welcome.");
        if (loadConfiguration()) {
            System.out.println("A previously saved configuration has been loaded.");
        } else {
            System.out.println("Enter command \"configure\" in order to setup a migration.");
        }

    }

    @ShellMethod(key = "configure", value = "Configure the origin and destination server/project", group = GROUP_CONFIGURATION)
    public void configure() throws IOException {

        final String originHostId = "originHostId";
        final String destinationHostId = "destinationHostId";
        final String originProjectId = "originProjectId";
        final String destinationProjectId = "destinationProjectId";
        final String originTokenOrJwtId = "originTokenOrJwtId";
        final String destinationTokenOrJwtId = "destinationTokenOrJwtId";
        final String originJwtId = "origin-jwt";
        final String destinationJwtId = "destination-jwt";
        final String originTokenId = "origin-token";
        final String destinationTokenId = "destination-token";

        Map<String, String> tokenOrJwtSelectItems = new HashMap<>();
        tokenOrJwtSelectItems.put("token", "token");
        tokenOrJwtSelectItems.put("jwt cookie (browser impersonation)", "jwt");


        ComponentFlow flow = componentFlowBuilder.clone().reset()
                .withStringInput(originHostId)
                .name("Origin server host")
                .and()
                .withStringInput(originProjectId)
                .name("Origin project id")
                .and()
                .withSingleItemSelector(originTokenOrJwtId)
                .name("Which connection mode do you want to use? (for more information, read the documentation)")
                .selectItems(tokenOrJwtSelectItems)
                .next(ctx -> "origin-" + ctx.getResultItem().get().getItem())
                .and()
                .withStringInput(originJwtId)
                .name("Valeur du cookie JWT")
                .next(ctx -> "destinationHostId")
                .and()
                .withStringInput(originTokenId)
                .name("Valeur du token")
                .next(ctx -> "destinationHostId")
                .and()
                .withStringInput(destinationHostId)
                .name("Destination server host")
                .and()
                .withStringInput(destinationProjectId)
                .name("Destination project id")
                .and()
                .withSingleItemSelector(destinationTokenOrJwtId)
                .name("Which connection mode do you want to use? (for more information, read the documentation)")
                .selectItems(tokenOrJwtSelectItems)
                .next(ctx -> "destination-" + ctx.getResultItem().get().getItem())
                .and()
                .withStringInput(destinationJwtId)
                .name("Valeur du cookie JWT")
                .next(ctx -> null)
                .and()
                .withStringInput(destinationTokenId)
                .name("Valeur du token")
                .next(ctx -> null)
                .and()
                .build();
        ComponentContext result = flow.run().getContext();
        migrationConfiguration = new MigrationConfiguration();
        ServerConfiguration origin = new ServerConfiguration();
        origin.setHost((String) result.get(originHostId));
        origin.setProjectId((String) result.get(originProjectId));
        if (result.containsKey(originJwtId)) {
            origin.setJwt((String) result.get(originJwtId));
            origin.setXsrfToken(extractXsrfToken(origin.getJwt()));
        }
        if (result.containsKey(originTokenId)) {
            origin.setToken((String) result.get(originTokenId));
        }
        migrationConfiguration.setOrigin(origin);

        ServerConfiguration destination = new ServerConfiguration();
        destination.setHost((String) result.get(destinationHostId));
        destination.setProjectId((String) result.get(destinationProjectId));
        if (result.containsKey(destinationJwtId)) {
            destination.setJwt((String) result.get(destinationJwtId));
            destination.setXsrfToken(extractXsrfToken(destination.getJwt()));
        }
        if (result.containsKey(destinationTokenId)) {
            destination.setToken((String) result.get(destinationTokenId));
        }
        migrationConfiguration.setDestination(destination);
        printConfiguration();
        testConnectionServer();
        saveConfiguration();
    }

    public void saveConfiguration() throws IOException {
        if (migrationConfiguration == null) {
            System.out.println("There is no configuration to save");
        } else {
            Path filePath = Paths.get(CONFIGURATION_FILE_PATH);
            if (!Files.exists(filePath)) {
                try {
                    Files.createDirectory(Paths.get(DATA_DIRECTORY_PATH));
                } catch (FileAlreadyExistsException ex) {
                    // Nothing to do
                }
                Files.createFile(filePath);
            }
            OBJECT_MAPPER.writeValue(new File(CONFIGURATION_FILE_PATH), migrationConfiguration);
            System.out.println("Configuration saved.");
        }
    }

    private boolean loadConfiguration() throws IOException {

        if (Files.exists(Path.of(CONFIGURATION_FILE_PATH))) {
            migrationConfiguration = OBJECT_MAPPER.readValue(new File(CONFIGURATION_FILE_PATH), MigrationConfiguration.class);
            return true;
        }
        return false;
    }

    @ShellMethod(key = "print configuration", value = "Print the current configuration", group = GROUP_CONFIGURATION)
    public void printConfiguration() {
        ServerConfiguration origin = migrationConfiguration.getOrigin();
        ServerConfiguration destination = migrationConfiguration.getDestination();
        System.out.println("Configuration is :");
        System.out.println("Origin server");
        System.out.println("- host : " + origin.getHost());
        System.out.println("- project id : " + origin.getProjectId());
        System.out.println("- Token : " + origin.getToken());
        System.out.println("- Jwt : " + origin.getJwt());
        System.out.println("Destination server");
        System.out.println("- host : " + destination.getHost());
        System.out.println("- project id : " + destination.getProjectId());
        System.out.println("- Token : " + destination.getToken());
        System.out.println("- Jwt : " + destination.getJwt());
    }

    @ShellMethod(key = "test connection", value = "Test the connection with the origin and destination servers", group = GROUP_CONFIGURATION)
    public void testConnectionServer() {
        try {
            boolean originValidity = testConnexionServer(migrationConfiguration.getOrigin());
            System.out.println("Test connection server origin : " + (originValidity ? "OK" : "KO"));
        } catch (Exception e) {
            System.out.println("Test connection server origin KO");
        }
        try {
            boolean destinationValidity = testConnexionServer(migrationConfiguration.getDestination());
            System.out.println("Test connection server destination : " + (destinationValidity ? "OK" : "KO"));
        } catch (Exception e) {
            System.out.println("Test connection server destination : KO");
        }
    }

    private HttpRequest.Builder createBuilder(ServerConfiguration config) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        if (StringUtils.hasText(config.getToken())) {
            builder.header("Authorization", "Bearer " + config.getToken()); //  'Authorization' => "Bearer $token",
        }
        if (StringUtils.hasText(config.getJwt())) {
            builder.header("Cookie", "JWT-SESSION=" + config.getJwt());
            builder.header("X-XSRF-TOKEN", config.getXsrfToken());
        }
        return builder;
    }


    private boolean testConnexionServer(ServerConfiguration config) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder builder = createBuilder(config);
        builder.uri(new URI(config.getHost() + "/api/authentication/validate")).GET();
        HttpRequest request = builder.build();
        HttpResponse<Supplier<StatusAuthentification>> response = httpClient.send(request, JsonBodyHandler.forType(StatusAuthentification.class));
        if (!String.valueOf(response.statusCode()).startsWith("2")) {
            LOGGER.error("Connexion au serveur sonarqube impossible. Code erreur {}", response.statusCode());
            throw new RuntimeException("Connexion impossible");
        }
        StatusAuthentification statusAuthentification = response.body().get();
        if (statusAuthentification == null || !statusAuthentification.valid()) {
            LOGGER.error("Authentification au serveur sonarqube invalide. Status : {}", statusAuthentification.valid());
            return false;
        }
        return true;
    }

    @ShellMethod(key = "prepare migration", value = "Prepare the migration (find issues we want to migrate from the origin)", group = GROUP_MIGRATION)
    private void prepareMigration() throws URISyntaxException, IOException, InterruptedException {

        migrationData = new MigrationData();

        int compteur = 0;
        int maxValue = Integer.MAX_VALUE;
        int pageNumber = 1;
        while (compteur < maxValue) {
            HttpRequest request = computeSearchRequest(migrationConfiguration.getOrigin(), pageNumber, 500, true, null);
            HttpResponse<Supplier<IssueSearchResult>> response = httpClient.send(request, JsonBodyHandler.forType(IssueSearchResult.class));
            IssueSearchResult searchResult = response.body().get();
            migrationData.addOrifinIssues(searchResult.getIssues());
            maxValue = searchResult.getTotalIssues();
            compteur += searchResult.getIssues().size();
            pageNumber++;
        }

        Set<String> ruleSetOrigin = migrationData.getOriginIssues().stream().map(Issue::getRule).collect(Collectors.toSet());
        migrationData.getRules().addAll(searchRules(migrationConfiguration.getOrigin(), ruleSetOrigin));

        printMigrationData();
        saveMigrationData();
    }

    private void printMigrationData() {
        System.out.println(migrationData.getOriginIssues().size() + " won't fix and false positive issues found");
        System.out.println(migrationData.getOriginIssues().stream().filter(w -> w.getComments().size() != 0)
                .collect(Collectors.toList()).size() + " issues with comments");
    }

    private void createFile(String filePathString) throws IOException {
        Path filePath = Paths.get(filePathString);
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectory(Paths.get(DATA_DIRECTORY_PATH));
            } catch (FileAlreadyExistsException ex) {
                // Nothing to do
            }
            Files.createFile(filePath);
        }
    }

    private void saveMigrationData() throws IOException {
        createFile(ISSUES_FILE_PATH);
        OBJECT_MAPPER.writeValue(new File(ISSUES_FILE_PATH), migrationData);
        System.out.println("Migration data saved.");
    }

    private void saveMigrationTransferabeIssues() throws IOException {
        createFile(TRANSFERABLE_ISSUES_FILE_PATH);
        OBJECT_MAPPER.writeValue(new File(TRANSFERABLE_ISSUES_FILE_PATH), migrationData.getIssuesToTransfer());
    }

    private void saveMigrationNonTransferabeIssues() throws IOException {
        createFile(NON_TRANSFERABLE_ISSUES_FILE_PATH);
        OBJECT_MAPPER.writeValue(new File(NON_TRANSFERABLE_ISSUES_FILE_PATH), migrationData.getNonTransferableIssues());
    }

    private HttpRequest computeSearchRequest(ServerConfiguration config, int page, int pageSize,
                                             boolean wontFixAndFalsePositiveOnly, String languageKey) throws URISyntaxException {
        HttpRequest.Builder builder = createBuilder(config);
        String uri = config.getHost() + "/api/issues/search?" +
                "componentKeys=" + config.getProjectId() +
                "&additionalFields=_all" +
                "&p=" + page +
                "&ps=" + pageSize;
        if (wontFixAndFalsePositiveOnly) {
            uri += "&resolutions=FALSE-POSITIVE,WONTFIX";
        }
        if (languageKey != null) {
            uri += "&languages=" + languageKey;
        }
        return builder.uri(new URI(uri)).GET()
                .build();
    }

    private String extractXsrfToken(String jwt) {
        return JWT.decode(jwt).getClaim("xsrfToken").asString();
    }

    @ShellMethod(key = "load migration", value = "Load a previously saved migration data", group = GROUP_ADVANCED_USAGE)
    public void loadMigrationData() throws IOException {

        if (Files.exists(Path.of(ISSUES_FILE_PATH))) {
            migrationData = OBJECT_MAPPER.readValue(new File(ISSUES_FILE_PATH), MigrationData.class);
            System.out.println("Migration configuration loaded");
            printMigrationData();
        } else {
            System.out.println("There is no migration data to load.");
        }

    }

    @ShellMethod(key = "list rules", value = "List origin server rules.", group = GROUP_ADVANCED_USAGE)
    public void listOriginRules() throws IOException, URISyntaxException, InterruptedException {
        Set<String> ruleSet = migrationData.getOriginIssues().stream().map(Issue::getRule).collect(Collectors.toSet());
        searchRules(migrationConfiguration.getOrigin(), ruleSet);
    }

    private List<Language> listLanguagesDestination() throws URISyntaxException, IOException, InterruptedException {
        ServerConfiguration config = migrationConfiguration.getDestination();
        HttpRequest.Builder builder = createBuilder(config);
        String uri = config.getHost() + "/api/languages/list";
        HttpRequest request = builder.uri(new URI(uri)).GET().build();
        HttpResponse<Supplier<LanguageSearchResult>> response = httpClient.send(request, JsonBodyHandler.forType(LanguageSearchResult.class));
        return response.body().get().getLanguages();
    }

    private List<Issue> listIssuesOnDestination(boolean printInfos) throws URISyntaxException, IOException, InterruptedException {

        List<Issue> issues = new ArrayList<>();
        List<Language> languages = listLanguagesDestination();
        Map<String, Integer> compteurParLangage = new HashMap<>();
        for (Language language : languages) {
            int compteurLanguage = 0;
            int maxValue = Integer.MAX_VALUE;
            int pageNumber = 1;
            while (compteurLanguage < maxValue) {
                HttpRequest request = computeSearchRequest(migrationConfiguration.getDestination(), pageNumber, 500, false, language.getKey());
                HttpResponse<Supplier<IssueSearchResult>> response = httpClient.send(request, JsonBodyHandler.forType(IssueSearchResult.class));
                IssueSearchResult searchResult = response.body().get();

                if (maxValue == Integer.MAX_VALUE) {
                    maxValue = searchResult.getTotalIssues();
                }
                if (searchResult.getIssues() != null) {
                    issues.addAll(searchResult.getIssues());
                    compteurLanguage += searchResult.getIssues().size();
                    pageNumber++;
                } else if (maxValue != Integer.MAX_VALUE) {
                    System.err.println("Warning, stopping reading destination at "
                            + compteurLanguage + "/" + maxValue + " for language " + language.getName());
                    System.err.println("We believe it's a limitation / bug of the sonarqube api.");
                }
            }
            if (compteurLanguage > 0) {
                compteurParLangage.put(language.getName(), compteurLanguage);
            }

        }
        if (printInfos) {
            System.out.println("Destination has " + issues.size() + " issues listed.");
            compteurParLangage.entrySet().stream().sorted((f1, f2) -> Long.compare(f2.getValue(), f1.getValue())).forEach(x -> {
                System.out.println(x.getKey() + " : " + x.getValue());
            });
        }
        return issues;
    }


    @ShellMethod(key = "dry run migrate", value = "Show what will be the result of the migration.", group = GROUP_MIGRATION)
    public void scanDestination() throws IOException, URISyntaxException, InterruptedException {

        migrationData.clearDestinationIssues();

        migrationData.addDestinationIssues(listIssuesOnDestination(true));

        Set<String> ruleSetDestination = migrationData.getDestinationIssues().stream().map(Issue::getRule).collect(Collectors.toSet());
        migrationData.getRules().addAll(searchRules(migrationConfiguration.getDestination(), ruleSetDestination));
        saveMigrationData();
        RuleUtils.initRulesByName(migrationData.getRules());


        Set<String> set = new HashSet<>();
        int counterTransferable = 0;
        int counterCouldeBeTransferedButFixed = 0;
        int counterNonTransferable = 0;
        for (Issue originIssue : migrationData.getOriginIssues()) {
            List<Issue> issues = migrationData.getDestinationIssuesByComponent().get(originIssue.getComponent());
            if (issues != null) {
                Optional<Issue> optTransferable = issues.stream().filter(x -> sameIssue(originIssue, x)).findAny();
                if (optTransferable.isPresent()) {
                    Issue transferable = optTransferable.get();
                    if (IssueResolution.FIXED == IssueResolution.fromLabel(transferable.getResolution())) {
                        counterCouldeBeTransferedButFixed++;
                    } else {
                        counterTransferable++;
                        IssueMigration issueMigration = new IssueMigration(transferable.getKey(), originIssue.getKey(),
                                IssueResolution.fromLabel(originIssue.getResolution()), originIssue.getComments(), originIssue.getRule());
                        migrationData.addIssueMigration(issueMigration);
                        set.add(originIssue.getRule());
                    }

                } else {
                    counterNonTransferable++;
                    migrationData.addNonTransferableIssue(originIssue);
                }
            } else {
                counterNonTransferable++;
                migrationData.addNonTransferableIssue(originIssue);
            }
        }
        System.out.println(counterTransferable + " issues transferable. " + counterNonTransferable + " issues non transferable. " +
                counterCouldeBeTransferedButFixed + " issues could have been transfered but already fixed on destination.");
        saveMigrationNonTransferabeIssues();
        saveMigrationTransferabeIssues();
    }

    private boolean sameIssue(Issue origin, Issue destination) {
        // Same rule name and same text range.
        // Component has been already checked
        return RuleUtils.equivalent(origin.getRule(), destination.getRule())
                && ((origin.getTextRange() == null && destination.getTextRange() == null)
                || Objects.equals(origin.getTextRange(), destination.getTextRange()));
    }

    private List<Rule> searchRules(ServerConfiguration config, Collection<String> rules) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.Builder builder = createBuilder(config);
        List<Rule> ruleList = new ArrayList<>();
        for (String rule : rules) {
            String uri = config.getHost() + "/api/rules/search?rule_key=" + rule;
            HttpRequest request = builder.uri(new URI(uri)).GET().build();
            HttpResponse<Supplier<RuleSearchResult>> response = httpClient.send(request, JsonBodyHandler.forType(RuleSearchResult.class));
            RuleSearchResult result = response.body().get();
            if (result.getRules() != null && !result.getRules().isEmpty()) {
                ruleList.add(result.getRules().get(0));
            }
        }
        return ruleList;
    }

    @ShellMethod(key = "migrate", value = "Do the migration", group = GROUP_MIGRATION)
    private void migrate() throws URISyntaxException, IOException, InterruptedException {
        scanDestination();
        Map<IssueResolution, List<IssueMigration>> issuesToTransfer = migrationData.getIssuesToTransfer();
        for (Map.Entry<IssueResolution, List<IssueMigration>> entry : issuesToTransfer.entrySet()) {
            transitionIssuesOnDestination(entry.getValue(), entry.getKey());
            System.out.println(entry.getKey() + " applied on " + entry.getValue().size() + " issues.");
        }
        List<IssueMigration> issuesWithComment = new ArrayList<>();
        for (List<IssueMigration> issueMigrations : issuesToTransfer.values()) {
            issuesWithComment.addAll(issueMigrations.stream()
                    .filter(x -> x.getComments() != null && !x.getComments().isEmpty()).collect(Collectors.toList()));
        }
        commentOnDestination(issuesWithComment);
    }

    @ShellMethod(key = "reset destination", value = "Delete all comments and re-open all \"won't fix\"-\"false positive\" " +
            "issues on the destination project (warning : all of them, even previous ones)", group = GROUP_ADVANCED_USAGE)
    private void resetDestination() throws URISyntaxException, IOException, InterruptedException {
        List<Issue> issues = listIssuesOnDestination(false).stream()
                .filter(x -> {
                    if (x.getResolution() != null) {
                        IssueResolution resolution = IssueResolution.fromLabel(x.getResolution());
                        return IssueResolution.WONTFIX == resolution || IssueResolution.FALSE_POSITIVE == resolution;
                    }
                    return false;

                }).collect(Collectors.toList());
        for (Issue issue : issues) {
            if (issue.getComments() != null && !issue.getComments().isEmpty()) {
                for (Comment c : issue.getComments()) {
                    deleteCommentOnDestination(c);
                }
            }
        }
        reopenIssuesOnDestination(issues);
    }

    private void deleteCommentOnDestination(Comment comment) throws URISyntaxException, IOException, InterruptedException {
        ServerConfiguration config = migrationConfiguration.getDestination();
        HttpRequest.Builder builder = createBuilder(config);
        String uri = config.getHost() + "/api/issues/delete_comment?comment=" + comment.getKey();
        HttpRequest request = builder.uri(new URI(uri)).POST(HttpRequest.BodyPublishers.noBody()).build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void reopenIssuesOnDestination(Collection<Issue> issues) throws URISyntaxException, IOException, InterruptedException {

        final int chunkSize = 100;
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<Issue>> listOfList = issues.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();

        for (List<Issue> issuesChunk : listOfList) {
            ServerConfiguration config = migrationConfiguration.getDestination();
            HttpRequest.Builder builder = createBuilder(config);
            String uri = config.getHost() + "/api/issues/bulk_change?do_transition=reopen&issues="
                    + issuesChunk.stream().map(Issue::getKey).collect(Collectors.joining(","));
            HttpRequest request = builder.uri(new URI(uri)).POST(HttpRequest.BodyPublishers.noBody()).build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

    }

    private void transitionIssuesOnDestination(Collection<IssueMigration> issues, IssueResolution resolution) throws URISyntaxException, IOException, InterruptedException {

        final int chunkSize = 100;
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<IssueMigration>> listOfList = issues.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();

        String transition = "";
        if (IssueResolution.WONTFIX == resolution) {
            transition = "wontfix";
        } else if (IssueResolution.FALSE_POSITIVE == resolution) {
            transition = "falsepositive";
        } else {
            System.err.println("Transition " + resolution + " is not yet supported.");
            return;
        }


        for (List<IssueMigration> issuesChunk : listOfList) {
            ServerConfiguration config = migrationConfiguration.getDestination();
            HttpRequest.Builder builder = createBuilder(config);
            String uri = config.getHost() + "/api/issues/bulk_change?do_transition=" + transition +
                    "&issues=" + issuesChunk.stream().map(IssueMigration::getDestinationKey).collect(Collectors.joining(","));
            HttpRequest request = builder.uri(new URI(uri)).POST(HttpRequest.BodyPublishers.noBody()).build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

    }

    private void commentOnDestination(Collection<IssueMigration> issuesWithComment) throws URISyntaxException, IOException, InterruptedException {

        int compteurComments = 0;
        for (IssueMigration issueMigration : issuesWithComment) {
            for (Comment comment : issueMigration.getComments()) {
                ServerConfiguration config = migrationConfiguration.getDestination();
                HttpRequest.Builder builder = createBuilder(config);
                String uri = config.getHost() + "/api/issues/add_comment?text=" + createDestinationComment(comment) +
                        "&issue=" + issueMigration.getDestinationKey();
                HttpRequest request = builder.uri(new URI(uri)).POST(HttpRequest.BodyPublishers.noBody()).build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                compteurComments++;
            }
        }
        System.out.println(compteurComments + " comments written on " + issuesWithComment.size() + " issues.");

    }

    private String createDestinationComment(Comment originalComment) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(originalComment.getMarkdown());
        sb.append("\n\nOriginal comment creator : ");
        sb.append(originalComment.getLogin());
        return URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8.toString());
    }
}