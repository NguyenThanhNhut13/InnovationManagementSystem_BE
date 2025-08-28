package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentMergeHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.MergeDepartmentsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SplitDepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.DepartmentService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

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

    // 5. Get Department User Statistics (All Departments)
    @GetMapping("/departments/users/statistics")
    @ApiMessage("Lấy thống kê users theo department thành công")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentUserStatistics() {
        List<DepartmentResponse> statistics = departmentService.getDepartmentUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    // 6. Get Department User Statistics by Department ID
    @GetMapping("/departments/{id}/users/statistics")
    @ApiMessage("Lấy thống kê users của phòng ban thành công")
    public ResponseEntity<DepartmentResponse> getDepartmentUserStatisticsById(@PathVariable String id) {
        DepartmentResponse statistics = departmentService.getDepartmentUserStatisticsById(id);
        return ResponseEntity.ok(statistics);
    }

    // 7. Search Departments By Keyword With Pagination
    @GetMapping("/departments/search")
    @ApiMessage("Tìm kiếm phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> searchDepartmentsByKeywordWithPagination(
            @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(departmentService.searchDepartmentsByKeywordWithPagination(keyword, pageable));
    }

    // 8. Get all User in Department
    @GetMapping("/departments/{id}/users")
    @ApiMessage("Lấy danh sách người dùng trong phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getAllUserInDepartment(@PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllUserInDepartment(id, pageable));
    }

    // 9. Get Active User in Department
    @GetMapping("/departments/{id}/users/active")
    @ApiMessage("Lấy danh sách người dùng đang hoạt động trong phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getActiveUserInDepartment(@PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(departmentService.getActiveUserInDepartment(id, pageable));
    }

    // 10. Get Inactive User in Department
    @GetMapping("/departments/{id}/users/inactive")
    @ApiMessage("Lấy danh sách người dùng không hoạt động trong phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getInactiveUserInDepartment(@PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(departmentService.getInactiveUserInDepartment(id, pageable));
    }

    // 11. Remove User from Department
    @DeleteMapping("/departments/{departmentId}/users/{userId}")
    @ApiMessage("Xóa người dùng khỏi phòng ban thành công")
    public ResponseEntity<Void> removeUserFromDepartment(@PathVariable String departmentId,
            @PathVariable String userId) {
        departmentService.removeUserFromDepartment(departmentId, userId);
        return ResponseEntity.noContent().build();
    }

    // 12. Merge departments
    @PostMapping("/departments/merge")
    @ApiMessage("Gộp phòng ban thành công")
    public ResponseEntity<DepartmentResponse> mergeDepartments(
            @Valid @RequestBody MergeDepartmentsRequest request) {
        DepartmentResponse mergedDepartment = departmentService.mergeDepartments(request);
        return ResponseEntity.ok(mergedDepartment);
    }

    // 13. Split department
    @PostMapping("/departments/split")
    @ApiMessage("Tách phòng ban thành công")
    public ResponseEntity<List<DepartmentResponse>> splitDepartment(
            @Valid @RequestBody SplitDepartmentRequest request) {
        List<DepartmentResponse> newDepartments = departmentService.splitDepartment(request);
        return ResponseEntity.ok(newDepartments);
    }

    // 14. Get department merge history
    @GetMapping("/departments/{id}/merge-history")
    @ApiMessage("Lấy lịch sử gộp phòng ban thành công")
    public ResponseEntity<List<DepartmentMergeHistory>> getDepartmentMergeHistory(@PathVariable String id) {
        List<DepartmentMergeHistory> history = departmentService.getDepartmentMergeHistory(id);
        return ResponseEntity.ok(history);
    }

}
