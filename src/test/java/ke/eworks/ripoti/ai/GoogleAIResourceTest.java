package ke.eworks.ripoti.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ke.eworks.ripoti.helpline.HelplineMessage;
import org.junit.jupiter.api.Test;


class GoogleAIResourceTest {

    @Test
    void summarizeAudioReturnsHelplineMessageDirectly() {
        final GoogleAIService googleAIService = mock(GoogleAIService.class);
        final GoogleAIResource resource = new GoogleAIResource(googleAIService);
        final HelplineMessage message = new HelplineMessage();
        message.setTitle("Water supply interruption");
        when(googleAIService.summarize("https://example.test/report.mp3")).thenReturn(message);

        final var response = resource.summarizeAudio(
                new AudioSummaryRequest("https://example.test/report.mp3"));

        assertThat(response.getBody()).isSameAs(message);
    }

}