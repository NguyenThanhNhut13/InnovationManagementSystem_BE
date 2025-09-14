package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentMergeHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentSplitHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.MergeDepartmentsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SplitDepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentMergeHistoryRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentSplitHistoryRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final InnovationRepository innovationRepository;
    private final DepartmentMergeHistoryRepository departmentMergeHistoryRepository;
    private final DepartmentSplitHistoryRepository departmentSplitHistoryRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository,
            InnovationRepository innovationRepository,
            DepartmentMergeHistoryRepository departmentMergeHistoryRepository,
            DepartmentSplitHistoryRepository departmentSplitHistoryRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.innovationRepository = innovationRepository;
        this.departmentMergeHistoryRepository = departmentMergeHistoryRepository;
        this.departmentSplitHistoryRepository = departmentSplitHistoryRepository;
        this.departmentMapper = departmentMapper;
    }

    // 1. Create Department
    public DepartmentResponse createDepartment(@NonNull DepartmentRequest departmentRequest) {
        if (departmentRepository.existsByDepartmentCode(departmentRequest.getDepartmentCode())) {
            throw new IdInvalidException("Mã phòng ban đã tồn tại");
        }
        Department department = departmentMapper.toDepartment(departmentRequest);
        departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(department);
    }

    // 2. Get All Departments
    public ResultPaginationDTO getAllDepartments(@NonNull Specification<Department> specification,
            @NonNull Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(specification, pageable);

        Page<DepartmentResponse> departmentResponses = departments.map(departmentMapper::toDepartmentResponse);
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 3. Get Department by Id
    public DepartmentResponse getDepartmentById(@NonNull String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return departmentMapper.toDepartmentResponse(department);
    }

    // 4. Update Department
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

    // 5. Get Department User Statistics
    public List<DepartmentResponse> getDepartmentUserStatistics() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    // 6. Get Department User Statistics by Department ID
    public DepartmentResponse getDepartmentUserStatisticsById(@NonNull String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return departmentMapper.toDepartmentResponse(department);
    }

    // 7. Search Department
    public ResultPaginationDTO searchDepartmentsByKeywordWithPagination(@NonNull String keyword,
            @NonNull Pageable pageable) {

        Page<Department> departments = departmentRepository.findByCodeOrNameContainingWithPagination(keyword, pageable);
        Page<DepartmentResponse> departmentResponses = departments.map(departmentMapper::toDepartmentResponse);
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 8. Get all User in Department
    public ResultPaginationDTO getAllUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }

        Page<User> users = departmentRepository.findUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 9. Get Active User in Department
    public ResultPaginationDTO getActiveUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }
        Page<User> users = departmentRepository.findActiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 10. Get Inactive User in Department
    public ResultPaginationDTO getInactiveUserInDepartment(@NonNull String departmentId, @NonNull Pageable pageable) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại");
        }
        Page<User> users = departmentRepository.findInactiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(departmentMapper::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 11. Remove User from Department
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

    // MERGE - SPLIT DEPARTMENT
    // Helper methods cho validation
    private List<Department> validateAndGetSourceDepartments(List<String> sourceDepartmentIds) {
        List<Department> departments = departmentRepository.findAllById(sourceDepartmentIds);

        if (departments.size() != sourceDepartmentIds.size()) {
            throw new IdInvalidException("Một số department không tồn tại");
        }

        // Kiểm tra tất cả departments đều đang hoạt động
        departments.forEach(dept -> {
            if (!dept.getIsActive()) {
                throw new IdInvalidException("Department " + dept.getDepartmentName() + " đã bị xóa");
            }
        });

        return departments;
    }

    private void validateNewDepartmentCode(String newDepartmentCode) {
        if (departmentRepository.existsByDepartmentCode(newDepartmentCode)) {
            throw new IdInvalidException("Mã phòng ban mới đã tồn tại: " + newDepartmentCode);
        }
    }

    private void validateDepartmentsCanBeMerged(List<Department> sourceDepartments) {
        // Kiểm tra không có department nào đang có innovations đang xử lý
        sourceDepartments.forEach(dept -> {
            long activeInnovations = dept.getInnovations().stream()
                    .filter(innovation -> !innovation.getStatus().equals(InnovationStatusEnum.DRAFT))
                    .count();

            if (activeInnovations > 0) {
                throw new IdInvalidException("Department " + dept.getDepartmentName() +
                        " có " + activeInnovations + " innovations đang xử lý, không thể gộp");
            }
        });
    }

    private Department createMergedDepartment(MergeDepartmentsRequest request) {
        Department mergedDepartment = new Department();
        mergedDepartment.setDepartmentName(request.getNewDepartmentName());
        mergedDepartment.setDepartmentCode(request.getNewDepartmentCode());
        mergedDepartment.setIsActive(true);
        return departmentRepository.save(mergedDepartment);
    }

    // Di chuyển users với transaction safety
    private void moveUsersToMergedDepartment(List<Department> sourceDepartments, Department mergedDepartment) {
        List<User> allUsers = new ArrayList<>();
        sourceDepartments.forEach(dept -> allUsers.addAll(dept.getUsers()));

        if (!allUsers.isEmpty()) {
            allUsers.forEach(user -> {
                user.setDepartment(mergedDepartment);
            });

            userRepository.saveAll(allUsers);

            // Verify transfer
            verifyUserTransfer(allUsers, mergedDepartment.getId());
        }
    }

    // Di chuyển innovations với transaction safety
    private void moveInnovationsToMergedDepartment(List<Department> sourceDepartments, Department mergedDepartment) {
        List<Innovation> allInnovations = new ArrayList<>();
        sourceDepartments.forEach(dept -> allInnovations.addAll(dept.getInnovations()));

        if (!allInnovations.isEmpty()) {
            allInnovations.forEach(innovation -> {
                innovation.setDepartment(mergedDepartment);
            });

            innovationRepository.saveAll(allInnovations);

            // Verify transfer
            verifyInnovationTransfer(allInnovations, mergedDepartment.getId());
        }
    }

    // Soft delete source departments
    private void softDeleteSourceDepartments(List<Department> sourceDepartments, String deletedBy, String reason) {
        sourceDepartments.forEach(dept -> {
            dept.softDelete(deletedBy, reason);
            departmentRepository.save(dept);
        });
    }

    // Lưu lịch sử gộp
    private void saveMergeHistory(Department mergedDepartment, MergeDepartmentsRequest request) {
        DepartmentMergeHistory mergeHistory = new DepartmentMergeHistory();
        mergeHistory.setMergedDepartmentId(mergedDepartment.getId());
        mergeHistory.setMergedDepartmentName(mergedDepartment.getDepartmentName());
        mergeHistory.setMergedDepartmentCode(mergedDepartment.getDepartmentCode());
        mergeHistory.setSourceDepartmentIds(request.getSourceDepartmentIds());
        mergeHistory.setMergeReason(request.getDescription());
        mergeHistory.setMergedBy(request.getMergedBy());
        departmentMergeHistoryRepository.save(mergeHistory);
    }

    // Verification methods để đảm bảo data consistency
    private void verifyUserTransfer(List<User> users, String newDepartmentId) {
        users.forEach(user -> {
            if (!user.getDepartment().getId().equals(newDepartmentId)) {
                throw new RuntimeException("Có lỗi xác thực xảy ra khi di chuyển user: " + user.getId());
            }
        });
    }

    private void verifyInnovationTransfer(List<Innovation> innovations, String newDepartmentId) {
        innovations.forEach(innovation -> {
            if (!innovation.getDepartment().getId().equals(newDepartmentId)) {
                throw new RuntimeException(
                        "Có lỗi xác thực xảy ra khi di chuyển innovation: " + innovation.getId());
            }
        });
    }

    // Lưu lịch sử tách department
    private void saveSplitHistory(Department sourceDepartment, List<Department> newDepartments,
            SplitDepartmentRequest request) {
        DepartmentSplitHistory splitHistory = new DepartmentSplitHistory();
        splitHistory.setSourceDepartmentId(sourceDepartment.getId());
        splitHistory.setSourceDepartmentName(sourceDepartment.getDepartmentName());
        splitHistory.setSourceDepartmentCode(sourceDepartment.getDepartmentCode());

        // Lưu thông tin các department mới
        List<String> newDeptIds = newDepartments.stream()
                .map(Department::getId)
                .collect(Collectors.toList());
        List<String> newDeptNames = newDepartments.stream()
                .map(Department::getDepartmentName)
                .collect(Collectors.toList());
        List<String> newDeptCodes = newDepartments.stream()
                .map(Department::getDepartmentCode)
                .collect(Collectors.toList());

        splitHistory.setNewDepartmentIds(newDeptIds);
        splitHistory.setNewDepartmentNames(newDeptNames);
        splitHistory.setNewDepartmentCodes(newDeptCodes);

        // Lưu thông tin người thực hiện và lý do
        splitHistory.setSplitBy(request.getSplitBy());
        splitHistory.setSplitReason("Tách từ " + sourceDepartment.getDepartmentName());

        departmentSplitHistoryRepository.save(splitHistory);
    }

    // Lấy lịch sử gộp department
    public List<DepartmentMergeHistory> getDepartmentMergeHistory(@NonNull String departmentId) {
        return departmentMergeHistoryRepository.findByMergedDepartmentId(departmentId);
    }

    // Lấy lịch sử tách department
    public List<DepartmentSplitHistory> getDepartmentSplitHistory(@NonNull String departmentId) {
        List<DepartmentSplitHistory> asSource = departmentSplitHistoryRepository.findBySourceDepartmentId(departmentId);
        List<DepartmentSplitHistory> asNew = departmentSplitHistoryRepository
                .findByNewDepartmentIdsContaining(departmentId);

        List<DepartmentSplitHistory> allHistory = new ArrayList<>();
        allHistory.addAll(asSource);
        allHistory.addAll(asNew);

        return allHistory;
    }

    // 12. Merge departments với Transaction Management và Data Consistency
    @Transactional(rollbackFor = Exception.class)
    public DepartmentResponse mergeDepartments(@NonNull MergeDepartmentsRequest request) {
        try {
            // Validation bước 1: Kiểm tra source departments
            List<Department> sourceDepartments = validateAndGetSourceDepartments(request.getSourceDepartmentIds());

            // Validation bước 2: Kiểm tra new department code
            validateNewDepartmentCode(request.getNewDepartmentCode());

            // Validation bước 3: Kiểm tra source departments có thể gộp được không
            validateDepartmentsCanBeMerged(sourceDepartments);

            // Tạo merged department
            Department mergedDepartment = createMergedDepartment(request);

            // Di chuyển users với transaction safety
            moveUsersToMergedDepartment(sourceDepartments, mergedDepartment);

            // Di chuyển innovations với transaction safety
            moveInnovationsToMergedDepartment(sourceDepartments, mergedDepartment);

            // Soft delete source departments
            softDeleteSourceDepartments(sourceDepartments, request.getMergedBy(),
                    "Gộp vào department: " + mergedDepartment.getDepartmentName());

            // Lưu lịch sử gộp
            saveMergeHistory(mergedDepartment, request);

            return departmentMapper.toDepartmentResponse(mergedDepartment);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gộp departments: " + e.getMessage(), e);
        }
    }

    // 13. Split department với Transaction Management và Data Consistency
    @Transactional(rollbackFor = Exception.class)
    public List<DepartmentResponse> splitDepartment(@NonNull SplitDepartmentRequest request) {
        try {
            // Validation bước 1: Kiểm tra source department
            Department sourceDepartment = departmentRepository.findById(request.getSourceDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy source department với id: " + request.getSourceDepartmentId()));

            // Validation bước 2: Kiểm tra new department codes
            for (SplitDepartmentRequest.NewDepartmentInfo newDept : request.getNewDepartments()) {
                if (departmentRepository.existsByDepartmentCode(newDept.getDepartmentCode())) {
                    throw new IdInvalidException("Mã phòng ban mới đã tồn tại: " + newDept.getDepartmentCode());
                }
            }

            // Validation bước 3: Kiểm tra users có thể tách được không
            for (SplitDepartmentRequest.NewDepartmentInfo newDept : request.getNewDepartments()) {
                for (String userId : newDept.getUserIds()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IdInvalidException("User không tồn tại: " + userId));
                    if (user.getDepartment() == null
                            || !user.getDepartment().getId().equals(sourceDepartment.getId())) {
                        throw new IdInvalidException("User " + userId + " không thuộc phòng ban nguồn");
                    }
                }
            }

            // Tạo new departments
            List<Department> newDepartments = new ArrayList<>();
            for (SplitDepartmentRequest.NewDepartmentInfo newDeptInfo : request.getNewDepartments()) {
                Department newDept = new Department();
                newDept.setDepartmentName(newDeptInfo.getDepartmentName());
                newDept.setDepartmentCode(newDeptInfo.getDepartmentCode());
                newDept.setIsActive(true);
                newDept = departmentRepository.save(newDept);
                newDepartments.add(newDept);
            }

            // Di chuyển users với transaction safety
            for (int i = 0; i < newDepartments.size(); i++) {
                Department newDept = newDepartments.get(i);
                SplitDepartmentRequest.NewDepartmentInfo newDeptInfo = request.getNewDepartments().get(i);
                for (String userId : newDeptInfo.getUserIds()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IdInvalidException("User không tồn tại: " + userId));
                    user.setDepartment(newDept);
                    userRepository.save(user);
                }
            }

            // Di chuyển innovations với transaction safety
            if (sourceDepartment.getInnovations() != null && !sourceDepartment.getInnovations().isEmpty()) {
                Department targetDept = newDepartments.get(0);
                for (Innovation innovation : sourceDepartment.getInnovations()) {
                    innovation.setDepartment(targetDept);
                    innovationRepository.save(innovation);
                }
            }

            // Soft delete source department
            sourceDepartment.softDelete(request.getSplitBy(), "Tách thành các department con");
            departmentRepository.save(sourceDepartment);

            // Lưu lịch sử tách
            saveSplitHistory(sourceDepartment, newDepartments, request);

            return newDepartments.stream()
                    .map(departmentMapper::toDepartmentResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tách department: " + e.getMessage(), e);
        }
    }

}
