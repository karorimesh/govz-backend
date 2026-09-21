package ke.eworks.ripoti.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleAIConfig {

    @Bean(destroyMethod = "close")
    public Client googleAIClient(@Value("${google.ai.api-key}") final String apiKey) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Google AI is not configured");
        }
        return Client.builder().apiKey(apiKey).build();
    }
}