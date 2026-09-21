package ke.eworks.ripoti.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import ke.eworks.ripoti.helpline.MessageCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class OtherDepartmentInitializerTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Test
    void missingOtherDepartmentIsSeeded() {
        when(departmentRepository.findById("OTHER")).thenReturn(Optional.empty());
        final OtherDepartmentInitializer initializer = new OtherDepartmentInitializer(departmentRepository);

        initializer.run(null);

        final ArgumentCaptor<Department> department = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(department.capture());
        assertThat(department.getValue().getId()).isEqualTo("OTHER");
        assertThat(department.getValue().getName()).isEqualTo("OTHER");
        assertThat(department.getValue().getType()).isEqualTo(DepartmentType.department);
        assertThat(department.getValue().getHandlesCategories())
                .containsExactlyInAnyOrderElementsOf(java.util.Arrays.stream(MessageCategory.values())
                        .map(Enum::name)
                        .toList());
        assertThat(department.getValue().getCreatedAt()).isEqualTo(department.getValue().getUpdatedAt());
    }

    @Test
    void existingOtherDepartmentIsNotOverwritten() {
        when(departmentRepository.findById("OTHER")).thenReturn(Optional.of(new Department()));
        final OtherDepartmentInitializer initializer = new OtherDepartmentInitializer(departmentRepository);

        initializer.run(null);

        verify(departmentRepository, never()).save(any());
    }

}