package brunotot.createyourownebookapi.openai.config;

import com.theokanning.openai.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiConfig {
    private final String token;
    private final Integer minutesTimeout;

    public OpenAiConfig(
            final @Value("${openai.token}") String token,
            final @Value("${openai.timeout.minutes}") Integer minutesTimeout
    ) {
        this.token = token;
        this.minutesTimeout = minutesTimeout;
    }

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(this.token, Duration.ofMinutes(this.minutesTimeout));
    }
}
