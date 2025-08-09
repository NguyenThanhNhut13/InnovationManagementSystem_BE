package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ApiResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        try {
            List<DepartmentResponse> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success(departments, "Lấy danh sách khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable String id) {
        try {
            DepartmentResponse department = departmentService.getDepartmentById(id);
            return ResponseEntity.ok(ApiResponse.success(department, "Lấy thông tin khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        try {
            DepartmentResponse department = departmentService.createDepartment(request);
            return ResponseEntity.ok(ApiResponse.success(department, "Tạo khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(@PathVariable String id,
            @Valid @RequestBody DepartmentRequest request) {
        try {
            DepartmentResponse department = departmentService.updateDepartment(id, request);
            return ResponseEntity.ok(ApiResponse.success(department, "Cập nhật khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDepartment(@PathVariable String id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa khoa/viện thành công", "Xóa khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> searchDepartments(@RequestParam String name) {
        try {
            List<DepartmentResponse> departments = departmentService.searchDepartmentsByName(name);
            return ResponseEntity.ok(ApiResponse.success(departments, "Tìm kiếm khoa/viện thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-code/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkDepartmentCodeExists(@PathVariable String code) {
        try {
            boolean exists = departmentService.existsByCode(code);
            return ResponseEntity.ok(ApiResponse.success(exists,
                    exists ? "Mã khoa/viện đã tồn tại" : "Mã khoa/viện có thể sử dụng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
