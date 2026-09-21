package ke.eworks.ripoti.talking;

import jakarta.servlet.http.HttpServletRequest;
import ke.eworks.ripoti.audit.AuditService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;


@RestController
@RequestMapping(value = "/call", produces = MediaType.APPLICATION_JSON_VALUE)
public class TalkingResource {

    private final AuditService auditService;

    public TalkingResource(final AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getCall( TalkingDTO params, final HttpServletRequest request) {
         String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Response>\n" +
                "    <GetDigits timeout=\"10\" finishOnKey=\"#\">\n" +
                "        <Say voice=\"en-US-Standard-C\">" +
                "           <speak> Hello welcome to the Ripoti platform." +
                "                   Kindly select your preferred language. Select " +
                "               <say-as interpret-as=\"cardinal\">1</say-as> for English. " +
                 "              Bonyeza mbili ili kuendelea na Kiswahili." +
                "           </speak>" +
                "        </Say>\n" +
                "    </GetDigits>\n" +
                "</Response>";
        if (params.getDtmfDigits() != null){
            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Response>\n" +
                    "    <Record finishOnKey=\"#\" maxLength=\"100\" trimSilence=\"true\" playBeep=\"true\">\n" +
                    "       <Say voice=\"en-US-Standard-C\">" +
                    "           Kindly let us know how we can help. press 1 once done" +
                    "       </Say>\n" +
                    "    </Record>\n" +
                    "</Response>";
        }
        if (params.getRecordingUrl() != null){
            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Response>\n" +
                    "    <Say voice=\"en-US-Standard-C\" playBeep=\"false\" >" +
                    "       Thank you for contacting us, your request has been captured" +
                    "    </Say>\n" +
                    "</Response>";
        }
        auditService.record(request, "/call", "-", response,new ObjectMapper().writeValueAsString(params), params.getSessionId());
        return ResponseEntity.ok(response);
    }

}
