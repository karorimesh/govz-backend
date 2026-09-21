package ke.eworks.ripoti.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;

    public AuditService(final AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    // never propagates failures so the caller's response is unaffected by audit persistence issues
    public void record(final HttpServletRequest request, final String endpoint,
            final Object requestPayload, final String responseBody, String params, String requestId) {
        try {
            final AuditLog auditLog = new AuditLog();
            auditLog.setEndpoint(endpoint);
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setRemoteAddress(request.getRemoteAddr());
            auditLog.setRequestPayload(requestPayload.toString());
            auditLog.setParams(params);
            auditLog.setResponseBody(responseBody);
            auditLog.setTimestamp(LocalDateTime.now().toString());
            auditLog.setRequestId(requestId);
            auditLog.setProcessed(false);
            auditRepository.save(auditLog);
            LOGGER.info("Call event saved successfully: {}", params);
        } catch (final RuntimeException e) {
            LOGGER.warn("Failed to record audit log for endpoint {}", endpoint, e);
        }
    }

    public AuditPageDTO findAll(final int page, final int size, final String requestId) {
        final var auditLogs = auditRepository.findAll(page, size, requestId);
        final long totalElements = auditRepository.count(requestId);

        final AuditPageDTO result = new AuditPageDTO();
        result.setContent(auditLogs);
        result.setPage(page);
        result.setSize(size);
        result.setTotalElements(totalElements);
        result.setTotalPages((int) Math.ceil((double) totalElements / size));
        return result;
    }

}
