package ke.eworks.ripoti.ai;

import jakarta.validation.Valid;
import ke.eworks.ripoti.helpline.HelplineMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/ai", produces = MediaType.APPLICATION_JSON_VALUE)
public class GoogleAIResource {

    private final GoogleAIService googleAIService;

    public GoogleAIResource(final GoogleAIService googleAIService) {
        this.googleAIService = googleAIService;
    }

    @PostMapping(value = "/audio-summary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HelplineMessage> summarizeAudio(
            @Valid @RequestBody final AudioSummaryRequest request) {
        return ResponseEntity.ok(googleAIService.summarize(request.audioUrl()));
    }

    @PostMapping(value = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TextPromptResponse> generateText(
            @Valid @RequestBody final TextPromptRequest request) {
        return ResponseEntity.ok(new TextPromptResponse(googleAIService.generateText(request.prompt())));
    }
}