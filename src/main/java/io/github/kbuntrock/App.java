package io.github.kbuntrock;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
public class App {

	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(App.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}

//	@Bean
//	public PromptProvider myPromptProvider() {
//		return () -> new AttributedString(":>", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
//	}
}
