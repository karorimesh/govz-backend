package ke.eworks.ripoti.audit;

import ke.eworks.ripoti.ai.GoogleAIService;
import ke.eworks.ripoti.helpline.HelplineMessage;
import ke.eworks.ripoti.helpline.HelplineMessageService;
import ke.eworks.ripoti.talking.TalkingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Service
public class AuditProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditProcessingService.class);

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final GoogleAIService googleAIService;
    private final HelplineMessageService helplineMessageService;

    public AuditProcessingService(final AuditRepository auditRepository,
            final ObjectMapper objectMapper,
            final GoogleAIService googleAIService,
            final HelplineMessageService helplineMessageService) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
        this.googleAIService = googleAIService;
        this.helplineMessageService = helplineMessageService;
    }

//    @Scheduled(fixedDelay = 10_000)
    public void processUnprocessedAudits() {
        LOGGER.info("Helpline Schedule starting");
        for (final AuditLog auditLog : auditRepository.findUnprocessed()) {
            process(auditLog);
        }
    }

    private void process(final AuditLog auditLog) {
        final TalkingDTO callback;
        try {
            callback = objectMapper.readValue(auditLog.getParams(), TalkingDTO.class);
        } catch (JacksonException | IllegalArgumentException exception) {
            LOGGER.warn("Skipping audit {} with invalid callback parameters", auditLog.getId(), exception);
            markProcessed(auditLog);
            return;
        }

        if (callback.getRecordingUrl() == null || callback.getRecordingUrl().isBlank()) {
            LOGGER.debug("Skipping audit {} without a recording URL", auditLog.getId());
            markProcessed(auditLog);
            return;
        }

        try {
            final HelplineMessage message = googleAIService.summarize(callback.getRecordingUrl());
            helplineMessageService.createGenerated(message);
            auditRepository.markProcessed(auditLog.getId());
            LOGGER.info("Created helpline message from audit {}", auditLog.getId());
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to process recorded audit {}; it will be retried", auditLog.getId(), exception);
        }
    }

    private void markProcessed(final AuditLog auditLog) {
        try {
            auditRepository.markProcessed(auditLog.getId());
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to mark skipped audit {} as processed", auditLog.getId(), exception);
        }
    }

}