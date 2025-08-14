package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // 1. Create Department
    @PostMapping("/departments")
    @ApiMessage("Tạo phòng ban thành công")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest departmentRequest) {
        DepartmentResponse departmentResponse = departmentService.createDepartment(departmentRequest);
        return ResponseEntity.ok(departmentResponse);
    }

    // 2. Get All Departments
    @GetMapping("/departments")
    @ApiMessage("Lấy danh sách phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getAllDepartments(@Filter Specification<Department> specification,
            Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartments(specification, pageable));
    }

    // 3. Get Department by Id
    @GetMapping("/departments/{id}")
    @ApiMessage("Lấy phòng ban thành công")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable String id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    // 4. Update Department
    @PutMapping("/departments/{id}")
    @ApiMessage("Cập nhật phòng ban thành công")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable String id,
            @Valid @RequestBody DepartmentRequest departmentRequest) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentRequest));
    }
}
