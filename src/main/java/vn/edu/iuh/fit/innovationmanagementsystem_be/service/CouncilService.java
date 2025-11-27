package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public CouncilService(CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository,
            UserRepository userRepository,
            InnovationRepository innovationRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            CouncilMapper councilMapper,
            UserService userService,
            InnovationRoundRepository innovationRoundRepository,
            DepartmentRepository departmentRepository) {
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
        return councilMapper.toCouncilResponse(savedCouncil);
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
}
