package ke.eworks.ripoti.helpline;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HelplineAttachment {

    @NotBlank
    private String id;

    @NotBlank
    private String fileName;

    @NotBlank
    private String fileType;

    @NotBlank
    private String url;

}