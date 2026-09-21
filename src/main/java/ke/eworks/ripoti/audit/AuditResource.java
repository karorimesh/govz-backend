package ke.eworks.ripoti.audit;

import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;


@Validated
@RestController
@RequestMapping(value = "/api/audits", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuditResource {

    private final AuditService auditService;

    public AuditResource(final AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<AuditPageDTO> getAllAudits(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) final int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) final int size,
            @RequestParam(name = "requestId", required = false) final String requestId) {
        return ResponseEntity.ok(auditService.findAll(page, size, requestId));
    }

}
