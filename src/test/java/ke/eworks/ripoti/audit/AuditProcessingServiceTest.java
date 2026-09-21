package ke.eworks.ripoti.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import ke.eworks.ripoti.ai.GoogleAIService;
import ke.eworks.ripoti.helpline.HelplineMessage;
import ke.eworks.ripoti.helpline.HelplineMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
class AuditProcessingServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private GoogleAIService googleAIService;

    @Mock
    private HelplineMessageService helplineMessageService;

    private AuditProcessingService service;

    @BeforeEach
    void setUp() {
        service = new AuditProcessingService(auditRepository, new ObjectMapper(), googleAIService,
                helplineMessageService);
    }

    @Test
    void recordingIsSummarizedSavedThenMarkedProcessed() {
        final AuditLog audit = audit("audit-1", "{\"recordingUrl\":\"https://example.test/call.mp3\"}");
        final HelplineMessage message = new HelplineMessage();
        when(auditRepository.findUnprocessed()).thenReturn(List.of(audit));
        when(googleAIService.summarize("https://example.test/call.mp3")).thenReturn(message);

        service.processUnprocessedAudits();

        final InOrder order = inOrder(googleAIService, helplineMessageService, auditRepository);
        order.verify(googleAIService).summarize("https://example.test/call.mp3");
        order.verify(helplineMessageService).createGenerated(message);
        order.verify(auditRepository).markProcessed("audit-1");
    }

    @Test
    void auditWithoutRecordingIsMarkedProcessed() {
        when(auditRepository.findUnprocessed()).thenReturn(List.of(audit("audit-1", "{\"sessionId\":\"call-1\"}")));

        service.processUnprocessedAudits();

        verify(auditRepository).markProcessed("audit-1");
        verify(googleAIService, never()).summarize(any());
    }

    @Test
    void malformedAuditIsMarkedProcessed() {
        when(auditRepository.findUnprocessed()).thenReturn(List.of(audit("audit-1", "not-json")));

        service.processUnprocessedAudits();

        verify(auditRepository).markProcessed("audit-1");
        verify(googleAIService, never()).summarize(any());
    }

    @Test
    void summarizationFailureLeavesAuditUnprocessed() {
        final AuditLog audit = audit("audit-1", "{\"recordingUrl\":\"https://example.test/call.mp3\"}");
        when(auditRepository.findUnprocessed()).thenReturn(List.of(audit));
        when(googleAIService.summarize("https://example.test/call.mp3"))
                .thenThrow(new IllegalStateException("AI unavailable"));

        service.processUnprocessedAudits();

        verify(helplineMessageService, never()).createGenerated(any());
        verify(auditRepository, never()).markProcessed("audit-1");
    }

    private AuditLog audit(final String id, final String params) {
        final AuditLog audit = new AuditLog();
        audit.setId(id);
        audit.setParams(params);
        return audit;
    }

}