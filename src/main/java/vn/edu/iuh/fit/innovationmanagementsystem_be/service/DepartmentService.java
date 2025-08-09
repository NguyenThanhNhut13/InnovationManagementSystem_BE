package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/viện với ID: " + id));
        return mapToResponse(department);
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // Kiểm tra mã khoa đã tồn tại chưa
        if (departmentRepository.existsByDepartmentCode(request.getDepartmentCode())) {
            throw new RuntimeException("Mã khoa/viện đã tồn tại: " + request.getDepartmentCode());
        }

        Department department = new Department();
        department.setDepartmentName(request.getDepartmentName());
        department.setDepartmentCode(request.getDepartmentCode());

        Department savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    public DepartmentResponse updateDepartment(String id, DepartmentRequest request) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/viện với ID: " + id));

        // Kiểm tra mã khoa có bị trùng với khoa khác không
        if (!existingDepartment.getDepartmentCode().equals(request.getDepartmentCode()) &&
                departmentRepository.existsByDepartmentCode(request.getDepartmentCode())) {
            throw new RuntimeException("Mã khoa/viện đã tồn tại: " + request.getDepartmentCode());
        }

        existingDepartment.setDepartmentName(request.getDepartmentName());
        existingDepartment.setDepartmentCode(request.getDepartmentCode());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return mapToResponse(updatedDepartment);
    }

    public void deleteDepartment(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/viện với ID: " + id));

        // Kiểm tra xem khoa có người dùng hay sáng kiến không
        if (!department.getUsers().isEmpty()) {
            throw new RuntimeException("Không thể xóa khoa/viện vì còn người dùng thuộc khoa này");
        }

        if (!department.getInnovations().isEmpty()) {
            throw new RuntimeException("Không thể xóa khoa/viện vì còn sáng kiến thuộc khoa này");
        }

        departmentRepository.deleteById(id);
    }

    public List<DepartmentResponse> searchDepartmentsByName(String name) {
        List<Department> departments = departmentRepository.findByDepartmentNameContainingIgnoreCase(name);
        return departments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean existsByCode(String departmentCode) {
        return departmentRepository.existsByDepartmentCode(departmentCode);
    }

    // Private helper method to map Department to DepartmentResponse
    private DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName(),
                department.getDepartmentCode(),
                department.getUsers() != null ? department.getUsers().size() : 0,
                department.getInnovations() != null ? department.getInnovations().size() : 0);
    }
}
