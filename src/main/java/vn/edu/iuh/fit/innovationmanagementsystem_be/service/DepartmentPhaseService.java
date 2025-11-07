package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SimpleUpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;

@Service
@Transactional
public class DepartmentPhaseService {

        private final DepartmentPhaseRepository departmentPhaseRepository;
        private final InnovationPhaseRepository innovationPhaseRepository;
        private final InnovationRoundRepository innovationRoundRepository;
        private final UserService userService;
        private final DepartmentPhaseMapper departmentPhaseMapper;

        public DepartmentPhaseService(DepartmentPhaseRepository departmentPhaseRepository,
                        InnovationPhaseRepository innovationPhaseRepository,
                        InnovationRoundRepository innovationRoundRepository,
                        UserService userService,
                        DepartmentPhaseMapper departmentPhaseMapper) {
                this.departmentPhaseRepository = departmentPhaseRepository;
                this.innovationPhaseRepository = innovationPhaseRepository;
                this.innovationRoundRepository = innovationRoundRepository;
                this.userService = userService;
                this.departmentPhaseMapper = departmentPhaseMapper;
        }

        // 1. Tạo nhiều phase cho khoa cùng lúc
        public List<DepartmentPhaseResponse> createMultipleDepartmentPhases(List<DepartmentPhaseRequest> requests) {
                if (requests == null || requests.isEmpty()) {
                        throw new IdInvalidException("Danh sách phase không được để trống");
                }

                long distinctPhaseTypeCount = requests.stream()
                                .map(DepartmentPhaseRequest::getPhaseType)
                                .distinct()
                                .count();

                if (distinctPhaseTypeCount < requests.size()) {
                        throw new IdInvalidException(
                                        "Không thể tạo trùng lặp loại phase trong cùng một request. Vui lòng kiểm tra lại.");
                }

                InnovationRound innovationRound = innovationRoundRepository.findByStatusOrderByCreatedAtDesc(
                                InnovationRoundStatusEnum.OPEN)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không có đợt sáng kiến nào đang mở"));

                User currentUser = userService.getCurrentUser();
                Department department = currentUser.getDepartment();

                if (department == null) {
                        throw new IdInvalidException("Người dùng hiện tại không thuộc khoa nào");
                }

                return requests.stream()
                                .map(request -> createSingleDepartmentPhase(request, innovationRound, department))
                                .toList();
        }

        private DepartmentPhaseResponse createSingleDepartmentPhase(DepartmentPhaseRequest request,
                        InnovationRound innovationRound, Department department) {
                InnovationPhase innovationPhase = innovationPhaseRepository
                                .findByInnovationRoundIdAndPhaseType(innovationRound.getId(), request.getPhaseType())
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy giai đoạn trường với loại: " + request.getPhaseType()));

                if (!InnovationPhaseLevelEnum.SCHOOL.equals(innovationPhase.getLevel())) {
                        throw new IdInvalidException(
                                        "Chỉ có thể tạo giai đoạn cho khoa từ giai đoạn của trường (SCHOOL level)");
                }

                if (!innovationRound.isPhaseWithinRoundTimeframe(request.getPhaseStartDate(),
                                request.getPhaseEndDate())) {
                        throw new IdInvalidException(
                                        "Thời gian giai đoạn của khoa phải nằm trong thời gian đợt sáng kiến của trường: "
                                                        +
                                                        innovationRound.getRegistrationStartDate() + " đến " +
                                                        innovationRound.getRegistrationEndDate());
                }

                if (!innovationPhase.isPhaseWithinPhaseTimeframe(request.getPhaseStartDate(),
                                request.getPhaseEndDate())) {
                        throw new IdInvalidException(
                                        "Thời gian giai đoạn của khoa phải nằm trong thời gian giai đoạn của trường: " +
                                                        innovationPhase.getPhaseStartDate() + " đến " +
                                                        innovationPhase.getPhaseEndDate());
                }

                if (Boolean.TRUE.equals(innovationPhase.getIsDeadline())) {
                        if (request.getPhaseEndDate().isAfter(innovationPhase.getPhaseEndDate())) {
                                throw new IdInvalidException(
                                                "Thời gian kết thúc giai đoạn của khoa không được vượt quá thời gian kết thúc của giai đoạn trường ("
                                                                + innovationPhase.getPhaseEndDate() + ")");
                        }
                }

                if (departmentPhaseRepository
                                .findByDepartmentIdAndInnovationRoundIdAndPhaseType(department.getId(),
                                                innovationRound.getId(), request.getPhaseType())
                                .isPresent()) {
                        throw new IdInvalidException(
                                        "Khoa đã có giai đoạn với loại: " + request.getPhaseType()
                                                        + ". Không thể tạo trùng lặp.");
                }

                DepartmentPhase departmentPhase = departmentPhaseMapper.toDepartmentPhase(request);
                departmentPhase.setInnovationPhase(innovationPhase);
                departmentPhase.setDepartment(department);
                departmentPhase.setInnovationRound(innovationRound);

                departmentPhase = departmentPhaseRepository.save(departmentPhase);

                return departmentPhaseMapper.toDepartmentPhaseResponse(departmentPhase);
        }

