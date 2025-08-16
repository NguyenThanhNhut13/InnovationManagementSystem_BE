package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.MergeDepartmentsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.SplitDepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserDepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentMergeHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentSplitHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentMergeHistoryRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentSplitHistoryRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final InnovationRepository innovationRepository;
    private final DepartmentMergeHistoryRepository departmentMergeHistoryRepository;
    private final DepartmentSplitHistoryRepository departmentSplitHistoryRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository,
            InnovationRepository innovationRepository,
            DepartmentMergeHistoryRepository departmentMergeHistoryRepository,
            DepartmentSplitHistoryRepository departmentSplitHistoryRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.innovationRepository = innovationRepository;
        this.departmentMergeHistoryRepository = departmentMergeHistoryRepository;
        this.departmentSplitHistoryRepository = departmentSplitHistoryRepository;
    }

    // 1. Create Department
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

    // 2. Get All Departments
    public ResultPaginationDTO getAllDepartments(Specification<Department> specification, Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(specification, pageable);

        Page<DepartmentResponse> departmentResponses = departments.map(department -> toDepartmentResponse(department));
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 3. Get Department by Id
    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return toDepartmentResponse(department);
    }

    // 4. Update Department
    public DepartmentResponse updateDepartment(String id, DepartmentRequest departmentRequest) {
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
        return toDepartmentResponse(department);
    }

    // 5. Get Department User Statistics
    public List<DepartmentResponse> getDepartmentUserStatistics() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(this::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    // 6. Get Department User Statistics by Department ID
    public DepartmentResponse getDepartmentUserStatisticsById(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        return toDepartmentResponse(department);
    }

    // 7. Search Department
    public ResultPaginationDTO searchDepartmentsByKeywordWithPagination(String keyword, Pageable pageable) {

        Page<Department> departments = departmentRepository.findByCodeOrNameContainingWithPagination(keyword, pageable);
        Page<DepartmentResponse> departmentResponses = departments.map(this::toDepartmentResponse);
        return Utils.toResultPaginationDTO(departmentResponses, pageable);
    }

    // 8. Get all User in Department
    public ResultPaginationDTO getAllUserInDepartment(String departmentId, Pageable pageable) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

        Page<User> users = departmentRepository.findUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(this::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 9. Get Active User in Department
    public ResultPaginationDTO getActiveUserInDepartment(String departmentId, Pageable pageable) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        Page<User> users = departmentRepository.findActiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(this::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 10. Get Inactive User in Department
    public ResultPaginationDTO getInactiveUserInDepartment(String departmentId, Pageable pageable) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
        Page<User> users = departmentRepository.findInactiveUsersByDepartmentId(departmentId, pageable);
        Page<UserDepartmentResponse> userResponses = users.map(this::toUserDepartmentResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 11 Remove User from Department
    public void removeUserFromDepartment(String departmentId, String userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

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
    public List<DepartmentMergeHistory> getDepartmentMergeHistory(String departmentId) {
        return departmentMergeHistoryRepository.findByMergedDepartmentId(departmentId);
    }

    // Lấy lịch sử tách department
    public List<DepartmentSplitHistory> getDepartmentSplitHistory(String departmentId) {
        List<DepartmentSplitHistory> asSource = departmentSplitHistoryRepository.findBySourceDepartmentId(departmentId);
        List<DepartmentSplitHistory> asNew = departmentSplitHistoryRepository
                .findByNewDepartmentIdsContaining(departmentId);

        List<DepartmentSplitHistory> allHistory = new ArrayList<>();
        allHistory.addAll(asSource);
        allHistory.addAll(asNew);

        return allHistory;
    }

    // Validation cho source department
    private Department validateAndGetSourceDepartment(String sourceDepartmentId) {
        Department department = departmentRepository.findById(sourceDepartmentId)
                .orElseThrow(() -> new IdInvalidException("Department cần tách không tồn tại"));

        if (!department.getIsActive()) {
            throw new IdInvalidException("Department " + department.getDepartmentName() + " đã bị xóa");
        }

        return department;
    }

    // Validation cho new department codes
    private void validateNewDepartmentCodes(List<SplitDepartmentRequest.NewDepartmentInfo> newDepartments) {
        // Kiểm tra codes không trùng lặp
        List<String> codes = newDepartments.stream()
                .map(SplitDepartmentRequest.NewDepartmentInfo::getDepartmentCode)
                .collect(Collectors.toList());

        Set<String> uniqueCodes = new HashSet<>(codes);
        if (uniqueCodes.size() != codes.size()) {
            throw new IdInvalidException("Các mã department mới không được trùng lặp");
        }

        // Kiểm tra codes không tồn tại trong database
        newDepartments.forEach(newDept -> {
            if (departmentRepository.existsByDepartmentCode(newDept.getDepartmentCode())) {
                throw new IdInvalidException("Mã phòng ban " + newDept.getDepartmentCode() + " đã tồn tại");
            }
        });
    }

    // Validation cho users có thể tách được
    private void validateUsersCanBeSplit(Department sourceDepartment,
            List<SplitDepartmentRequest.NewDepartmentInfo> newDepartments) {
        // Lấy tất cả userIds từ request
        Set<String> allRequestedUserIds = newDepartments.stream()
                .flatMap(newDept -> newDept.getUserIds().stream())
                .collect(Collectors.toSet());

        // Lấy tất cả userIds trong source department
        Set<String> sourceUserIds = sourceDepartment.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        // Kiểm tra tất cả users được yêu cầu có thuộc source department không
        if (!sourceUserIds.containsAll(allRequestedUserIds)) {
            throw new IdInvalidException("Một số users không thuộc department cần tách");
        }

        // Kiểm tra không có user nào bị bỏ sót
        if (allRequestedUserIds.size() != sourceUserIds.size()) {
            throw new IdInvalidException("Phải chuyển tất cả users trong department cần tách");
        }
    }

    // Tạo new departments
    private List<Department> createNewDepartments(List<SplitDepartmentRequest.NewDepartmentInfo> newDepartmentsInfo) {
        List<Department> newDepartments = new ArrayList<>();

        for (SplitDepartmentRequest.NewDepartmentInfo newDeptInfo : newDepartmentsInfo) {
            Department newDepartment = new Department();
            newDepartment.setDepartmentName(newDeptInfo.getDepartmentName());
            newDepartment.setDepartmentCode(newDeptInfo.getDepartmentCode());
            newDepartment.setIsActive(true);

            newDepartment = departmentRepository.save(newDepartment);
            newDepartments.add(newDepartment);
        }

        return newDepartments;
    }

    // Di chuyển users đến new departments
    private void moveUsersToNewDepartments(Department sourceDepartment, List<Department> newDepartments,
            List<SplitDepartmentRequest.NewDepartmentInfo> newDepartmentsInfo) {

        for (int i = 0; i < newDepartments.size(); i++) {
            Department newDepartment = newDepartments.get(i);
            SplitDepartmentRequest.NewDepartmentInfo newDeptInfo = newDepartmentsInfo.get(i);

            List<User> usersToMove = userRepository.findAllById(newDeptInfo.getUserIds());

            if (usersToMove.size() != newDeptInfo.getUserIds().size()) {
                throw new IdInvalidException("Một số users không tồn tại");
            }

            usersToMove.forEach(user -> {
                user.setDepartment(newDepartment);
            });

            userRepository.saveAll(usersToMove);

            // Verify transfer
            verifyUserTransfer(usersToMove, newDepartment.getId());
        }
    }

    // Di chuyển innovations đến new departments
    private void moveInnovationsToNewDepartments(Department sourceDepartment, List<Department> newDepartments,
            List<SplitDepartmentRequest.NewDepartmentInfo> newDepartmentsInfo) {

        // Lấy tất cả innovations của source department
        List<Innovation> sourceInnovations = sourceDepartment.getInnovations();

        if (!sourceInnovations.isEmpty()) {
            // Phân bổ innovations theo tỷ lệ users hoặc theo logic business
            // Ở đây sẽ phân bổ đều cho các new departments

            int innovationsPerDept = sourceInnovations.size() / newDepartments.size();
            int remainder = sourceInnovations.size() % newDepartments.size();

            int startIndex = 0;
            for (int i = 0; i < newDepartments.size(); i++) {
                Department newDepartment = newDepartments.get(i);

                // Tính số innovations cho department này
                int deptInnovationCount = innovationsPerDept + (i < remainder ? 1 : 0);

                if (deptInnovationCount > 0) {
                    List<Innovation> innovationsToMove = sourceInnovations.subList(startIndex,
                            startIndex + deptInnovationCount);

                    innovationsToMove.forEach(innovation -> {
                        innovation.setDepartment(newDepartment);
                    });

                    innovationRepository.saveAll(innovationsToMove);

                    // Verify transfer
                    verifyInnovationTransfer(innovationsToMove, newDepartment.getId());

                    startIndex += deptInnovationCount;
                }
            }
        }
    }

    // 12. Merge departments với Transaction Management và Data Consistency
    @Transactional(rollbackFor = Exception.class)
    public DepartmentResponse mergeDepartments(MergeDepartmentsRequest request) {
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

            return toDepartmentResponse(mergedDepartment);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gộp departments: " + e.getMessage(), e);
        }
    }

    // 13. Split department với Transaction Management và Data Consistency
    @Transactional(rollbackFor = Exception.class)
    public List<DepartmentResponse> splitDepartment(SplitDepartmentRequest request) {
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
                    .map(this::toDepartmentResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tách department: " + e.getMessage(), e);
        }
    }

    // Mapper
    private DepartmentResponse toDepartmentResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName(),
                department.getDepartmentCode(),
                department.getUsers() != null ? (long) department.getUsers().size() : 0L,
                department.getUsers() != null ? department.getUsers().stream()
                        .filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase("ACTIVE"))
                        .count() : 0L,
                department.getUsers() != null ? department.getUsers().stream()
                        .filter(user -> user.getStatus() != null
                                && user.getStatus().name().equalsIgnoreCase("INACTIVE"))
                        .count() : 0L,
                department.getUsers() != null ? department.getUsers().stream()
                        .filter(user -> user.getStatus() != null
                                && user.getStatus().name().equalsIgnoreCase("SUSPENDED"))
                        .count() : 0L,
                department.getInnovations() != null ? department.getInnovations().size() : 0);
    }

    private UserDepartmentResponse toUserDepartmentResponse(User user) {
        return new UserDepartmentResponse(
                user.getId(),
                user.getPersonnelId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getDepartment().getId(),
                user.getDepartment().getDepartmentName(),
                user.getDepartment().getDepartmentCode(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

}
