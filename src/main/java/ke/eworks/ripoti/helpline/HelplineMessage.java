package ke.eworks.ripoti.helpline;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HelplineMessage {

    @DocumentId
    private String id;

    private String country;

    private String title;

    private String message;

    private MessageCategory category;

    private MessageUrgency urgency;

    private MessageStatus status;

    private HelplineLocation location;

    private HelplineSender sender;

    private HelplineMaskedSender maskedSender;

    private List<HelplineAttachment> attachments;

    private HelplineClassification classification;

    private String submittedAt;

    private String createdAt;

    private String updatedAt;

}