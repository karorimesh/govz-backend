package ke.eworks.ripoti.helpline;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HelplineMessageDTO {

    private String id;

    @NotBlank
    @Size(max = 255)
    private String country;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private MessageCategory category;

    @NotNull
    private MessageUrgency urgency;

    private MessageStatus status;

    @Valid
    private HelplineLocation location;

    @Valid
    private HelplineSender sender;

    @NotNull
    @Valid
    private HelplineMaskedSender maskedSender;

    @Valid
    private List<HelplineAttachment> attachments;

    @NotNull
    @Valid
    private HelplineClassification classification;

    private String submittedAt;

    private String createdAt;

    private String updatedAt;

}