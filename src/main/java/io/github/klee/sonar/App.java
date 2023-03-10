package io.github.klee.sonar;

import org.jline.reader.impl.history.DefaultHistory;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

@SpringBootApplication
public class App {

    @Autowired
    public SonarqubeCommand sonarqubeCommand;

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(App.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);

    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString(">", AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
    }

    @Component
    public class NoSaveHistory extends DefaultHistory {
        @Override
        public void save() throws IOException {
            // nothing to do
        }
    }
}
