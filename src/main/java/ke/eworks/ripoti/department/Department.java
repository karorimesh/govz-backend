package ke.eworks.ripoti.department;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class Department {

    @DocumentId
    private String id;

    private String country;

    private String name;

    private DepartmentType type;

    private String description;

    private List<String> handlesCategories;

    private List<String> keywords;

    private DepartmentContact contact;

    private String escalationOfficeId;

    private DepartmentServiceLevel serviceLevel;

    private String createdAt;

    private String updatedAt;

}
