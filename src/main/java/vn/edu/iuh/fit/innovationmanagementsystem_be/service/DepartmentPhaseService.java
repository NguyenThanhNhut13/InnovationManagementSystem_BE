package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentPhaseService {

    private final DepartmentPhaseRepository departmentPhaseRepository;
    private final DepartmentRepository departmentRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final DepartmentPhaseMapper departmentPhaseMapper;
    private final UserService userService;
    private final InnovationRoundRepository innovationRoundRepository;

    public DepartmentPhaseService(DepartmentPhaseRepository departmentPhaseRepository,
            DepartmentRepository departmentRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            DepartmentPhaseMapper departmentPhaseMapper,
            UserService userService,
            InnovationRoundRepository innovationRoundRepository) {
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.departmentRepository = departmentRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.departmentPhaseMapper = departmentPhaseMapper;
        this.userService = userService;
        this.innovationRoundRepository = innovationRoundRepository;
    }

    // 1. Create department phase with validation
    public DepartmentPhaseResponse createDepartmentPhase(String departmentId, DepartmentPhaseRequest request) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

        InnovationPhase innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy InnovationPhase với ID: " + request.getInnovationPhaseId()));

        if (innovationPhase.getPhaseType() != request.getPhaseType()) {
            throw new IdInvalidException("Loại giai đoạn không khớp với InnovationPhase");
        }

        // Validate time constraints
        validateTimeConstraints(request, innovationPhase);

        if (departmentPhaseRepository.existsByDepartmentIdAndInnovationPhaseId(departmentId,
                request.getInnovationPhaseId())) {
            throw new IdInvalidException("Khoa đã có giai đoạn cho InnovationPhase này");
        }

        // Create department phase using mapper
        DepartmentPhase departmentPhase = departmentPhaseMapper.toDepartmentPhase(request);
        departmentPhase.setDepartment(department);
        // departmentPhase.setInnovationPhase(innovationPhase);
        departmentPhase.setIsActive(true);

        DepartmentPhase savedPhase = departmentPhaseRepository.save(departmentPhase);
        return departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase);
    }

    // 2. Create phases for department based on InnovationPhase
    public List<DepartmentPhaseResponse> createPhasesForDepartmentFromInnovationPhase(String departmentId,
            String innovationPhaseId) {
        InnovationPhase innovationPhase = innovationPhaseRepository.findById(innovationPhaseId)
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + innovationPhaseId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

        // Check if already exists
        if (departmentPhaseRepository.existsByDepartmentIdAndInnovationPhaseId(departmentId, innovationPhaseId)) {
            throw new IdInvalidException("Khoa đã có giai đoạn cho InnovationPhase này");
        }

        // Create department phase with same timeframe as InnovationPhase
        DepartmentPhase departmentPhase = new DepartmentPhase();
        departmentPhase.setPhaseType(innovationPhase.getPhaseType());
        departmentPhase.setStartDate(innovationPhase.getPhaseStartDate());
        departmentPhase.setEndDate(innovationPhase.getPhaseEndDate());
        departmentPhase.setDescription(innovationPhase.getDescription() + " - " + department.getDepartmentName());
        departmentPhase.setDepartment(department);
        // departmentPhase.setInnovationPhase(innovationPhase);
        departmentPhase.setIsActive(true);

        DepartmentPhase savedPhase = departmentPhaseRepository.save(departmentPhase);
        return List.of(departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase));
    }

    // 3. Get phases by department and round
    public List<DepartmentPhaseResponse> getPhasesByDepartmentAndPhase(String departmentId, String phaseId) {
        List<DepartmentPhase> phases = departmentPhaseRepository
                .findByDepartmentIdAndInnovationPhaseIdOrderByPhaseOrder(departmentId, phaseId);
        return phases.stream()
                .map(departmentPhaseMapper::toDepartmentPhaseResponse)
                .collect(Collectors.toList());
    }

    // 4. Get current phase of department
    public DepartmentPhaseResponse getCurrentPhase(String departmentId, String phaseId) {
        DepartmentPhase phase = departmentPhaseRepository.findCurrentActivePhase(departmentId, phaseId, LocalDate.now())
                .orElse(null);
        return phase != null ? departmentPhaseMapper.toDepartmentPhaseResponse(phase) : null;
    }

    // 5. Update phase dates
    public DepartmentPhaseResponse updatePhaseDates(String phaseId, LocalDate startDate, LocalDate endDate) {
        DepartmentPhase phase = departmentPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy DepartmentPhase với ID: " + phaseId));

        // Validate time constraints
        validateTimeConstraintsForUpdate(phase, startDate, endDate);

        phase.setStartDate(startDate);
        phase.setEndDate(endDate);

        DepartmentPhase savedPhase = departmentPhaseRepository.save(phase);
        return departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase);
    }

    // 6. Update phase
    public DepartmentPhaseResponse updatePhase(String phaseId, UpdateDepartmentPhaseRequest request) {
        DepartmentPhase phase = departmentPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy DepartmentPhase với ID: " + phaseId));

        // Update only non-null fields
        if (request.getPhaseType() != null) {
            phase.setPhaseType(request.getPhaseType());
        }
        if (request.getStartDate() != null) {
            phase.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            phase.setEndDate(request.getEndDate());
        }
        if (request.getDescription() != null) {
            phase.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            phase.setIsActive(request.getIsActive());
        }
        if (request.getPhaseOrder() != null) {
            phase.setPhaseOrder(request.getPhaseOrder());
        }
        if (request.getInnovationPhaseId() != null) {
            InnovationPhase innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy InnovationPhase với ID: " + request.getInnovationPhaseId()));
            phase.setInnovationPhase(innovationPhase);
        }

        DepartmentPhase savedPhase = departmentPhaseRepository.save(phase);
        return departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase);
    }

    // 7. Toggle phase status
    public DepartmentPhaseResponse togglePhaseStatus(String phaseId, boolean isActive) {
        DepartmentPhase phase = departmentPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy DepartmentPhase với ID: " + phaseId));

        phase.setIsActive(isActive);
        DepartmentPhase savedPhase = departmentPhaseRepository.save(phase);
        return departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase);
    }

    // Private validation methods
    private void validateTimeConstraints(DepartmentPhaseRequest request, InnovationPhase innovationPhase) {

        if (!innovationPhase.isPhaseWithinPhaseTimeframe(request.getStartDate(), request.getEndDate())) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationPhase: " +
                    innovationPhase.getPhaseStartDate() + " đến " + innovationPhase.getPhaseEndDate());
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private void validateTimeConstraintsForUpdate(DepartmentPhase phase, LocalDate startDate, LocalDate endDate) {
        InnovationPhase innovationPhase = phase.getInnovationPhase();

        // Check if dates are within InnovationPhase timeframe
        if (!innovationPhase.isPhaseWithinPhaseTimeframe(startDate, endDate)) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationPhase: " +
                    innovationPhase.getPhaseStartDate() + " đến " + innovationPhase.getPhaseEndDate());
        }

        // Check if dates are within InnovationRound timeframe
        if (!innovationPhase.isPhaseWithinRoundTimeframe(startDate, endDate)) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                    innovationPhase.getInnovationRound().getRegistrationStartDate() + " đến "
                    + innovationPhase.getInnovationRound().getRegistrationEndDate());
        }

        // Check if start date is before end date
        if (startDate.isAfter(endDate)) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    // Get department ID by phase ID
    private String getDepartmentIdByPhaseId(String phaseId) {
        DepartmentPhase phase = departmentPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy DepartmentPhase với ID: " + phaseId));
        return phase.getDepartment().getId();
    }

    // Validate that current user is department head of the specified department or
    // THU_KY_QLKH_HTQT
    public void validateDepartmentHeadAccess(String departmentId) {
        User currentUser = userService.getCurrentUser();

        // Kiểm tra user có role TRUONG_KHOA hoặc THU_KY_QLKH_HTQT không
        boolean hasRequiredRole = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName()
                        .equals(UserRoleEnum.TRUONG_KHOA) ||
                        userRole.getRole().getRoleName().equals(UserRoleEnum.THU_KY_QLKH_HTQT));

        if (!hasRequiredRole) {
            throw new IdInvalidException("Access denied: User phải có role TRUONG_KHOA hoặc THU_KY_QLKH_HTQT");
        }

        // Nếu là TRUONG_KHOA, kiểm tra user có thuộc department này không
        boolean isTruongKhoa = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName().equals(UserRoleEnum.TRUONG_KHOA));

        if (isTruongKhoa
                && (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(departmentId))) {
            throw new IdInvalidException("Access denied: User này không phải là trưởng khoa của phòng ban này");
        }
    }

    /**
     * Validate that current user is department head of the department that owns the
     * specified phase or THU_KY_QLKH_HTQT
     */
    public void validateDepartmentHeadAccessForPhase(String phaseId) {
        User currentUser = userService.getCurrentUser();

        // Kiểm tra user có role TRUONG_KHOA hoặc THU_KY_QLKH_HTQT không
        boolean hasRequiredRole = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName()
                        .equals(UserRoleEnum.TRUONG_KHOA) ||
                        userRole.getRole().getRoleName().equals(UserRoleEnum.THU_KY_QLKH_HTQT));

        if (!hasRequiredRole) {
            throw new IdInvalidException("Access denied: User must have TRUONG_KHOA or THU_KY_QLKH_HTQT role");
        }

        // Nếu là TRUONG_KHOA, kiểm tra user có thuộc department này không
        boolean isTruongKhoa = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName().equals(UserRoleEnum.TRUONG_KHOA));

        if (isTruongKhoa) {
            // Lấy departmentId từ phaseId thông qua service
            String departmentId = getDepartmentIdByPhaseId(phaseId);

            // Kiểm tra user có thuộc department này không
            if (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(departmentId)) {
                throw new IdInvalidException("Access denied: User is not the head of this department");
            }
        }
    }

    // 8. Create all 3 required phases for department
