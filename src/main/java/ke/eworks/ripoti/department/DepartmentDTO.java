package ke.eworks.ripoti.department;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DepartmentDTO {

    private String id;

    @Size(max = 255)
    private String country;

    @NotBlank
    @Size(max = 255)
    private String name;

    private DepartmentType type;

    @Size(max = 255)
    private String description;

    private List<String> handlesCategories;

    private List<String> keywords;

    private DepartmentContact contact;

    private String escalationOfficeId;

    private DepartmentServiceLevel serviceLevel;

    private String createdAt;

    private String updatedAt;

}
