package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AddCouncilMemberRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilMemberResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.CouncilMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserRepository userRepository;
    private final CouncilMapper councilMapper;

    public CouncilService(CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository,
            UserRepository userRepository,
            CouncilMapper councilMapper) {
        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.userRepository = userRepository;
        this.councilMapper = councilMapper;
    }

    // 1. Tạo Council
    @Transactional
    public CouncilResponse createCouncil(@NonNull CouncilRequest councilRequest) {
        if (councilRepository.existsByName(councilRequest.getName())) {
            throw new IdInvalidException("Tên hội đồng đã tồn tại");
        }

        Council council = councilMapper.toCouncil(councilRequest);
        council = councilRepository.save(council);

        // Thêm thành viên nếu có
        if (councilRequest.getMemberIds() != null && !councilRequest.getMemberIds().isEmpty()) {
            addMembersToCouncil(council.getId(), councilRequest.getMemberIds());
        }

        return councilMapper.toCouncilResponse(council);
    }

    // 2. Lấy tất cả Councils
    public ResultPaginationDTO getAllCouncils(@NonNull Specification<Council> specification,
            @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Council> councils = councilRepository.findAll(specification, pageable);
        Page<CouncilResponse> councilResponses = councils.map(councilMapper::toCouncilResponse);
        return Utils.toResultPaginationDTO(councilResponses, pageable);
    }

    // 3. Lấy Council by Id
    public CouncilResponse getCouncilById(@NonNull String id) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Hội đồng không tồn tại"));
        return councilMapper.toCouncilResponse(council);
    }

    // 4. Cập nhật Council
    @Transactional
    public CouncilResponse updateCouncil(@NonNull String id, @NonNull CouncilRequest councilRequest) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hội đồng có ID: " + id));

        if (councilRequest.getName() != null) {
            if (!council.getName().equals(councilRequest.getName()) &&
                    councilRepository.existsByName(councilRequest.getName())) {
                throw new IdInvalidException("Tên hội đồng đã tồn tại");
            }
            council.setName(councilRequest.getName());
        }

        if (councilRequest.getReviewCouncilLevel() != null) {
            council.setReviewCouncilLevel(councilRequest.getReviewCouncilLevel());
        }

        council = councilRepository.save(council);
        return councilMapper.toCouncilResponse(council);
    }

    // 5. Tìm kiếm Councils by Name
    public ResultPaginationDTO searchCouncilsByName(@NonNull String keyword, @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Council> councils = councilRepository.findByNameContaining(keyword, pageable);
        Page<CouncilResponse> councilResponses = councils.map(councilMapper::toCouncilResponse);
        return Utils.toResultPaginationDTO(councilResponses, pageable);
    }

    // 6. Lấy Councils by Review Level
    public ResultPaginationDTO getCouncilsByReviewLevel(@NonNull ReviewLevelEnum level, @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Council> councils = councilRepository.findByReviewLevel(level, pageable);
        Page<CouncilResponse> councilResponses = councils.map(councilMapper::toCouncilResponse);
        return Utils.toResultPaginationDTO(councilResponses, pageable);
    }

    // 7. Lấy Active Councils
    public ResultPaginationDTO getActiveCouncils(@NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<Council> councils = councilRepository.findActiveCouncils(pageable);
        Page<CouncilResponse> councilResponses = councils.map(councilMapper::toCouncilResponse);
        return Utils.toResultPaginationDTO(councilResponses, pageable);
    }

    // 8. Lấy tất cả Active Councils (List)
    public List<CouncilResponse> getAllActiveCouncils() {
        List<Council> councils = councilRepository.findAllActiveCouncils();
        return councils.stream()
                .map(councilMapper::toCouncilResponse)
                .collect(Collectors.toList());
    }

    // 9. Lấy Councils by Review Level (List)
    public List<CouncilResponse> getActiveCouncilsByReviewLevel(@NonNull ReviewLevelEnum level) {
        List<Council> councils = councilRepository.findActiveCouncilsByReviewLevel(level);
        return councils.stream()
                .map(councilMapper::toCouncilResponse)
                .collect(Collectors.toList());
    }

    // COUNCIL MEMBER MANAGEMENT

    // 10. Thêm Member vào Council
    @Transactional
    public CouncilMemberResponse addMemberToCouncil(@NonNull String councilId,
            @NonNull AddCouncilMemberRequest request) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new IdInvalidException("Hội đồng không tồn tại"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại"));

        if (councilMemberRepository.existsByCouncilIdAndUserId(councilId, request.getUserId())) {
            throw new IdInvalidException("Người dùng đã là thành viên của hội đồng này");
        }

        CouncilMember councilMember = new CouncilMember();
        councilMember.setCouncil(council);
        councilMember.setUser(user);
        councilMember = councilMemberRepository.save(councilMember);

        return councilMapper.toCouncilMemberResponse(councilMember);
    }

    // 11. Xóa Member khỏi Council
    @Transactional
    public void removeMemberFromCouncil(@NonNull String councilId, @NonNull String userId) {
        CouncilMember councilMember = councilMemberRepository
                .findByCouncilIdAndUserId(councilId, userId)
                .orElseThrow(() -> new IdInvalidException("Thành viên không tồn tại trong hội đồng"));

        councilMemberRepository.delete(councilMember);
    }

    // 12. Lấy Council Members
    public ResultPaginationDTO getCouncilMembers(@NonNull String councilId, @NonNull Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        if (!councilRepository.existsById(councilId)) {
            throw new IdInvalidException("Hội đồng không tồn tại");
        }

        Page<CouncilMember> councilMembers = councilMemberRepository.findByCouncilId(councilId, pageable);
        Page<CouncilMemberResponse> memberResponses = councilMembers.map(councilMapper::toCouncilMemberResponse);
        return Utils.toResultPaginationDTO(memberResponses, pageable);
    }

    // 13. Lấy tất cả Council Members (List)
    public List<CouncilMemberResponse> getAllCouncilMembers(@NonNull String councilId) {
        if (!councilRepository.existsById(councilId)) {
            throw new IdInvalidException("Hội đồng không tồn tại");
        }

        List<CouncilMember> councilMembers = councilMemberRepository.findAllByCouncilId(councilId);
        return councilMembers.stream()
                .map(councilMapper::toCouncilMemberResponse)
                .collect(Collectors.toList());
    }

    // 14. Lấy Councils của User
    public List<CouncilResponse> getUserCouncils(@NonNull String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        List<CouncilMember> userCouncilMembers = councilMemberRepository.findActiveCouncilMembersByUserId(userId);
        return userCouncilMembers.stream()
                .map(CouncilMember::getCouncil)
                .map(councilMapper::toCouncilResponse)
                .collect(Collectors.toList());
    }

    /*
     * Helper method thêm nhiều thành viên
     */
    private void addMembersToCouncil(String councilId, List<String> memberIds) {
        for (String memberId : memberIds) {
            if (!councilMemberRepository.existsByCouncilIdAndUserId(councilId, memberId)) {
                User user = userRepository.findById(memberId)
                        .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại: " + memberId));

                CouncilMember councilMember = new CouncilMember();
                councilMember.setCouncil(councilRepository.findById(councilId).orElse(null));
                councilMember.setUser(user);
                councilMemberRepository.save(councilMember);
            }
        }
    }
}
