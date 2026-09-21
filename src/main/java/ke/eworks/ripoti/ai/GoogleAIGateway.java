package ke.eworks.ripoti.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Component;


@Component
public class GoogleAIGateway {

    private final Client client;

    public GoogleAIGateway(final Client client) {
        this.client = client;
    }

    public String generateContent(final String model, final Content content,
            final GenerateContentConfig config) {
        final GenerateContentResponse response = client.models.generateContent(model, content, config);
        return response.text();
    }

}