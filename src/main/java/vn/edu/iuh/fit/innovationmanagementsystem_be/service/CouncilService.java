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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationWithScoreResponse;
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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ScoringProgressResponse;
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
            ReviewScoreRepository reviewScoreRepository) {
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

        // QUAN_TRI_VIEN_HE_THONG và QUAN_TRI_VIEN_QLKH_HTQT → TRUONG
        if (userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_HE_THONG)
                || userRoles.contains(UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT)) {
            return ReviewLevelEnum.TRUONG;
        }

        // Nếu không có role phù hợp, throw exception
        throw new IllegalArgumentException(
                "Bạn không có quyền tạo Hội đồng. Chỉ TRUONG_KHOA, QUAN_TRI_VIEN_KHOA, QUAN_TRI_VIEN_HE_THONG, QUAN_TRI_VIEN_QLKH_HTQT mới có quyền tạo Hội đồng");
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

    // Helper method: Lấy danh sách member user IDs có quyền chấm điểm (THANH_VIEN)
    private List<String> getScoringMemberUserIds(Council council) {
        return council.getCouncilMembers().stream()
                .filter(member -> member.getRole() == CouncilMemberRoleEnum.THANH_VIEN)
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

    // 5. Lấy danh sách hội đồng với pagination và filtering
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
}