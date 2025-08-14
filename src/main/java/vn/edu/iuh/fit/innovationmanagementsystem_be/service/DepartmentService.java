package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Create Department
    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest) {
        if (departmentRepository.existsByDepartmentCode(departmentRequest.getDepartmentCode())) {
            throw new IdInvalidException("Mã phòng ban đã tồn tại");
        }
        Department department = new Department();
        department.setDepartmentName(departmentRequest.getDepartmentName());
        department.setDepartmentCode(departmentRequest.getDepartmentCode());
        departmentRepository.save(department);
        return toDepartmentResponse(department);
    }

    // Get All Departments
    public ResultPaginationDTO getAllDepartments(Specification<Department> specification, Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(specification, pageable);

        Page<DepartmentResponse> departmentResponses = departments.map(department -> toDepartmentResponse(department));
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // Get Department by Id
    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return toDepartmentResponse(department);
    }

    // Update Department
    public DepartmentResponse updateDepartment(String id, DepartmentRequest departmentRequest) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tim thấy phòng ban có ID: " + id));

        // chỉ cập nhập các trường được cập nhập
        if (departmentRequest.getDepartmentName() != null) {
            department.setDepartmentName(departmentRequest.getDepartmentName());
        }
        if (departmentRequest.getDepartmentCode() != null) {
            if (department.getDepartmentCode().equals(departmentRequest.getDepartmentCode())
                    && departmentRepository.existsByDepartmentCode(departmentRequest.getDepartmentCode())) {
                throw new IdInvalidException("Mã phòng ban đã tồn tại");
            }
            department.setDepartmentCode(departmentRequest.getDepartmentCode());
        }
        departmentRepository.save(department);
        return toDepartmentResponse(department);
    }

    // Mapper
    private DepartmentResponse toDepartmentResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName(),
                department.getDepartmentCode(),
                department.getUsers() != null ? department.getUsers().size() : 0,
                department.getInnovations() != null ? department.getInnovations().size() : 0);
    }
}
