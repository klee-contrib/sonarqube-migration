package io.github.klee.sonar.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.style.FigureSettings;
import org.springframework.shell.style.StyleSettings;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeSettings;

/**
 * @author Kévin Buntrock
 */
@Configuration
public class ThemeConfiguration {

    @Bean
    Theme enhancedTheme() {
        return new Theme() {
            @Override
            public String getName() {
                return "enhanced";
            }

            @Override
            public ThemeSettings getSettings() {
                return new MyThemeSettings();
            }
        };
    }

    public class MyThemeSettings extends ThemeSettings {

        @Override
        public StyleSettings styles() {
            return new MyStyleSettings();
        }

        @Override
        public FigureSettings figures() {
            return new MyFigureSettings();
        }
    }

    public class MyStyleSettings extends StyleSettings {

        public String value() {
            return "fg:!cyan";
        }

        public String listKey() {
            return "default";
        }

        public String listValue() {
            return "bold,fg:!green";
        }

        public String listLevelInfo() {
            return "fg:!green";
        }

        public String listLevelWarn() {
            return "fg:!yellow";
        }

        public String listLevelError() {
            return "fg:!red";
        }

        public String itemEnabled() {
            return "bold";
        }

        public String itemDisabled() {
            return "faint";
        }

        public String itemSelected() {
            return "fg:!green";
        }

        public String itemUnselected() {
            return "bold";
        }

        public String itemSelector() {
            return "bold,fg:bright-cyan";
        }
    }

    public class MyFigureSettings extends FigureSettings {

        @Override
        public String tick() {
            return "v";
        }

        @Override
        public String info() {
            return "i";
        }

        @Override
        public String warning() {
            return "!";
        }

        @Override
        public String error() {
            return "x";
        }

        @Override
        public String checkboxOff() {
            return "[ ]";
        }

        @Override
        public String checkboxOn() {
            return "[x]";
        }

        @Override
        public String leftwardsArrow() {
            return "<";
        }

        @Override
        public String upwardsArrow() {
            return "^";
        }

        @Override
        public String righwardsArror() {
            return ">";
        }

        @Override
        public String downwardsArror() {
            return "v";
        }

        @Override
        public String leftPointingQuotation() {
            return "<";
        }

        @Override
        public String rightPointingQuotation() {
            return ">";
        }
    }
}
