package ke.eworks.ripoti.audit;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuditLog {

    @DocumentId
    private String id;

    private String endpoint;

    private String httpMethod;

    private String remoteAddress;

    private String requestPayload;

    private String responseBody;

    private String params;

    private String timestamp;

    private String requestId;

    private boolean processed;

}
