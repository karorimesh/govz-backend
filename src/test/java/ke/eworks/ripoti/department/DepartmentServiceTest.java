package ke.eworks.ripoti.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import ke.eworks.ripoti.util.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void createMapsAllFieldsAndSetsTimestamps() {
        final DepartmentDTO request = department("Kenya Revenue Authority");
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            final Department saved = invocation.getArgument(0);
            saved.setId("department-1");
            return saved;
        });

        final DepartmentDTO result = departmentService.create(request);

        assertThat(result.getId()).isEqualTo("department-1");
        assertThat(result.getCountry()).isEqualTo("Kenya");
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getType()).isEqualTo(DepartmentType.department);
        assertThat(result.getHandlesCategories()).containsExactly("tax");
        assertThat(result.getKeywords()).containsExactly("revenue");
        assertThat(result.getContact().getEmail()).isEqualTo("help@example.test");
        assertThat(result.getServiceLevel().getCritical()).isEqualTo("15 minutes");
        assertThat(result.getCreatedAt()).isNotBlank().isEqualTo(result.getUpdatedAt());
    }

    @Test
    void updatePreservesCreatedAtAndRefreshesUpdatedAt() {
        final Department existing = new Department();
        existing.setId("department-1");
        existing.setCreatedAt("2026-09-19T10:15:30Z");
        existing.setUpdatedAt("2026-09-19T10:15:30Z");
        when(departmentRepository.findById("department-1")).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final DepartmentDTO result = departmentService.update("department-1", department("Updated name"));

        assertThat(result.getCreatedAt()).isEqualTo("2026-09-19T10:15:30Z");
        assertThat(result.getUpdatedAt()).isNotEqualTo(result.getCreatedAt());
        assertThat(result.getName()).isEqualTo("Updated name");
    }

    @Test
    void missingDepartmentCannotBeUpdated() {
        when(departmentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.update("missing", department("Missing")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteChecksThatDepartmentExistsBeforeDeleting() {
        when(departmentRepository.findById("department-1")).thenReturn(Optional.of(new Department()));

        departmentService.delete("department-1");

        verify(departmentRepository).deleteById("department-1");
    }

    private DepartmentDTO department(final String name) {
        final DepartmentContact contact = new DepartmentContact();
        contact.setEmail("help@example.test");

        final DepartmentServiceLevel serviceLevel = new DepartmentServiceLevel();
        serviceLevel.setCritical("15 minutes");

        final DepartmentDTO department = new DepartmentDTO();
        department.setCountry("Kenya");
        department.setName(name);
        department.setType(DepartmentType.department);
        department.setDescription("Public service department");
        department.setHandlesCategories(List.of("tax"));
        department.setKeywords(List.of("revenue"));
        department.setContact(contact);
        department.setEscalationOfficeId("office-1");
        department.setServiceLevel(serviceLevel);
        return department;
    }

}