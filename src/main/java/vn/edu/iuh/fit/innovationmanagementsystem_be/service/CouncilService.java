package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilMemberRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateCouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateCouncilMembersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationWithScoreResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyAssignedInnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.CouncilMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewScoreRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringPeriodStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ScoringPeriodInfo;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ScoringProgressResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResultsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResultDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MemberEvaluationDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserRepository userRepository;
    private final InnovationRepository innovationRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CouncilMapper councilMapper;
    private final UserService userService;
    private final InnovationRoundRepository innovationRoundRepository;
    private final DepartmentRepository departmentRepository;
    private final ReviewScoreRepository reviewScoreRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final DepartmentPhaseRepository departmentPhaseRepository;

    public CouncilService(CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository,
            UserRepository userRepository,
            InnovationRepository innovationRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            CouncilMapper councilMapper,
            UserService userService,
            InnovationRoundRepository innovationRoundRepository,
            DepartmentRepository departmentRepository,
            ReviewScoreRepository reviewScoreRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            DepartmentPhaseRepository departmentPhaseRepository) {
        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.userRepository = userRepository;
        this.innovationRepository = innovationRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.councilMapper = councilMapper;
        this.userService = userService;
        this.innovationRoundRepository = innovationRoundRepository;
        this.departmentRepository = departmentRepository;
        this.reviewScoreRepository = reviewScoreRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.departmentPhaseRepository = departmentPhaseRepository;
    }

    // 1. Tạo Hội đồng mới
    @Transactional
    public CouncilResponse createCouncil(CreateCouncilRequest request) {
        // Xác định reviewCouncilLevel
        ReviewLevelEnum councilLevel;
        if (request.getReviewCouncilLevel() != null) {
            // Nếu FE truyền level, validate quyền
            councilLevel = request.getReviewCouncilLevel();
            validateCouncilLevelPermission(councilLevel);
        } else {
            // Nếu FE không truyền, tự động gắn dựa trên role
            councilLevel = determineCouncilLevelFromUserRole();
        }

        // Lấy round info
        InnovationRound round;
        if (request.getRoundId() != null && !request.getRoundId().isEmpty()) {
            // Nếu FE truyền roundId, dùng round đó
            round = innovationRoundRepository.findById(request.getRoundId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy đợt sáng kiến với ID: " + request.getRoundId()));
        } else {
            // Nếu không truyền, tự động lấy round hiện tại đang mở
            round = innovationRoundRepository.findCurrentActiveRound(
                    LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                    .orElseThrow(() -> new IdInvalidException(
                            "Không có đợt sáng kiến nào đang mở. Vui lòng truyền roundId hoặc đảm bảo có đợt sáng kiến đang mở"));
        }

        // Lấy department info
        Department department = null;
        if (request.getDepartmentId() != null && !request.getDepartmentId().isEmpty()) {
            // Nếu FE truyền departmentId, dùng department đó
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + request.getDepartmentId()));
        } else if (councilLevel == ReviewLevelEnum.KHOA) {
            // Nếu cấp Khoa nhưng không có departmentId, lấy từ current user
            User currentUser = userService.getCurrentUser();
            if (currentUser.getDepartment() == null) {
                throw new IdInvalidException(
                        "Người dùng hiện tại chưa được gán vào khoa nào. Không thể tạo hội đồng cấp Khoa");
            }
            department = currentUser.getDepartment();
        }

        // Validate không được tạo trùng hội đồng cho cùng round và department
        validateNoDuplicateCouncil(round.getId(), councilLevel, department);

        // Tự động generate tên hội đồng
        String councilName = generateCouncilName(round, councilLevel, department);

        // Validate tên Hội đồng không trùng
        validateCouncilName(councilName);

        // Validate danh sách thành viên
        validateMembers(request.getMembers());

        // Tạo Council entity
        Council council = new Council();
        council.setName(councilName);
        council.setReviewCouncilLevel(councilLevel);
        council.setStatus(CouncilStatusEnum.CON_HIEU_LUC); // Mặc định còn hiệu lực
        council.setDepartment(department); // Set department (null nếu cấp trường)
        council.setInnovationRound(round); // Set innovation round

        // Lưu Council trước để có ID
        council = councilRepository.save(council);

        // Tạo danh sách CouncilMember và gắn role TV_HOI_DONG
        List<CouncilMember> councilMembers = createCouncilMembersAndAssignRoles(council, request.getMembers(),
                councilLevel);
        council.setCouncilMembers(councilMembers);

        // Tự động lấy và gán eligible innovations từ roundId
        List<Innovation> eligibleInnovations = getEligibleInnovationsForCouncil(round.getId(), councilLevel,
                department);
        if (!eligibleInnovations.isEmpty()) {
            council.setInnovations(eligibleInnovations);
        }

        // Lưu lại Council với đầy đủ thông tin
        Council savedCouncil = councilRepository.save(council);

        // Trả về response
        // Map sang response
        CouncilResponse response = councilMapper.toCouncilResponse(savedCouncil);
        
        // Tính toán và set scoring progress (mới tạo nên sẽ là 0)
        ScoringProgressResponse scoringProgress = calculateScoringProgress(savedCouncil);
        response.setScoringProgress(scoringProgress);
        
        return response;
    }

    // Helper method: Tự động xác định cấp độ Hội đồng dựa trên role của user
    private ReviewLevelEnum determineCouncilLevelFromUserRole() {
        User currentUser = userService.getCurrentUser();

        // Lấy danh sách roles của user
        Set<UserRoleEnum> userRoles = currentUser.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toSet());

        // TRUONG_KHOA và QUAN_TRI_VIEN_KHOA → KHOA
        if (userRoles.contains(UserRoleEnum.TRUONG_KHOA) || userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_KHOA)) {
            return ReviewLevelEnum.KHOA;
        }

        // TV_HOI_DONG_KHOA → KHOA
        if (userRoles.contains(UserRoleEnum.TV_HOI_DONG_KHOA)) {
            return ReviewLevelEnum.KHOA;
        }

        // QUAN_TRI_VIEN_HE_THONG và QUAN_TRI_VIEN_QLKH_HTQT → TRUONG
        if (userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_HE_THONG)
                || userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT)) {
            return ReviewLevelEnum.TRUONG;
        }

        // TV_HOI_DONG_TRUONG → TRUONG
        if (userRoles.contains(UserRoleEnum.TV_HOI_DONG_TRUONG)) {
            return ReviewLevelEnum.TRUONG;
        }

        // Nếu không có role phù hợp, throw exception
        throw new IllegalArgumentException(
                "Không xác định được cấp độ hội đồng. Chỉ TRUONG_KHOA, QUAN_TRI_VIEN_KHOA, QUAN_TRI_VIEN_HE_THONG, QUAN_TRI_VIEN_QLKH_HTQT, TV_HOI_DONG_KHOA, TV_HOI_DONG_TRUONG mới có quyền truy cập hội đồng");
    }

    // Helper method: Validate quyền tạo Hội đồng theo cấp độ
    private void validateCouncilLevelPermission(ReviewLevelEnum councilLevel) {
        User currentUser = userService.getCurrentUser();

        // Lấy danh sách roles của user
        Set<UserRoleEnum> userRoles = currentUser.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toSet());

        // TRUONG_KHOA và QUAN_TRI_VIEN_KHOA chỉ được tạo Hội đồng cấp KHOA
        if (userRoles.contains(UserRoleEnum.TRUONG_KHOA) || userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_KHOA)) {
            if (councilLevel != ReviewLevelEnum.KHOA) {
                throw new IllegalArgumentException(
                        "Bạn chỉ có quyền tạo Hội đồng cấp Khoa. Vui lòng chọn cấp độ 'KHOA'");
            }
        }

        // QUAN_TRI_VIEN_HE_THONG và QUAN_TRI_VIEN_QLKH_HTQT chỉ được tạo Hội đồng cấp TRUONG
        if (userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_HE_THONG)
                || userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT)) {
            if (councilLevel != ReviewLevelEnum.TRUONG) {
                throw new IllegalArgumentException(
                        "Bạn chỉ có quyền tạo Hội đồng cấp Trường. Vui lòng chọn cấp độ 'TRUONG'");
            }
        }
    }

    // Helper method: Validate không được tạo trùng hội đồng cho cùng round và department
    private void validateNoDuplicateCouncil(String roundId, ReviewLevelEnum councilLevel, Department department) {
        Optional<Council> existingCouncil;
        
        if (councilLevel == ReviewLevelEnum.KHOA && department != null) {
            // Faculty level: check theo round + level + department
            existingCouncil = councilRepository.findByRoundIdAndLevelAndDepartmentId(
                    roundId, councilLevel, department.getId());
            
            if (existingCouncil.isPresent()) {
                throw new IllegalArgumentException(
                        String.format("Đã tồn tại hội đồng cấp Đơn vị cho đợt này. Mỗi đợt chỉ được có 1 hội đồng cấp Đơn vị cho khoa '%s'.",
                                department.getDepartmentName()));
            }
        } else if (councilLevel == ReviewLevelEnum.TRUONG) {
            // School level: check theo round + level (không có department)
            existingCouncil = councilRepository.findByRoundIdAndLevelAndNoDepartment(roundId, councilLevel);
            
            if (existingCouncil.isPresent()) {
                throw new IllegalArgumentException(
                        "Đã tồn tại hội đồng cấp Trường cho đợt này. Mỗi đợt chỉ được có 1 hội đồng cấp Trường.");
            }
        }
    }

    // Helper method: Validate tên Hội đồng không trùng
    private void validateCouncilName(String name) {
        councilRepository.findByNameIgnoreCase(name).ifPresent(existingCouncil -> {
            throw new IllegalArgumentException("Tên Hội đồng '" + name + "' đã tồn tại trong hệ thống");
        });
    }

    // Helper method: Validate danh sách thành viên
    private void validateMembers(List<CouncilMemberRequest> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Danh sách thành viên không được để trống");
        }

        if (members.size() < 3) {
            throw new IllegalArgumentException("Hội đồng phải có ít nhất 3 thành viên");
        }

        // Validate số lượng thành viên phải là số lẻ
        if (members.size() % 2 == 0) {
            throw new IllegalArgumentException(
                    "Hội đồng phải có số lượng thành viên là số lẻ (3, 5, 7, ...) để đảm bảo quyết định rõ ràng. " +
                    "Hiện tại có " + members.size() + " thành viên.");
        }

        // Đếm số lượng Chủ tịch và Thư ký
        long chairmanCount = members.stream()
                .filter(m -> m.getRole() == CouncilMemberRoleEnum.CHU_TICH)
                .count();

        long secretaryCount = members.stream()
                .filter(m -> m.getRole() == CouncilMemberRoleEnum.THU_KY)
                .count();

        if (chairmanCount != 1) {
            throw new IllegalArgumentException("Hội đồng phải có đúng 1 Chủ tịch");
        }

        if (secretaryCount != 1) {
            throw new IllegalArgumentException("Hội đồng phải có đúng 1 Thư ký");
        }

        // Kiểm tra không trùng userId
        Set<String> userIds = new HashSet<>();
        for (CouncilMemberRequest member : members) {
            if (!userIds.add(member.getUserId())) {
                throw new IllegalArgumentException("Không được trùng lặp thành viên trong Hội đồng");
            }
        }

        // Kiểm tra tất cả userId phải tồn tại
        for (String userId : userIds) {
            if (!userRepository.existsById(userId)) {
                throw new IdInvalidException("Không tìm thấy user với ID: " + userId);
            }
        }
    }

    // Helper method: Tạo CouncilMember và gắn role TV_HOI_DONG
    private List<CouncilMember> createCouncilMembersAndAssignRoles(Council council,
            List<CouncilMemberRequest> memberRequests,
            ReviewLevelEnum councilLevel) {
        List<CouncilMember> councilMembers = new ArrayList<>();

        // Xác định role cần gắn dựa trên cấp độ Hội đồng
        UserRoleEnum councilRoleToAssign = (councilLevel == ReviewLevelEnum.TRUONG)
                ? UserRoleEnum.TV_HOI_DONG_TRUONG
                : UserRoleEnum.TV_HOI_DONG_KHOA;

        for (CouncilMemberRequest memberRequest : memberRequests) {
            User user = userRepository.findById(memberRequest.getUserId())
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy user với ID: " + memberRequest.getUserId()));

            CouncilMember councilMember = new CouncilMember();
            councilMember.setCouncil(council);
            councilMember.setUser(user);
            councilMember.setRole(memberRequest.getRole());

            councilMembers.add(councilMemberRepository.save(councilMember));

            // Gắn role TV_HOI_DONG cho user nếu chưa có
            assignCouncilRoleToUser(user, councilRoleToAssign);
        }

        return councilMembers;
    }

    // Helper method: Gắn role TV_HOI_DONG cho user
    private void assignCouncilRoleToUser(User user, UserRoleEnum roleEnum) {
        // Kiểm tra user đã có role này chưa
        boolean hasRole = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == roleEnum);

        if (!hasRole) {
            // Tìm Role entity
            Role role = roleRepository.findByRoleName(roleEnum)
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy role: " + roleEnum));

            // Tạo UserRole mới
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);

            // Lưu UserRole
            userRoleRepository.save(userRole);

            // Thêm vào user's roles (để tránh lazy loading issue)
            user.getUserRoles().add(userRole);

            System.out.println("Đã gắn role " + roleEnum + " cho user: " + user.getFullName());
        }
    }

    // Helper method: Tự động generate tên hội đồng
    private String generateCouncilName(InnovationRound round, ReviewLevelEnum councilLevel, Department department) {
        if (councilLevel == ReviewLevelEnum.TRUONG) {
            // Cấp Trường: "Hội đồng sáng kiến cấp Trường"
            return "Hội đồng sáng kiến cấp Trường";
        } else {
            // Cấp Khoa: "Hội đồng sáng kiến cấp Đơn vị - {departmentName}"
            if (department != null) {
                return "Hội đồng sáng kiến cấp Đơn vị - " + department.getDepartmentName();
            } else {
                return "Hội đồng sáng kiến cấp Đơn vị";
            }
        }
    }

    // Helper method: Tự động lấy eligible innovations từ roundId
    private List<Innovation> getEligibleInnovationsForCouncil(String roundId, ReviewLevelEnum councilLevel,
            Department department) {
        // Lấy tất cả innovations SUBMITTED từ round
        List<Innovation> allSubmitted = innovationRepository.findByRoundIdAndStatus(roundId,
                InnovationStatusEnum.SUBMITTED);

        if (councilLevel == ReviewLevelEnum.KHOA) {
            // Faculty level: chỉ lấy innovations của khoa
            if (department == null) {
                return new ArrayList<>();
            }
            String departmentId = department.getId();
            return allSubmitted.stream()
                    .filter(i -> i.getDepartment() != null && i.getDepartment().getId().equals(departmentId))
                    .collect(Collectors.toList());
        } else {
            // School level: lấy tất cả
            return allSubmitted;
        }
    }

    // 2. Lấy thông tin hội đồng hiện tại (dựa trên round hiện tại và department của user)
    public CouncilResponse getCurrentCouncil() {
        // Lấy round hiện tại đang mở
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElseThrow(() -> new IdInvalidException(
                        "Không có đợt sáng kiến nào đang mở. Vui lòng đảm bảo có đợt sáng kiến đang mở"));

        // Xác định cấp độ hội đồng từ role của user
        ReviewLevelEnum councilLevel = determineCouncilLevelFromUserRole();

        // Lấy department (nếu faculty level)
        Department department = null;
        if (councilLevel == ReviewLevelEnum.KHOA) {
            User currentUser = userService.getCurrentUser();
            if (currentUser.getDepartment() == null) {
                throw new IdInvalidException(
                        "Người dùng hiện tại chưa được gán vào khoa nào. Không thể lấy hội đồng cấp Khoa");
            }
            department = currentUser.getDepartment();
        }

        // Tìm council
        Optional<Council> council;
        if (councilLevel == ReviewLevelEnum.KHOA && department != null) {
            // Faculty level: tìm theo round + level + department
            council = councilRepository.findByRoundIdAndLevelAndDepartmentId(
                    currentRound.getId(), councilLevel, department.getId());
        } else if (councilLevel == ReviewLevelEnum.TRUONG) {
            // School level: tìm theo round + level (không có department)
            council = councilRepository.findByRoundIdAndLevelAndNoDepartment(
                    currentRound.getId(), councilLevel);
        } else {
            throw new IdInvalidException("Không xác định được cấp độ hội đồng");
        }

        // Nếu không tìm thấy, throw exception
        Council foundCouncil = council.orElseThrow(() -> new IdInvalidException(
                "Chưa có hội đồng nào được thành lập cho đợt sáng kiến hiện tại. Vui lòng thành lập hội đồng trước."));

        // Trigger lazy load cho innovations trong cùng transaction để tránh LazyInitializationException
        foundCouncil.getInnovations().size();

        // Map sang response
        CouncilResponse response = councilMapper.toCouncilResponse(foundCouncil);
        
        // Tính toán và set scoring progress
        ScoringProgressResponse scoringProgress = calculateScoringProgress(foundCouncil);
        response.setScoringProgress(scoringProgress);
        
        // Tính toán và set scoring period info (chung cho tất cả innovations trong council)
        ScoringPeriodInfo scoringPeriodInfo = calculateScoringPeriodInfoForCouncil(foundCouncil, currentRound);
        response.setScoringStartDate(scoringPeriodInfo.getStartDate());
        response.setScoringEndDate(scoringPeriodInfo.getEndDate());
        response.setCanScore(scoringPeriodInfo.isCanScore());
        response.setCanView(scoringPeriodInfo.isCanView());
        response.setScoringPeriodStatus(scoringPeriodInfo.getStatus());
        
        return response;
    }

    // 3. Lấy thông tin chi tiết hội đồng theo ID
    @Transactional(readOnly = true)
    public CouncilResponse getCouncilById(String councilId) {
        // Tìm và validate council
        Council council = findAndValidateCouncil(councilId);

        // Trigger lazy load cho các collections trong cùng transaction
        council.getCouncilMembers().size(); // Đã được fetch trong query nếu dùng findByRoundIdAndLevel...
        council.getInnovations().size(); // Trigger lazy load

        // Map sang response
        CouncilResponse response = councilMapper.toCouncilResponse(council);

        // Tính toán và set scoring progress
        ScoringProgressResponse scoringProgress = calculateScoringProgress(council);
        response.setScoringProgress(scoringProgress);

        // Tính toán và set scoring period info (chung cho tất cả innovations trong council)
        InnovationRound round = council.getInnovationRound();
        if (round != null) {
            ScoringPeriodInfo scoringPeriodInfo = calculateScoringPeriodInfoForCouncil(council, round);
            response.setScoringStartDate(scoringPeriodInfo.getStartDate());
            response.setScoringEndDate(scoringPeriodInfo.getEndDate());
            response.setCanScore(scoringPeriodInfo.isCanScore());
            response.setCanView(scoringPeriodInfo.isCanView());
            response.setScoringPeriodStatus(scoringPeriodInfo.getStatus());
        }

        return response;
    }

    // Helper method: Tìm và validate council
    private Council findAndValidateCouncil(String councilId) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hội đồng với ID: " + councilId));
        validateCouncilAccessPermission(council);
        return council;
    }

    // Helper method: Validate quyền truy cập council
    private void validateCouncilAccessPermission(Council council) {
        User currentUser = userService.getCurrentUser();
        ReviewLevelEnum councilLevel = determineCouncilLevelFromUserRole();

        // Faculty level: chỉ được xem council của cùng department
        if (councilLevel == ReviewLevelEnum.KHOA) {
            if (currentUser.getDepartment() == null ||
                    council.getDepartment() == null ||
                    !council.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new IllegalArgumentException("Bạn không có quyền truy cập hội đồng này");
            }
        } else {
            // School level: chỉ được xem council cấp trường (không có department)
            if (council.getDepartment() != null) {
                throw new IllegalArgumentException("Bạn không có quyền truy cập hội đồng này");
            }
        }
    }

    // Helper method: Lấy danh sách member user IDs có quyền chấm điểm (THANH_VIEN, CHU_TICH, THU_KY)
    private List<String> getScoringMemberUserIds(Council council) {
        return council.getCouncilMembers().stream()
                .filter(member -> member.getRole() == CouncilMemberRoleEnum.THANH_VIEN 
                        || member.getRole() == CouncilMemberRoleEnum.CHU_TICH
                        || member.getRole() == CouncilMemberRoleEnum.THU_KY)
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());
    }

    // Inner class để chứa kết quả scoring của một innovation
    private static class InnovationScoringResult {
        final int scoredReviewers;
        final double totalScore;
        final Double averageScore;
        final boolean isCompleted;

        InnovationScoringResult(int scoredReviewers, double totalScore, Double averageScore, boolean isCompleted) {
            this.scoredReviewers = scoredReviewers;
            this.totalScore = totalScore;
            this.averageScore = averageScore;
            this.isCompleted = isCompleted;
        }
    }

    // Helper method: Tính scoring cho một innovation
    private InnovationScoringResult calculateInnovationScoring(Innovation innovation, List<String> memberUserIds) {
        int scoredReviewers = 0;
        double totalScore = 0.0;

        for (String memberUserId : memberUserIds) {
            Optional<ReviewScore> reviewScore = reviewScoreRepository
                    .findByInnovationIdAndReviewerId(innovation.getId(), memberUserId);

            if (reviewScore.isPresent() && reviewScore.get().getTotalScore() != null) {
                scoredReviewers++;
                totalScore += reviewScore.get().getTotalScore();
            }
        }

        Double averageScore = scoredReviewers > 0 ? totalScore / scoredReviewers : null;
        boolean isCompleted = scoredReviewers == memberUserIds.size() && memberUserIds.size() > 0;

        return new InnovationScoringResult(scoredReviewers, totalScore, averageScore, isCompleted);
    }

    // 4. Lấy danh sách sáng kiến của hội đồng với pagination và scoring progress
    @Transactional(readOnly = true)
    public ResultPaginationDTO getCouncilInnovations(String councilId, Pageable pageable) {
        // Tìm và validate council
        Council council = findAndValidateCouncil(councilId);

        // Lấy danh sách member user IDs có quyền chấm điểm
        List<String> memberUserIds = getScoringMemberUserIds(council);
        int totalReviewers = memberUserIds.size();

        // Lấy innovations của council với pagination
        List<Innovation> allInnovations = council.getInnovations();
        
        // Tính toán pagination thủ công (vì đây là ManyToMany relationship)
        int totalElements = allInnovations.size();
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<Innovation> pagedInnovations = start < totalElements 
                ? allInnovations.subList(start, end)
                : new ArrayList<>();

        // Map sang InnovationWithScoreResponse
        List<InnovationWithScoreResponse> innovationResponses = pagedInnovations.stream()
                .map(innovation -> {
                    // Tính scoring cho innovation này
                    InnovationScoringResult scoringResult = calculateInnovationScoring(innovation, memberUserIds);

                    // Lấy thông tin author
                    String authorName = innovation.getUser() != null ? innovation.getUser().getFullName() : "N/A";
                    String departmentName = innovation.getDepartment() != null 
                            ? innovation.getDepartment().getDepartmentName() 
                            : null;

                    return new InnovationWithScoreResponse(
                            innovation.getId(),
                            innovation.getInnovationName(),
                            authorName,
                            departmentName,
                            innovation.getStatus(),
                            innovation.getIsScore(),
                            totalReviewers,
                            scoringResult.scoredReviewers,
                            scoringResult.averageScore,
                            scoringResult.isCompleted
                    );
                })
                .collect(Collectors.toList());

        // Tạo Page object
        Page<InnovationWithScoreResponse> responsePage = new PageImpl<>(
                innovationResponses,
                pageable,
                totalElements
        );

        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // 5. Lấy danh sách sáng kiến được phân công cho thành viên hội đồng hiện tại
    @Transactional(readOnly = true)
    public ResultPaginationDTO getMyAssignedInnovations(ScoringStatusEnum scoringStatus, Pageable pageable) {
        // 1. Lấy current user
        User currentUser = userService.getCurrentUser();
        String currentUserId = currentUser.getId();
        
        // 2. Lấy council hiện tại (tái sử dụng logic từ getCurrentCouncil)
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElseThrow(() -> new IdInvalidException(
                        "Không có đợt sáng kiến nào đang mở. Vui lòng đảm bảo có đợt sáng kiến đang mở"));
        
        ReviewLevelEnum councilLevel = determineCouncilLevelFromUserRole();
        Department department = null;
        if (councilLevel == ReviewLevelEnum.KHOA) {
            if (currentUser.getDepartment() == null) {
                throw new IdInvalidException(
                        "Người dùng hiện tại chưa được gán vào khoa nào. Không thể lấy hội đồng cấp Khoa");
            }
            department = currentUser.getDepartment();
        }
        
        Optional<Council> councilOpt;
        if (councilLevel == ReviewLevelEnum.KHOA && department != null) {
            councilOpt = councilRepository.findByRoundIdAndLevelAndDepartmentId(
                    currentRound.getId(), councilLevel, department.getId());
        } else if (councilLevel == ReviewLevelEnum.TRUONG) {
            councilOpt = councilRepository.findByRoundIdAndLevelAndNoDepartment(
                    currentRound.getId(), councilLevel);
        } else {
            throw new IdInvalidException("Không xác định được cấp độ hội đồng");
        }
        
        Council currentCouncil = councilOpt.orElseThrow(() -> new IdInvalidException(
                "Chưa có hội đồng nào được thành lập cho đợt sáng kiến hiện tại. Vui lòng thành lập hội đồng trước."));
        
        // Trigger lazy load cho innovations và members
        currentCouncil.getInnovations().size();
        currentCouncil.getCouncilMembers().size();
        
        // 3. Validate user là thành viên của council này
        boolean isMember = currentCouncil.getCouncilMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        
        if (!isMember) {
            throw new IdInvalidException("Bạn không phải là thành viên của hội đồng này");
        }
        
        // 4. Lấy innovations của council hiện tại với user và department đã được fetch (JOIN FETCH)
        // Sử dụng query riêng để tránh LazyInitializationException và N+1 queries
        List<Innovation> allInnovations = innovationRepository.findByCouncilIdWithUserAndDepartment(currentCouncil.getId());
        
        // 5. Map sang MyAssignedInnovationResponse
        List<MyAssignedInnovationResponse> responses = allInnovations.stream()
                .map(innovation -> {
                    // Check current user đã đánh giá chưa (để filter và sort)
                    boolean hasScored = reviewScoreRepository.existsByInnovationIdAndReviewerId(
                            innovation.getId(),
                            currentUserId
                    );
                    
                    // Lấy quyết định của current user nếu đã đánh giá
                    Boolean myIsApproved = null;
                    if (hasScored) {
                        Optional<ReviewScore> myScoreRecord = reviewScoreRepository
                                .findByInnovationIdAndReviewerId(innovation.getId(), currentUserId);
                        if (myScoreRecord.isPresent()) {
                            myIsApproved = myScoreRecord.get().getIsApproved();
                        }
                    }
                    
                    // Lấy thông tin author (user và department đã được fetch sẵn qua JOIN FETCH)
                    String authorName = innovation.getUser() != null ? innovation.getUser().getFullName() : "N/A";
                    String departmentName = innovation.getDepartment() != null
                            ? innovation.getDepartment().getDepartmentName()
                            : null;
                    
                    // Map sang response (scoring period info đã được lấy từ council response)
                    return new MyAssignedInnovationResponse(
                            innovation.getId(),
                            innovation.getInnovationName(),
                            authorName,
                            departmentName,
                            innovation.getStatus(),
                            innovation.getIsScore(),
                            myIsApproved
                    );
                })
                .collect(Collectors.toList());
        
        // 7. Filter theo scoringStatus (nếu có) - dùng myIsApproved để check
        if (scoringStatus != null && scoringStatus != ScoringStatusEnum.ALL) {
            if (scoringStatus == ScoringStatusEnum.PENDING) {
                responses = responses.stream()
                        .filter(r -> r.getMyIsApproved() == null)
                        .collect(Collectors.toList());
            } else if (scoringStatus == ScoringStatusEnum.SCORED) {
                responses = responses.stream()
                        .filter(r -> r.getMyIsApproved() != null)
                        .collect(Collectors.toList());
            }
        }
        
        // 8. Sort: Chưa đánh giá trước, đã đánh giá sau
        responses.sort((a, b) -> {
            boolean aHasScored = a.getMyIsApproved() != null;
            boolean bHasScored = b.getMyIsApproved() != null;
            if (aHasScored != bHasScored) {
                return aHasScored ? 1 : -1;
            }
            // Nếu cùng trạng thái, sort theo tên
            return a.getInnovationName().compareTo(b.getInnovationName());
        });
        
        // 9. Pagination
        int totalElements = responses.size();
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<MyAssignedInnovationResponse> pagedResponses = start < totalElements
                ? responses.subList(start, end)
                : new ArrayList<>();
        
        Page<MyAssignedInnovationResponse> responsePage = new PageImpl<>(
                pagedResponses,
                pageable,
                totalElements
        );
        
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // 6. Lấy danh sách hội đồng với pagination và filtering
    public ResultPaginationDTO getAllCouncilsWithPaginationAndFilter(
            Specification<Council> specification, Pageable pageable) {
        
        // Default sort: createdAt DESC nếu không có sort
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("createdAt").descending());
        }

        // Xác định cấp độ hội đồng từ role của user để filter
        ReviewLevelEnum councilLevel = determineCouncilLevelFromUserRole();
        User currentUser = userService.getCurrentUser();
        
        // Tạo specification để filter theo role và department (nếu faculty level)
        Specification<Council> roleSpec = (root, query, criteriaBuilder) -> {
            if (councilLevel == ReviewLevelEnum.KHOA) {
                // Faculty level: chỉ lấy councils của khoa hiện tại
                if (currentUser.getDepartment() == null) {
                    // Nếu user không có department, trả về empty
                    return criteriaBuilder.disjunction();
                }
                return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("reviewCouncilLevel"), councilLevel),
                    criteriaBuilder.equal(root.get("department").get("id"), currentUser.getDepartment().getId())
                );
            } else {
                // School level: chỉ lấy councils cấp trường (không có department)
                return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("reviewCouncilLevel"), councilLevel),
                    criteriaBuilder.isNull(root.get("department"))
                );
            }
        };

        // Kết hợp với specification từ filter (nếu có)
        Specification<Council> combinedSpec = specification != null 
            ? roleSpec.and(specification) 
            : roleSpec;

        // Query với pagination
        Page<Council> councilPage = councilRepository.findAll(combinedSpec, pageable);
        
        // Map sang CouncilListResponse (tóm tắt, không có scoring progress)
        Page<CouncilListResponse> responsePage = councilPage.map(council -> 
            councilMapper.toCouncilListResponse(council)
        );

        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // Helper method: Tính toán tiến độ chấm điểm
    public ScoringProgressResponse calculateScoringProgress(Council council) {
        // Lấy tất cả innovations của council
        List<Innovation> innovations = council.getInnovations();
        if (innovations == null || innovations.isEmpty()) {
            return new ScoringProgressResponse(0, 0, 0, null, 0);
        }

        // Lấy danh sách member user IDs có quyền chấm điểm
        List<String> memberUserIds = getScoringMemberUserIds(council);

        if (memberUserIds.isEmpty()) {
            // Nếu không có thành viên nào, không thể chấm điểm
            return new ScoringProgressResponse(
                    innovations.size(),
                    0,
                    innovations.size(),
                    null,
                    0);
        }

        int totalInnovations = innovations.size();
        int scoredCount = 0;
        int pendingCount = 0;
        double totalScoreSum = 0.0;
        int totalScoreCount = 0;

        // Với mỗi innovation, kiểm tra xem đã có đủ điểm từ tất cả members chưa
        for (Innovation innovation : innovations) {
            // Tính scoring cho innovation này
            InnovationScoringResult scoringResult = calculateInnovationScoring(innovation, memberUserIds);

            // Một innovation được coi là "scored" nếu đã có đủ điểm từ tất cả members
            if (scoringResult.isCompleted) {
                scoredCount++;
                totalScoreSum += scoringResult.totalScore;
                totalScoreCount += scoringResult.scoredReviewers;
            } else {
                pendingCount++;
                // Vẫn tính điểm từ các members đã chấm
                if (scoringResult.scoredReviewers > 0) {
                    totalScoreSum += scoringResult.totalScore;
                    totalScoreCount += scoringResult.scoredReviewers;
                }
            }
        }

        // Tính điểm trung bình
        Double averageScore = (totalScoreCount > 0) ? totalScoreSum / totalScoreCount : null;

        // Tính % hoàn thành
        int completionPercentage = totalInnovations > 0
                ? Math.round((scoredCount * 100.0f) / totalInnovations)
                : 0;

        return new ScoringProgressResponse(
                totalInnovations,
                scoredCount,
                pendingCount,
                averageScore,
                completionPercentage);
    }

    // 6. Cập nhật thành viên hội đồng
    @Transactional
    public CouncilResponse updateCouncil(String councilId, UpdateCouncilMembersRequest request) {
        // 1. Tìm và validate council
        Council council = findAndValidateCouncil(councilId);

        // 2. Validate chưa có điểm chấm
        validateNoScoringStarted(council);

        // 3. Validate danh sách thành viên mới
        validateMembers(request.getMembers());

        // 4. Lấy danh sách thành viên hiện tại
        List<CouncilMember> currentMembers = council.getCouncilMembers();
        Set<String> currentUserIds = currentMembers.stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toSet());

        // 5. Lấy danh sách userId mới
        Set<String> newUserIds = request.getMembers().stream()
                .map(CouncilMemberRequest::getUserId)
                .collect(Collectors.toSet());

        // 6. Xác định thành viên cần xóa (có trong current nhưng không có trong new)
        List<String> userIdsToRemove = currentUserIds.stream()
                .filter(userId -> !newUserIds.contains(userId))
                .collect(Collectors.toList());

        // 7. Xác định role cần gỡ dựa trên cấp độ Hội đồng
        UserRoleEnum councilRoleToRevoke = (council.getReviewCouncilLevel() == ReviewLevelEnum.TRUONG)
                ? UserRoleEnum.TV_HOI_DONG_TRUONG
                : UserRoleEnum.TV_HOI_DONG_KHOA;

        // 8. Xóa thành viên cũ và gỡ role
        for (String userIdToRemove : userIdsToRemove) {
            // Xóa CouncilMember
            CouncilMember memberToRemove = currentMembers.stream()
                    .filter(m -> m.getUser().getId().equals(userIdToRemove))
                    .findFirst()
                    .orElse(null);

            if (memberToRemove != null) {
                councilMemberRepository.delete(memberToRemove);
                currentMembers.remove(memberToRemove);
            }

            // Gỡ role TV_HOI_DONG nếu user không còn là thành viên của hội đồng nào khác
            revokeCouncilRoleIfNotInOtherCouncils(userIdToRemove, councilRoleToRevoke, councilId);
        }

        // 9. Cập nhật/Thêm thành viên mới
        UserRoleEnum councilRoleToAssign = (council.getReviewCouncilLevel() == ReviewLevelEnum.TRUONG)
                ? UserRoleEnum.TV_HOI_DONG_TRUONG
                : UserRoleEnum.TV_HOI_DONG_KHOA;

        for (CouncilMemberRequest memberRequest : request.getMembers()) {
            User user = userRepository.findById(memberRequest.getUserId())
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy user với ID: " + memberRequest.getUserId()));

            // Tìm CouncilMember hiện tại (nếu có)
            Optional<CouncilMember> existingMember = currentMembers.stream()
                    .filter(m -> m.getUser().getId().equals(memberRequest.getUserId()))
                    .findFirst();

            if (existingMember.isPresent()) {
                // Cập nhật role của thành viên hiện có
                CouncilMember member = existingMember.get();
                member.setRole(memberRequest.getRole());
                councilMemberRepository.save(member);
            } else {
                // Tạo CouncilMember mới
                CouncilMember newMember = new CouncilMember();
                newMember.setCouncil(council);
                newMember.setUser(user);
                newMember.setRole(memberRequest.getRole());
                councilMemberRepository.save(newMember);
                currentMembers.add(newMember);
            }

            // Gắn role TV_HOI_DONG cho user nếu chưa có
            assignCouncilRoleToUser(user, councilRoleToAssign);
        }

        // 10. Refresh council để có danh sách thành viên mới nhất
        council = councilRepository.findById(councilId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hội đồng với ID: " + councilId));

        // 11. Tự động gán các sáng kiến mới được nộp (chưa được gán vào hội đồng)
        // Trigger lazy load để tránh LazyInitializationException
        List<Innovation> currentInnovations = council.getInnovations();
        currentInnovations.size(); // Trigger lazy load
        Set<String> currentInnovationIds = currentInnovations.stream()
                .map(Innovation::getId)
                .collect(Collectors.toSet());
        
        // Lấy các sáng kiến eligible mới
        List<Innovation> eligibleInnovations = getEligibleInnovationsForCouncil(
                council.getInnovationRound().getId(), 
                council.getReviewCouncilLevel(), 
                council.getDepartment()
        );
        
        // Chỉ thêm các sáng kiến chưa được gán
        List<Innovation> newInnovations = eligibleInnovations.stream()
                .filter(innovation -> !currentInnovationIds.contains(innovation.getId()))
                .collect(Collectors.toList());
        
        if (!newInnovations.isEmpty()) {
            currentInnovations.addAll(newInnovations);
            council.setInnovations(currentInnovations);
            council = councilRepository.save(council);
        }

        // 12. Map sang response
        CouncilResponse response = councilMapper.toCouncilResponse(council);
        ScoringProgressResponse scoringProgress = calculateScoringProgress(council);
        response.setScoringProgress(scoringProgress);

        return response;
    }

    // Helper method: Validate chưa có điểm chấm
    private void validateNoScoringStarted(Council council) {
        // Lấy danh sách innovation IDs của council
        List<String> innovationIds = council.getInnovations().stream()
                .map(Innovation::getId)
                .collect(Collectors.toList());

        if (innovationIds.isEmpty()) {
            // Không có sáng kiến nào, cho phép cập nhật
            return;
        }

        // Lấy danh sách member user IDs hiện tại
        List<String> memberUserIds = getScoringMemberUserIds(council);

        if (memberUserIds.isEmpty()) {
            // Không có thành viên nào, cho phép cập nhật
            return;
        }

        // Kiểm tra xem có ReviewScore nào từ các innovation và reviewer này không
        for (String innovationId : innovationIds) {
            for (String reviewerId : memberUserIds) {
                if (reviewScoreRepository.existsByInnovationIdAndReviewerId(innovationId, reviewerId)) {
                    throw new IllegalArgumentException(
                            "Không thể cập nhật thành viên hội đồng khi đã có điểm chấm. " +
                            "Hội đồng đã bắt đầu quá trình chấm điểm.");
                }
            }
        }
    }

    // Helper method: Gỡ role TV_HOI_DONG nếu user không còn là thành viên của hội đồng nào khác cùng cấp độ
    private void revokeCouncilRoleIfNotInOtherCouncils(String userId, UserRoleEnum councilRole, String currentCouncilId) {
        // Xác định cấp độ hội đồng từ role
        ReviewLevelEnum councilLevel = (councilRole == UserRoleEnum.TV_HOI_DONG_TRUONG)
                ? ReviewLevelEnum.TRUONG
                : ReviewLevelEnum.KHOA;

        // Kiểm tra xem user có còn là thành viên của hội đồng nào khác cùng cấp độ không
        List<CouncilMember> otherMemberships = councilMemberRepository.findByUserId(userId);

        // Lọc bỏ council hiện tại và chỉ lấy các hội đồng cùng cấp độ
        boolean isMemberOfOtherCouncilsSameLevel = otherMemberships.stream()
                .filter(member -> !member.getCouncil().getId().equals(currentCouncilId))
                .anyMatch(member -> member.getCouncil().getReviewCouncilLevel() == councilLevel);

        if (!isMemberOfOtherCouncilsSameLevel) {
            // User không còn là thành viên của hội đồng nào khác cùng cấp độ, gỡ role
            Role role = roleRepository.findByRoleName(councilRole)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy role: " + councilRole));

            userRoleRepository.deleteByUserIdAndRoleId(userId, role.getId());
        }
    }

    // 7. Lấy kết quả chấm điểm của hội đồng
    @Transactional(readOnly = true)
    public CouncilResultsResponse getCouncilResults(String councilId) {
        // Tìm và validate council
        Council council = findAndValidateCouncil(councilId);

        // Trigger lazy load
        council.getCouncilMembers().size();
        council.getInnovations().size();
        council.getInnovationRound().getId(); // Trigger lazy load

        // Lấy scoring phase end date
        LocalDate scoringEndDate = getScoringPhaseEndDate(council);
        LocalDate currentDate = LocalDate.now();
        boolean canViewResults = scoringEndDate != null && currentDate.isAfter(scoringEndDate);

        if (!canViewResults) {
                throw new IllegalArgumentException(
                    "Chưa hết thời gian chấm điểm. Chỉ có thể xem kết quả sau ngày " + scoringEndDate);
        }

        // Lấy danh sách member user IDs có quyền chấm điểm
        List<String> memberUserIds = getScoringMemberUserIds(council);
        int totalMembers = memberUserIds.size();

        // Lấy danh sách innovations
        List<Innovation> innovations = council.getInnovations();

        // Tính kết quả cho từng innovation
        List<InnovationResultDetail> innovationResults = new ArrayList<>();
        int completedCount = 0;
        int pendingCount = 0;
        
        // Map để đếm số innovation mà mỗi member đã chấm
        java.util.Map<String, Integer> memberScoredCount = new java.util.HashMap<>();
        for (String memberId : memberUserIds) {
            memberScoredCount.put(memberId, 0);
        }

        for (Innovation innovation : innovations) {
            InnovationResultDetail result = calculateInnovationResult(innovation, memberUserIds, totalMembers, council);
            innovationResults.add(result);

            if (result.getScoredMembers() == totalMembers) {
                completedCount++;
            } else {
                pendingCount++;
            }
            
            // Đếm số innovation mà mỗi member đã chấm
            for (MemberEvaluationDetail evaluation : result.getMemberEvaluations()) {
                if (evaluation.getHasScored() != null && evaluation.getHasScored()) {
                    memberScoredCount.put(evaluation.getMemberId(), 
                        memberScoredCount.get(evaluation.getMemberId()) + 1);
                }
            }
        }

        // Phân loại thành viên
        int notScoredAtAll = 0;      // Chưa chấm bất kỳ innovation nào
        int notScoredAll = 0;        // Đã chấm một số nhưng chưa chấm hết tất cả
        int totalInnovations = innovations.size();

        for (String memberId : memberUserIds) {
            int scoredCount = memberScoredCount.get(memberId);
            if (scoredCount == 0) {
                notScoredAtAll++;
            } else if (scoredCount < totalInnovations) {
                notScoredAll++;
            }
        }

        // Tạo warning message nếu có thành viên chưa chấm hoặc chưa chấm xong
        String warningMessage = null;
        if (notScoredAtAll > 0 || notScoredAll > 0) {
            StringBuilder message = new StringBuilder();
            if (notScoredAtAll > 0 && notScoredAll > 0) {
                message.append(String.format(
                    "Có %d thành viên chưa đánh giá và %d thành viên chưa đánh giá xong. Kết quả có thể không đầy đủ.",
                    notScoredAtAll, notScoredAll));
            } else if (notScoredAtAll > 0) {
                message.append(String.format(
                    "Có %d thành viên chưa đánh giá. Kết quả có thể không đầy đủ.",
                    notScoredAtAll));
            } else {
                message.append(String.format(
                    "Có %d thành viên chưa đánh giá xong. Kết quả có thể không đầy đủ.",
                    notScoredAll));
            }
            warningMessage = message.toString();
        }

        // Tạo response
        CouncilResultsResponse response = new CouncilResultsResponse();
        response.setCouncilId(council.getId());
        response.setCouncilName(council.getName());
        response.setReviewCouncilLevel(council.getReviewCouncilLevel().name());
        response.setScoringEndDate(scoringEndDate);
        response.setCanViewResults(true);
        response.setTotalInnovations(innovations.size());
        response.setCompletedInnovations(completedCount);
        response.setPendingInnovations(pendingCount);
        response.setWarningMessage(warningMessage);
        response.setInnovationResults(innovationResults);

        return response;
    }

    // Helper method: Lấy scoring phase end date
    private LocalDate getScoringPhaseEndDate(Council council) {
        ReviewLevelEnum councilLevel = council.getReviewCouncilLevel();
        InnovationRound round = council.getInnovationRound();

        if (councilLevel == ReviewLevelEnum.KHOA) {
            Department department = council.getDepartment();
            if (department == null) {
                return null;
            }

            Optional<DepartmentPhase> scoringPhase = departmentPhaseRepository
                    .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                            department.getId(),
                            round.getId(),
                            InnovationPhaseTypeEnum.SCORING);

            return scoringPhase.map(DepartmentPhase::getPhaseEndDate).orElse(null);
        } else {
            Optional<InnovationPhase> scoringPhase = innovationPhaseRepository
                    .findByInnovationRoundIdAndPhaseType(round.getId(), InnovationPhaseTypeEnum.SCORING);

            if (scoringPhase.isPresent() && scoringPhase.get().getLevel() == InnovationPhaseLevelEnum.SCHOOL) {
                return scoringPhase.get().getPhaseEndDate();
            }
            return null;
        }
    }

    // Helper method: Tính toán scoring period info cho council (không cần innovation)
    private ScoringPeriodInfo calculateScoringPeriodInfoForCouncil(Council council, InnovationRound round) {
        ReviewLevelEnum councilLevel = council.getReviewCouncilLevel();
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        PhaseStatusEnum phaseStatus;

        if (councilLevel == ReviewLevelEnum.KHOA) {
            Department department = council.getDepartment();
            if (department == null) {
                return new ScoringPeriodInfo(null, null, false, false, ScoringPeriodStatusEnum.NOT_STARTED);
            }

            Optional<DepartmentPhase> scoringPhaseOpt = departmentPhaseRepository
                    .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                            department.getId(),
                            round.getId(),
                            InnovationPhaseTypeEnum.SCORING);

            if (scoringPhaseOpt.isEmpty()) {
                return new ScoringPeriodInfo(null, null, false, false, ScoringPeriodStatusEnum.NOT_STARTED);
            }

            DepartmentPhase scoringPhase = scoringPhaseOpt.get();
            phaseStatus = scoringPhase.getPhaseStatus();
            startDate = scoringPhase.getPhaseStartDate();
            endDate = scoringPhase.getPhaseEndDate();
        } else {
            Optional<InnovationPhase> scoringPhaseOpt = innovationPhaseRepository
                    .findByInnovationRoundIdAndPhaseType(round.getId(), InnovationPhaseTypeEnum.SCORING);

            if (scoringPhaseOpt.isEmpty()) {
                return new ScoringPeriodInfo(null, null, false, false, ScoringPeriodStatusEnum.NOT_STARTED);
            }

            InnovationPhase scoringPhase = scoringPhaseOpt.get();
            if (scoringPhase.getLevel() != InnovationPhaseLevelEnum.SCHOOL) {
                return new ScoringPeriodInfo(null, null, false, false, ScoringPeriodStatusEnum.NOT_STARTED);
            }

            phaseStatus = scoringPhase.getPhaseStatus();
            startDate = scoringPhase.getPhaseStartDate();
            endDate = scoringPhase.getPhaseEndDate();
        }

        // Nếu không có startDate/endDate thì không thể tính toán
        if (startDate == null || endDate == null) {
            return new ScoringPeriodInfo(startDate, endDate, false, false, ScoringPeriodStatusEnum.NOT_STARTED);
        }

        // Tính toán status dựa trên currentDate vs startDate/endDate
        boolean canScore = false;
        boolean canView = false;
        ScoringPeriodStatusEnum status = ScoringPeriodStatusEnum.NOT_STARTED; // Giá trị mặc định

        // Xác định vị trí hiện tại so với thời gian chấm điểm
        boolean isBeforeStartDate = currentDate.isBefore(startDate);
        boolean isAfterEndDate = !currentDate.isBefore(endDate);

        if (isBeforeStartDate) {
            // Trường hợp 1: Chưa đến thời gian chấm điểm
            canScore = false; // Không cho phép chấm điểm
            
            // Kiểm tra xem có trong khoảng 3 ngày trước không (preview period)
            LocalDate previewStartDate = startDate.minusDays(3);
            boolean isInPreviewPeriod = !currentDate.isBefore(previewStartDate);
            
            if (isInPreviewPeriod) {
                // Trong khoảng 3 ngày trước: cho phép xem trước
                canView = true;
                status = ScoringPeriodStatusEnum.PREVIEW;
            } else {
                // Trước 4 ngày trở lên: không cho xem
                canView = false;
                status = ScoringPeriodStatusEnum.NOT_STARTED;
            }
            
        } else if (isAfterEndDate) {
            // Trường hợp 2: Đã hết thời gian chấm điểm
            canScore = false; // Không cho phép chấm điểm
            canView = true; // Vẫn cho phép xem để xem lại đánh giá đã chấm
            status = ScoringPeriodStatusEnum.ENDED;
            
        } else {
            // Trường hợp 3: Đang trong thời gian chấm điểm
            canView = true; // Luôn cho phép xem
            
            // Chỉ cho phép chấm điểm nếu phase status là ACTIVE
            boolean isPhaseActive = phaseStatus == PhaseStatusEnum.ACTIVE;
            canScore = isPhaseActive;
            status = ScoringPeriodStatusEnum.ACTIVE;
        }

        return new ScoringPeriodInfo(startDate, endDate, canScore, canView, status);
    }


    // Helper method: Tính kết quả cho một innovation
    private InnovationResultDetail calculateInnovationResult(Innovation innovation, List<String> memberUserIds,
            int totalMembers, Council council) {
        // Lấy danh sách đánh giá của từng thành viên
        List<MemberEvaluationDetail> memberEvaluations = new ArrayList<>();
        int scoredMembers = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        double totalScore = 0.0;
        int scoreCount = 0;

        // Lấy thông tin Chủ tịch để dùng cho tie-breaking
        Boolean chairmanDecision = null;
        Double chairmanScore = null;

        // Lấy danh sách council members để map role
        List<CouncilMember> councilMembers = councilMemberRepository.findByCouncilId(council.getId());
        java.util.Map<String, CouncilMemberRoleEnum> memberRoleMap = councilMembers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getUser().getId(),
                        CouncilMember::getRole));

        for (String memberUserId : memberUserIds) {
            // Lấy thông tin thành viên
            User member = userRepository.findById(memberUserId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user với ID: " + memberUserId));

            // Lấy role của thành viên trong council
            CouncilMemberRoleEnum memberRoleEnum = memberRoleMap.getOrDefault(memberUserId,
                    CouncilMemberRoleEnum.THANH_VIEN);
            String memberRole = memberRoleEnum.name();

            // Lấy đánh giá của thành viên
            Optional<ReviewScore> reviewScore = reviewScoreRepository
                    .findByInnovationIdAndReviewerId(innovation.getId(), memberUserId);

            MemberEvaluationDetail evaluation = new MemberEvaluationDetail();
            evaluation.setMemberId(memberUserId);
            evaluation.setMemberName(member.getFullName());
            evaluation.setMemberRole(memberRole);

            if (reviewScore.isPresent() && reviewScore.get().getIsApproved() != null) {
                // Đã chấm điểm
                scoredMembers++;
                evaluation.setHasScored(true);
                evaluation.setIsApproved(reviewScore.get().getIsApproved());
                evaluation.setTotalScore(reviewScore.get().getTotalScore());
                evaluation.setComments(reviewScore.get().getDetailedComments());
                evaluation.setReviewedAt(reviewScore.get().getReviewedAt());

                if (reviewScore.get().getIsApproved()) {
                    approvedCount++;
                } else {
                    rejectedCount++;
                }

                if (reviewScore.get().getTotalScore() != null) {
                    totalScore += reviewScore.get().getTotalScore();
                    scoreCount++;
                }

                // Lưu quyết định của Chủ tịch
                if (memberRoleEnum == CouncilMemberRoleEnum.CHU_TICH) {
                    chairmanDecision = reviewScore.get().getIsApproved();
                    chairmanScore = reviewScore.get().getTotalScore() != null
                            ? reviewScore.get().getTotalScore().doubleValue()
                            : null;
                }
            } else {
                // Chưa chấm điểm
                evaluation.setHasScored(false);
                evaluation.setIsApproved(null);
                evaluation.setTotalScore(null);
                evaluation.setComments(null);
                evaluation.setReviewedAt(null);
            }

            memberEvaluations.add(evaluation);
        }

        // Tính điểm trung bình
        Double averageScore = scoreCount > 0 ? totalScore / scoreCount : null;

        // Tính finalDecision với logic tie-breaking
        Boolean finalDecision = calculateFinalDecision(
                innovation.getIsScore(),
                approvedCount,
                rejectedCount,
                scoredMembers,
                totalMembers,
                averageScore,
                chairmanDecision,
                chairmanScore);

        // Tạo decision reason
        String decisionReason = generateDecisionReason(
                innovation.getIsScore(),
                approvedCount,
                rejectedCount,
                scoredMembers,
                totalMembers,
                averageScore,
                chairmanDecision);

        // Tạo result
        InnovationResultDetail result = new InnovationResultDetail();
        result.setInnovationId(innovation.getId());
        result.setInnovationName(innovation.getInnovationName());
        result.setAuthorName(innovation.getUser() != null ? innovation.getUser().getFullName() : "N/A");
        result.setDepartmentName(
                innovation.getDepartment() != null ? innovation.getDepartment().getDepartmentName() : null);
        result.setIsScore(innovation.getIsScore());
        result.setTotalMembers(totalMembers);
        result.setScoredMembers(scoredMembers);
        result.setApprovedCount(approvedCount);
        result.setRejectedCount(rejectedCount);
        result.setPendingCount(totalMembers - scoredMembers);
        result.setAverageScore(averageScore);
        result.setFinalDecision(finalDecision);
        result.setDecisionReason(decisionReason);
        result.setMemberEvaluations(memberEvaluations);

        return result;
    }

    // Helper method: Tính finalDecision với logic tie-breaking
    private Boolean calculateFinalDecision(Boolean isScore, int approvedCount, int rejectedCount, int scoredMembers,
            int totalMembers, Double averageScore, Boolean chairmanDecision, Double chairmanScore) {
        // Nếu chưa có ai chấm điểm (kể cả Chủ tịch)
        if (scoredMembers == 0) {
            return null;
        }

        // Nếu đa số thông qua → lấy kết quả, không cần Chủ tịch
        if (approvedCount > rejectedCount) {
            return true;
        }

        // Nếu đa số không thông qua → lấy kết quả, không cần Chủ tịch
        if (rejectedCount > approvedCount) {
            return false;
        }

        // Nếu bằng nhau (approvedCount == rejectedCount)
        // Trường hợp này chỉ xảy ra khi số thành viên chấm điểm là số chẵn
        // (vì tổng số thành viên luôn là số lẻ)
        // → Cần Chủ tịch chấm để quyết định

        // Tie-breaking 1: Nếu sáng kiến có chấm điểm, dùng điểm trung bình
        if (isScore != null && isScore && averageScore != null) {
            if (averageScore >= 70.0) {
                return true;
            } else if (averageScore < 70.0) {
                return false;
            }
            // Nếu averageScore == 70.0 (rất hiếm), tiếp tục tie-breaking 2
        }

        // Tie-breaking 2: Dùng quyết định của Chủ tịch
        if (chairmanDecision != null) {
            return chairmanDecision;
        }

        // Nếu bằng nhau và không có quyết định của Chủ tịch → null
        return null;
    }

    // Helper method: Tạo decision reason
    private String generateDecisionReason(Boolean isScore, int approvedCount, int rejectedCount, int scoredMembers,
            int totalMembers, Double averageScore, Boolean chairmanDecision) {
        if (scoredMembers == 0) {
            return "Chưa có thành viên nào chấm điểm";
        }

        // Đa số rõ ràng → không cần Chủ tịch
        if (approvedCount > rejectedCount) {
            return String.format("Đa số thông qua (%d/%d)", approvedCount, scoredMembers);
        }

        if (rejectedCount > approvedCount) {
            return String.format("Đa số không thông qua (%d/%d)", rejectedCount, scoredMembers);
        }

        // Bằng nhau → cần Chủ tịch
        if (isScore != null && isScore && averageScore != null) {
            if (averageScore >= 70.0) {
                return String.format("Bằng nhau (%d/%d) - Dựa vào điểm trung bình (%.2f >= 70)",
                        approvedCount, scoredMembers, averageScore);
            } else {
                return String.format("Bằng nhau (%d/%d) - Dựa vào điểm trung bình (%.2f < 70)",
                        approvedCount, scoredMembers, averageScore);
            }
        }

        if (chairmanDecision != null) {
            return String.format("Bằng nhau (%d/%d) - Quyết định của Chủ tịch (%s)",
                    approvedCount, scoredMembers, chairmanDecision ? "Thông qua" : "Không thông qua");
        }

        // Bằng nhau nhưng Chủ tịch chưa chấm
        return String.format("Bằng nhau (%d/%d) - Chưa có kết quả (Chủ tịch chưa chấm điểm)", approvedCount, scoredMembers);
    }
}