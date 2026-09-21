package ke.eworks.ripoti.department;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import ke.eworks.ripoti.helpline.MessageCategory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class OtherDepartmentInitializer implements ApplicationRunner {

    public static final String OTHER_DEPARTMENT_ID = "OTHER";

    private final DepartmentRepository departmentRepository;

    public OtherDepartmentInitializer(final DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(final ApplicationArguments args) {
        if (departmentRepository.findById(OTHER_DEPARTMENT_ID).isPresent()) {
            return;
        }

        final String now = Instant.now().toString();
        final Department department = new Department();
        department.setId(OTHER_DEPARTMENT_ID);
        department.setName(OTHER_DEPARTMENT_ID);
        department.setType(DepartmentType.department);
        department.setDescription("Fallback for helpline messages that do not match a specific department");
        department.setHandlesCategories(Arrays.stream(MessageCategory.values())
                .map(Enum::name)
                .toList());
        department.setKeywords(List.of("other", "unmatched", "general"));
        department.setCreatedAt(now);
        department.setUpdatedAt(now);
        departmentRepository.save(department);
    }

}