//    public List<DepartmentPhaseResponse> createAllRequiredPhasesForDepartment(String departmentId, String roundId) {
//        Department department = departmentRepository.findById(departmentId)
//                .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));
//
//        innovationRoundRepository.findById(roundId)
//                .orElseThrow(() -> new IdInvalidException("Không tìm thấy round với ID: " + roundId));
//
//        // Get all innovation phases for this round
//        List<InnovationPhase> innovationPhases = innovationPhaseRepository
//                .findByInnovationRoundIdOrderByPhaseOrder(roundId);
//
//        if (innovationPhases.isEmpty()) {
//            throw new IdInvalidException("Round này chưa có giai đoạn nào");
//        }
//
//        // Check if department already has phases for this round
//        boolean hasExistingPhases = innovationPhases.stream()
//                .anyMatch(phase -> departmentPhaseRepository.existsByDepartmentIdAndInnovationPhaseId(departmentId,
//                        phase.getId()));
//
//        if (hasExistingPhases) {
//            throw new IdInvalidException("Khoa đã có giai đoạn cho round này");
//        }
//
//        List<DepartmentPhaseResponse> createdPhases = new ArrayList<>();
//
//        // Create phases for each innovation phase
//        for (InnovationPhase innovationPhase : innovationPhases) {
//            // Only create phases for the 3 required types
//            if (isRequiredPhaseType(innovationPhase.getPhaseType())) {
//                DepartmentPhase departmentPhase = new DepartmentPhase();
//                departmentPhase.setPhaseType(innovationPhase.getPhaseType());
//                departmentPhase.setStartDate(innovationPhase.getPhaseStartDate());
//                departmentPhase.setEndDate(innovationPhase.getPhaseEndDate());
//                departmentPhase
//                        .setDescription(innovationPhase.getDescription() + " - " + department.getDepartmentName());
//                departmentPhase.setDepartment(department);
//                departmentPhase.setInnovationPhase(innovationPhase);
//                departmentPhase.setIsActive(true);
//
//                DepartmentPhase savedPhase = departmentPhaseRepository.save(departmentPhase);
//                createdPhases.add(departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase));
//            }
//        }
//
//        return createdPhases;
//    }
//
//    // 9. Get phases by department and round
//    public List<DepartmentPhaseResponse> getPhasesByDepartmentAndRound(String departmentId, String roundId) {
//        // Get all innovation phases for this round
//        List<InnovationPhase> innovationPhases = innovationPhaseRepository
//                .findByInnovationRoundIdOrderByPhaseOrder(roundId);
//
//        List<DepartmentPhaseResponse> departmentPhases = new ArrayList<>();
//
//        for (InnovationPhase innovationPhase : innovationPhases) {
//            List<DepartmentPhase> phases = departmentPhaseRepository
//                    .findByDepartmentIdAndInnovationPhaseIdOrderByPhaseOrder(departmentId, innovationPhase.getId());
//
//            departmentPhases.addAll(phases.stream()
//                    .map(departmentPhaseMapper::toDepartmentPhaseResponse)
//                    .collect(Collectors.toList()));
//        }
//
//        return departmentPhases;
//    }
//
//    // 10. Get current active phase of department in round
//    public DepartmentPhaseResponse getCurrentActivePhase(String departmentId, String roundId) {
//        // Get all innovation phases for this round
//        List<InnovationPhase> innovationPhases = innovationPhaseRepository
//                .findByInnovationRoundIdOrderByPhaseOrder(roundId);
//
//        for (InnovationPhase innovationPhase : innovationPhases) {
//            DepartmentPhase currentPhase = departmentPhaseRepository
//                    .findCurrentActivePhase(departmentId, innovationPhase.getId(), LocalDate.now())
//                    .orElse(null);
//
//            if (currentPhase != null) {
//                return departmentPhaseMapper.toDepartmentPhaseResponse(currentPhase);
//            }
//        }
//
//        return null;
//    }

    // 11. Create single required phase for department
    public DepartmentPhaseResponse createRequiredPhaseForDepartment(String departmentId, String roundId,
            InnovationPhaseTypeEnum phaseType, LocalDate startDate, LocalDate endDate, String description) {

        // Validate phase type
        if (!isRequiredPhaseType(phaseType)) {
            throw new IdInvalidException(
                    "Chỉ được tạo giai đoạn SUBMISSION, DEPARTMENT_EVALUATION, hoặc DOCUMENT_SUBMISSION");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

        innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy round với ID: " + roundId));

        // Find corresponding innovation phase
        InnovationPhase innovationPhase = innovationPhaseRepository
                .findByInnovationRoundIdAndPhaseType(roundId, phaseType)
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy giai đoạn " + phaseType + " trong round này"));

        // Check if department already has this phase for this round
        if (departmentPhaseRepository.existsByDepartmentIdAndInnovationPhaseId(departmentId, innovationPhase.getId())) {
            throw new IdInvalidException("Khoa đã có giai đoạn " + phaseType + " cho round này");
        }

        // Validate time constraints
        validateDepartmentPhaseTimeConstraints(startDate, endDate, innovationPhase);

        DepartmentPhase departmentPhase = new DepartmentPhase();
        departmentPhase.setPhaseType(phaseType);
        departmentPhase.setStartDate(startDate);
        departmentPhase.setEndDate(endDate);
        departmentPhase.setDescription(description != null ? description
                : innovationPhase.getDescription() + " - " + department.getDepartmentName());
        departmentPhase.setDepartment(department);
        departmentPhase.setInnovationPhase(innovationPhase);
        departmentPhase.setIsActive(true);

        DepartmentPhase savedPhase = departmentPhaseRepository.save(departmentPhase);
        return departmentPhaseMapper.toDepartmentPhaseResponse(savedPhase);
    }

    // Helper method to check if phase type is required
    private boolean isRequiredPhaseType(InnovationPhaseTypeEnum phaseType) {
        return phaseType == InnovationPhaseTypeEnum.SUBMISSION ||
                phaseType == InnovationPhaseTypeEnum.SCORING ||
                phaseType == InnovationPhaseTypeEnum.ANNOUNCEMENT;
    }

    // Validate department phase time constraints
    private void validateDepartmentPhaseTimeConstraints(LocalDate startDate, LocalDate endDate,
            InnovationPhase innovationPhase) {
        // Check if dates are within InnovationPhase timeframe
        if (!innovationPhase.isPhaseWithinPhaseTimeframe(startDate, endDate)) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationPhase: " +
                    innovationPhase.getPhaseStartDate() + " đến " + innovationPhase.getPhaseEndDate());
        }

        // Check if dates are within InnovationRound timeframe
        if (!innovationPhase.isPhaseWithinRoundTimeframe(startDate, endDate)) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                    innovationPhase.getInnovationRound().getRegistrationStartDate() + " đến "
                    + innovationPhase.getInnovationRound().getRegistrationEndDate());
        }

        // Check if start date is before end date
        if (startDate.isAfter(endDate)) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // Check if department phase end date is before innovation phase end date
        if (endDate.isAfter(innovationPhase.getPhaseEndDate())) {
            throw new IdInvalidException(
                    "Thời gian kết thúc giai đoạn khoa phải trước thời gian kết thúc giai đoạn toàn trường: "
                            + innovationPhase.getPhaseEndDate());
        }
    }
}