        // 2. Cập nhật nhiều phase theo phaseType (không cần ID)
        public List<DepartmentPhaseResponse> updateMultipleDepartmentPhasesByType(
                        List<SimpleUpdateDepartmentPhaseRequest> requests) {
                if (requests == null || requests.isEmpty()) {
                        throw new IdInvalidException("Danh sách phase không được để trống");
                }

                long distinctPhaseTypeCount = requests.stream()
                                .map(SimpleUpdateDepartmentPhaseRequest::getPhaseType)
                                .distinct()
                                .count();

                if (distinctPhaseTypeCount < requests.size()) {
                        throw new IdInvalidException(
                                        "Không thể cập nhật trùng lặp loại phase trong cùng một request. Vui lòng kiểm tra lại.");
                }

                InnovationRound innovationRound = innovationRoundRepository
                                .findByStatusOrderByCreatedAtDesc(InnovationRoundStatusEnum.OPEN)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không có đợt sáng kiến nào đang mở"));

                User currentUser = userService.getCurrentUser();
                Department department = currentUser.getDepartment();

                if (department == null) {
                        throw new IdInvalidException("Người dùng hiện tại không thuộc khoa nào");
                }

                return requests.stream()
                                .map(request -> updateSingleDepartmentPhaseByType(request, innovationRound, department))
                                .toList();
        }

        private DepartmentPhaseResponse updateSingleDepartmentPhaseByType(SimpleUpdateDepartmentPhaseRequest request,
                        InnovationRound innovationRound, Department department) {
                DepartmentPhase departmentPhase = departmentPhaseRepository
                                .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                department.getId(),
                                                innovationRound.getId(),
                                                request.getPhaseType())
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy giai đoạn với loại: " + request.getPhaseType()));

                InnovationPhase innovationPhase = departmentPhase.getInnovationPhase();

                if (innovationPhase == null) {
                        throw new IdInvalidException("ID giai đoạn không hợp lệ");
                }

                InnovationPhaseTypeEnum newPhaseType = request.getPhaseType();

                if (!departmentPhase.getPhaseType().equals(newPhaseType)) {
                        InnovationPhase newInnovationPhase = innovationPhaseRepository
                                        .findByInnovationRoundIdAndPhaseType(innovationRound.getId(), newPhaseType)
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy giai đoạn trường với loại: " + newPhaseType));

                        if (!InnovationPhaseLevelEnum.SCHOOL.equals(newInnovationPhase.getLevel())) {
                                throw new IdInvalidException(
                                                "Chỉ có thể cập nhật giai đoạn cho khoa từ giai đoạn của trường (SCHOOL level)");
                        }

                        innovationPhase = newInnovationPhase;
                        departmentPhase.setInnovationPhase(newInnovationPhase);
                }

                departmentPhase.setName(request.getName());
                departmentPhase.setPhaseType(newPhaseType);
                departmentPhase.setPhaseOrder(request.getPhaseOrder());
                departmentPhase.setPhaseStartDate(request.getPhaseStartDate());
                departmentPhase.setPhaseEndDate(request.getPhaseEndDate());
                departmentPhase.setDescription(request.getDescription());

                if (!innovationRound.isPhaseWithinRoundTimeframe(request.getPhaseStartDate(),
                                request.getPhaseEndDate())) {
                        throw new IdInvalidException(
                                        "Thời gian giai đoạn của khoa phải nằm trong thời gian đợt sáng kiến của trường: "
                                                        + innovationRound.getRegistrationStartDate() + " đến "
                                                        + innovationRound.getRegistrationEndDate());
                }

                if (!innovationPhase.isPhaseWithinPhaseTimeframe(request.getPhaseStartDate(),
                                request.getPhaseEndDate())) {
                        throw new IdInvalidException(
                                        "Thời gian giai đoạn của khoa phải nằm trong thời gian giai đoạn của trường: "
                                                        + innovationPhase.getPhaseStartDate() + " đến "
                                                        + innovationPhase.getPhaseEndDate());
                }

                if (Boolean.TRUE.equals(innovationPhase.getIsDeadline())) {
                        if (request.getPhaseEndDate().isAfter(innovationPhase.getPhaseEndDate())) {
                                throw new IdInvalidException(
                                                "Thời gian kết thúc giai đoạn của khoa không được vượt quá thời gian kết thúc của giai đoạn trường ("
                                                                + innovationPhase.getPhaseEndDate() + ")");
                        }
                }

                departmentPhase = departmentPhaseRepository.save(departmentPhase);

                return departmentPhaseMapper.toDepartmentPhaseResponse(departmentPhase);
        }

        // 3. Lấy danh sách tất cả giai đoạn khoa với pagination và filtering
        public ResultPaginationDTO getAllDepartmentPhasesWithPaginationAndFilter(
                        Specification<DepartmentPhase> specification, Pageable pageable) {
                Page<DepartmentPhase> departmentPhases = departmentPhaseRepository.findAll(specification, pageable);
                return Utils.toResultPaginationDTO(
                                departmentPhases.map(departmentPhaseMapper::toDepartmentPhaseResponse), pageable);
        }

        // 4. Xóa phase (chỉ được xóa khi status là DRAFT)
        public void deleteDepartmentPhase(String id) {
                DepartmentPhase departmentPhase = departmentPhaseRepository.findById(id)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy giai đoạn khoa với ID: " + id));

                if (!PhaseStatusEnum.DRAFT.equals(departmentPhase.getPhaseStatus())) {
                        throw new IdInvalidException(
                                        "Chỉ có thể xóa giai đoạn khi trạng thái là DRAFT. Trạng thái hiện tại: "
                                                        + departmentPhase.getPhaseStatus().getDisplayName());
                }

                departmentPhaseRepository.delete(departmentPhase);
        }
}
