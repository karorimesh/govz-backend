package ke.eworks.ripoti.department;

import java.time.Instant;
import java.util.List;

import ke.eworks.ripoti.util.NotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(final DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDTO> findAll() {
        final List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(department -> mapToDTO(department, new DepartmentDTO()))
                .toList();
    }

    public DepartmentDTO get(final String id) {
        return departmentRepository.findById(id)
                .map(department -> mapToDTO(department, new DepartmentDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public DepartmentDTO create(final DepartmentDTO departmentDTO) {
        final Department department = new Department();
        mapToEntity(departmentDTO, department);
        final String now = Instant.now().toString();
        department.setCreatedAt(now);
        department.setUpdatedAt(now);
        return mapToDTO(departmentRepository.save(department), new DepartmentDTO());
    }

    public DepartmentDTO update(final String id, final DepartmentDTO departmentDTO) {
        final Department department = departmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(departmentDTO, department);
        department.setUpdatedAt(Instant.now().toString());
        return mapToDTO(departmentRepository.save(department), new DepartmentDTO());
    }

    public void delete(final String id) {
        departmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(final Department department, final DepartmentDTO departmentDTO) {
        departmentDTO.setId(department.getId());
        departmentDTO.setCountry(department.getCountry());
        departmentDTO.setName(department.getName());
        departmentDTO.setType(department.getType());
        departmentDTO.setDescription(department.getDescription());
        departmentDTO.setHandlesCategories(department.getHandlesCategories());
        departmentDTO.setKeywords(department.getKeywords());
        departmentDTO.setContact(department.getContact());
        departmentDTO.setEscalationOfficeId(department.getEscalationOfficeId());
        departmentDTO.setServiceLevel(department.getServiceLevel());
        departmentDTO.setCreatedAt(department.getCreatedAt());
        departmentDTO.setUpdatedAt(department.getUpdatedAt());
        return departmentDTO;
    }

    private void mapToEntity(final DepartmentDTO departmentDTO, final Department department) {
        department.setCountry(departmentDTO.getCountry());
        department.setName(departmentDTO.getName());
        department.setType(departmentDTO.getType());
        department.setDescription(departmentDTO.getDescription());
        department.setHandlesCategories(departmentDTO.getHandlesCategories());
        department.setKeywords(departmentDTO.getKeywords());
        department.setContact(departmentDTO.getContact());
        department.setEscalationOfficeId(departmentDTO.getEscalationOfficeId());
        department.setServiceLevel(departmentDTO.getServiceLevel());
    }

}
