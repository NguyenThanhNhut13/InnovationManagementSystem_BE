package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;

import java.time.LocalDate;
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

    public DepartmentPhaseService(DepartmentPhaseRepository departmentPhaseRepository,
            DepartmentRepository departmentRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            DepartmentPhaseMapper departmentPhaseMapper,
            UserService userService) {
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.departmentRepository = departmentRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.departmentPhaseMapper = departmentPhaseMapper;
        this.userService = userService;
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
        departmentPhase.setInnovationPhase(innovationPhase);
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
        departmentPhase.setPhaseOrder(innovationPhase.getPhaseOrder());
        departmentPhase.setDepartment(department);
        departmentPhase.setInnovationPhase(innovationPhase);
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
                    innovationPhase.getRoundStartDate() + " đến " + innovationPhase.getRoundEndDate());
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

    // Validate that current user is department head of the specified department
    public void validateDepartmentHeadAccess(String departmentId) {
        User currentUser = userService.getCurrentUser();

        // Kiểm tra user có role TRUONG_KHOA không
        boolean isDepartmentHead = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName()
                        .equals(UserRoleEnum.TRUONG_KHOA));

        if (!isDepartmentHead) {
            throw new IdInvalidException("Access denied: User phải có role TRUONG_KHOA");
        }

        // Kiểm tra user có thuộc department này không
        if (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(departmentId)) {
            throw new IdInvalidException("Access denied: User này không phải là trưởng khoa của phòng ban này");
        }
    }

    /**
     * Validate that current user is department head of the department that owns the
     * specified phase
     */
    public void validateDepartmentHeadAccessForPhase(String phaseId) {
        User currentUser = userService.getCurrentUser();

        // Kiểm tra user có role TRUONG_KHOA không
        boolean isDepartmentHead = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName()
                        .equals(UserRoleEnum.TRUONG_KHOA));

        if (!isDepartmentHead) {
            throw new IdInvalidException("Access denied: User must have TRUONG_KHOA role");
        }

        // Lấy departmentId từ phaseId thông qua service
        String departmentId = getDepartmentIdByPhaseId(phaseId);

        // Kiểm tra user có thuộc department này không
        if (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(departmentId)) {
            throw new IdInvalidException("Access denied: User is not the head of this department");
        }
    }
}
