package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.departmentMapper = departmentMapper;
    }

    // 1. Tạo Department
    public DepartmentResponse createDepartment(@NonNull DepartmentRequest departmentRequest) {
        if (departmentRepository.existsByDepartmentCode(departmentRequest.getDepartmentCode())) {
            throw new IdInvalidException("Mã phòng ban đã tồn tại");
        }
        Department department = departmentMapper.toDepartment(departmentRequest);
        departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(department);
    }

    // 2. Lấy tất cả Departments
    public ResultPaginationDTO getAllDepartments(@NonNull Specification<Department> specification,
            @NonNull Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Department> departments = departmentRepository.findAll(specification, pageable);

        Page<DepartmentResponse> departmentResponses = departments.map(departmentMapper::toDepartmentResponse);
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 3. Lấy Department by Id
    public DepartmentResponse getDepartmentById(@NonNull String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return departmentMapper.toDepartmentResponse(department);
    }

    // 4. Cập nhật Department
    public DepartmentResponse updateDepartment(@NonNull String id, @NonNull DepartmentRequest departmentRequest) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tim thấy phòng ban có ID: " + id));

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
        return departmentMapper.toDepartmentResponse(department);
    }

    // 5. Lấy Department User Statistics
    public List<DepartmentResponse> getDepartmentUserStatistics() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    // 6. Lấy Department User Statistics by Department ID
    public DepartmentResponse getDepartmentUserStatisticsById(@NonNull String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return departmentMapper.toDepartmentResponse(department);
    }

    // 7. Tìm kiếm Department
    public ResultPaginationDTO searchDepartmentsByKeywordWithPagination(@NonNull String keyword,
            @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Department> departments = departmentRepository.findByCodeOrNameContainingWithPagination(keyword, pageable);
        Page<DepartmentResponse> departmentResponses = departments.map(departmentMapper::toDepartmentResponse);
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 8. Lấy tất cả User trong Department
    public ResultPaginationDTO getAllUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {
        // Thêm sort mặc định theo ngày tạo mới nhất trước nếu chưa có sort
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }

        Page<User> users = departmentRepository.findUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 9. Lấy Active User trong Department
    public ResultPaginationDTO getActiveUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }
        Page<User> users = departmentRepository.findActiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 10. Lấy Inactive User trong Department
    public ResultPaginationDTO getInactiveUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }
        Page<User> users = departmentRepository.findInactiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 11. Xóa User khỏi Department
    public void removeUserFromDepartment(@NonNull String departmentId, @NonNull String userId) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

        if (!user.getDepartment().getId().equals(departmentId)) {
            throw new IdInvalidException("User không thuộc phòng ban này");
        }
        user.setDepartment(null);
        userRepository.save(user);
    }
}
