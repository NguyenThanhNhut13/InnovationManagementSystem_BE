package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SimpleUpdateDepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.DepartmentPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
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

                // Check deadline constraint: Phải check với phase có isDeadline = true
                // (không phụ thuộc vào phaseType của request)
                InnovationPhase phaseWithDeadline = innovationPhaseRepository
                                .findPhaseWithDeadlineByRoundId(innovationRound.getId())
                                .orElse(null);

                if (phaseWithDeadline != null) {
                        // Check thời gian phase khoa phải nằm trong thời gian phase có deadline
                        if (!phaseWithDeadline.isPhaseWithinPhaseTimeframe(request.getPhaseStartDate(),
                                        request.getPhaseEndDate())) {
                                throw new IdInvalidException(
                                                "Thời gian giai đoạn của khoa phải nằm trong thời gian giai đoạn deadline: "
                                                                + phaseWithDeadline.getPhaseStartDate() + " đến " +
                                                                phaseWithDeadline.getPhaseEndDate());
                        }
                }

                // if (departmentPhaseRepository
                // .findByDepartmentIdAndInnovationRoundIdAndPhaseType(department.getId(),
                // innovationRound.getId(), request.getPhaseType())
                // .isPresent()) {
                // throw new IdInvalidException(
                // "Khoa đã có giai đoạn với loại: " + request.getPhaseType()
                // + ". Không thể tạo trùng lặp.");
                // }

                DepartmentPhase departmentPhase = departmentPhaseMapper.toDepartmentPhase(request);
                departmentPhase.setInnovationPhase(innovationPhase);
                departmentPhase.setDepartment(department);
                departmentPhase.setInnovationRound(innovationRound);
                if (request.getStatus() != null) {
                        departmentPhase.setStatus(request.getStatus());
                }

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

                // Check deadline constraint: Phải check với phase có isDeadline = true
                // (không phụ thuộc vào phaseType của request)
                InnovationPhase phaseWithDeadline = innovationPhaseRepository
                                .findPhaseWithDeadlineByRoundId(innovationRound.getId())
                                .orElse(null);

                if (phaseWithDeadline != null) {
                        // Check thời gian phase khoa phải nằm trong thời gian phase có deadline
                        if (!phaseWithDeadline.isPhaseWithinPhaseTimeframe(request.getPhaseStartDate(),
                                        request.getPhaseEndDate())) {
                                throw new IdInvalidException(
                                                "Thời gian giai đoạn của khoa phải nằm trong thời gian giai đoạn deadline: "
                                                                + phaseWithDeadline.getPhaseStartDate() + " đến " +
                                                                phaseWithDeadline.getPhaseEndDate());
                        }
                }

                departmentPhase = departmentPhaseRepository.save(departmentPhase);

                return departmentPhaseMapper.toDepartmentPhaseResponse(departmentPhase);
        }

        // 3. Lấy department phase list với pagination và filtering (theo khoa của user)
        public ResultPaginationDTO getDepartmentPhasesListForTable(
                        Specification<InnovationRound> specification, Pageable pageable) {

                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                User currentUser = userService.getCurrentUser();
                Department department = currentUser.getDepartment();

                if (department == null) {
                        throw new IdInvalidException("Người dùng hiện tại không thuộc khoa nào");
                }

                Page<InnovationRound> roundPage = innovationRoundRepository.findAll(specification, pageable);
                Page<DepartmentPhaseListResponse> responsePage = roundPage
                                .map(round -> convertToListResponse(round, department));
                return Utils.toResultPaginationDTO(responsePage, pageable);
        }

        private DepartmentPhaseListResponse convertToListResponse(InnovationRound round, Department department) {
                DepartmentPhaseListResponse response = new DepartmentPhaseListResponse();
                response.setId(round.getId());
                response.setName(round.getName());
                response.setAcademicYear(round.getAcademicYear());
                response.setRegistrationStartDate(round.getRegistrationStartDate());
                response.setRegistrationEndDate(round.getRegistrationEndDate());

                // Đếm số DepartmentPhase của khoa trong round này
                List<DepartmentPhase> departmentPhases = departmentPhaseRepository
                                .findByDepartmentIdAndInnovationRoundId(department.getId(), round.getId());
                response.setPhaseCount(departmentPhases != null ? departmentPhases.size() : 0);

                // Lấy status từ DepartmentPhase đầu tiên (theo phaseOrder tăng dần)
                if (departmentPhases != null && !departmentPhases.isEmpty()) {
                        DepartmentPhase firstPhase = departmentPhases.stream()
                                        .sorted((p1, p2) -> Integer.compare(p1.getPhaseOrder(), p2.getPhaseOrder()))
                                        .findFirst()
                                        .orElse(null);
                        if (firstPhase != null) {
                                response.setStatus(firstPhase.getStatus());
                        }
                }

                // Tính daysRemaining: số ngày còn lại từ hôm nay đến ngày kết thúc đăng ký
                // Dương: còn X ngày, 0: hết hạn hôm nay, Âm: đã quá hạn X ngày
                LocalDate today = LocalDate.now();
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, round.getRegistrationEndDate());
                response.setDaysRemaining((int) daysBetween);

                return response;
        }

        // 4. Lấy tất cả department phase trong một round bằng roundId
        public List<DepartmentPhaseResponse> getDepartmentPhasesByRoundId(String roundId) {
                if (roundId == null || roundId.trim().isEmpty()) {
                        throw new IdInvalidException("Round ID không được để trống");
                }

                innovationRoundRepository.findById(roundId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy đợt sáng kiến với ID: " + roundId));

                List<DepartmentPhase> departmentPhases = departmentPhaseRepository
                                .findByInnovationRoundId(roundId);

                return departmentPhases.stream()
                                .map(departmentPhaseMapper::toDepartmentPhaseResponse)
                                .toList();
        }

        // 4. Xóa phase (chỉ được xóa khi status là DRAFT)
        public void deleteDepartmentPhase(String id) {
                DepartmentPhase departmentPhase = departmentPhaseRepository.findById(id)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy giai đoạn khoa với ID: " + id));

                if (!InnovationRoundStatusEnum.DRAFT.equals(departmentPhase.getStatus())) {
                        throw new IdInvalidException(
                                        "Chỉ có thể xóa giai đoạn khi trạng thái là DRAFT. Trạng thái hiện tại: "
                                                        + departmentPhase.getStatus().getValue());
                }

                departmentPhaseRepository.delete(departmentPhase);
        }

        // 5. Công bố tất cả DepartmentPhase của InnovationRound - chuyển status từ
        // DRAFT sang OPEN
        @Transactional
        public List<DepartmentPhaseResponse> publishDepartmentPhase(String innovationRoundId) {
                if (innovationRoundId == null || innovationRoundId.trim().isEmpty()) {
                        throw new IdInvalidException("InnovationRound ID không được để trống");
                }

                innovationRoundRepository.findById(innovationRoundId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy đợt sáng kiến với ID: " + innovationRoundId));

                List<DepartmentPhase> departmentPhases = departmentPhaseRepository
                                .findByInnovationRoundId(innovationRoundId);

                if (departmentPhases.isEmpty()) {
                        throw new IdInvalidException(
                                        "Không tìm thấy giai đoạn khoa nào thuộc đợt sáng kiến này");
                }

                // Kiểm tra tất cả departmentPhase đều có status DRAFT
                for (DepartmentPhase phase : departmentPhases) {
                        if (!InnovationRoundStatusEnum.DRAFT.equals(phase.getStatus())) {
                                throw new IdInvalidException(
                                                "Chỉ có thể công bố khi tất cả giai đoạn khoa có trạng thái DRAFT. "
                                                                + "Giai đoạn '" + phase.getName() + "' có trạng thái: "
                                                                + phase.getStatus().getValue());
                        }
                }

                // Cập nhật status cho tất cả departmentPhase
                for (DepartmentPhase phase : departmentPhases) {
                        phase.setStatus(InnovationRoundStatusEnum.OPEN);
                }

                List<DepartmentPhase> savedPhases = departmentPhaseRepository.saveAll(departmentPhases);

                return savedPhases.stream()
                                .map(departmentPhaseMapper::toDepartmentPhaseResponse)
                                .toList();
        }

        // 6. Đóng tất cả DepartmentPhase của InnovationRound - chuyển status sang
        // CLOSED
        @Transactional
        public List<DepartmentPhaseResponse> closeDepartmentPhase(String innovationRoundId) {
                if (innovationRoundId == null || innovationRoundId.trim().isEmpty()) {
                        throw new IdInvalidException("InnovationRound ID không được để trống");
                }

                innovationRoundRepository.findById(innovationRoundId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy đợt sáng kiến với ID: " + innovationRoundId));

                List<DepartmentPhase> departmentPhases = departmentPhaseRepository
                                .findByInnovationRoundId(innovationRoundId);

                if (departmentPhases.isEmpty()) {
                        throw new IdInvalidException(
                                        "Không tìm thấy giai đoạn khoa nào thuộc đợt sáng kiến này");
                }

                LocalDate today = LocalDate.now();

                // Kiểm tra tất cả departmentPhase đều có status OPEN và đã qua ngày kết thúc
                for (DepartmentPhase phase : departmentPhases) {
                        if (!InnovationRoundStatusEnum.OPEN.equals(phase.getStatus())) {
                                throw new IdInvalidException(
                                                "Chỉ có thể đóng khi tất cả giai đoạn khoa có trạng thái OPEN. "
                                                                + "Giai đoạn '" + phase.getName() + "' có trạng thái: "
                                                                + phase.getStatus().getValue());
                        }

                }

                // Cập nhật status cho tất cả departmentPhase
                for (DepartmentPhase phase : departmentPhases) {
                        phase.setStatus(InnovationRoundStatusEnum.CLOSED);
                }

                List<DepartmentPhase> savedPhases = departmentPhaseRepository.saveAll(departmentPhases);

                return savedPhases.stream()
                                .map(departmentPhaseMapper::toDepartmentPhaseResponse)
                                .toList();
        }

}
