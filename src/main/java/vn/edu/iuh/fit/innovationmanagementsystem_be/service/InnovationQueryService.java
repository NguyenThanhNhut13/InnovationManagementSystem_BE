package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterMyInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FilterAdminInnovationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.MyInnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InnovationQueryService {

        private static final Logger logger = LoggerFactory.getLogger(InnovationQueryService.class);

        private final InnovationRepository innovationRepository;
        private final FormDataRepository formDataRepository;
        private final DepartmentPhaseRepository departmentPhaseRepository;
        private final UserService userService;
        private final InnovationMapper innovationMapper;

        public InnovationQueryService(
                        InnovationRepository innovationRepository,
                        FormDataRepository formDataRepository,
                        DepartmentPhaseRepository departmentPhaseRepository,
                        UserService userService,
                        InnovationMapper innovationMapper) {
                this.innovationRepository = innovationRepository;
                this.formDataRepository = formDataRepository;
                this.departmentPhaseRepository = departmentPhaseRepository;
                this.userService = userService;
                this.innovationMapper = innovationMapper;
        }

        public ResultPaginationDTO getAllInnovationsByCurrentUserWithDetailedFilter(
                        FilterMyInnovationRequest filterRequest, Pageable pageable) {
                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                String currentUserId = userService.getCurrentUserId();

                Specification<Innovation> userSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .equal(root.get("user").get("id"), currentUserId);

                Specification<Innovation> filterSpec = buildFilterSpecification(filterRequest);

                Specification<Innovation> combinedSpec = filterSpec != null
                                ? userSpec.and(filterSpec)
                                : userSpec;

                Page<Innovation> innovations = innovationRepository.findAll(combinedSpec, pageable);

                List<String> innovationIds = innovations.getContent().stream()
                                .map(Innovation::getId)
                                .collect(Collectors.toList());

                List<FormData> allFormData = innovationIds.isEmpty()
                                ? new ArrayList<>()
                                : formDataRepository.findByInnovationIdsWithRelations(innovationIds);

                Map<String, Integer> authorCountMap = allFormData.stream()
                                .filter(fd -> fd.getFormField() != null
                                                && "danh_sach_tac_gia".equals(fd.getFormField().getFieldKey()))
                                .collect(Collectors.groupingBy(
                                                fd -> fd.getInnovation().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToInt(this::countAuthorsFromFormData)
                                                                                .max()
                                                                                .orElse(0))));

                Page<MyInnovationResponse> responses = innovations.map(innovation -> {
                        int authorCount = authorCountMap.getOrDefault(innovation.getId(), 0);
                        return toMyInnovationResponse(innovation, authorCount);
                });
                return Utils.toResultPaginationDTO(responses, pageable);
        }

        public ResultPaginationDTO getAllDepartmentInnovationsWithDetailedFilter(
                        FilterMyInnovationRequest filterRequest, Pageable pageable) {
                User currentUser = userService.getCurrentUser();

                boolean hasQuanTriVienKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_KHOA);
                boolean hasTruongKhoaRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.TRUONG_KHOA);

                if (!hasQuanTriVienKhoaRole && !hasTruongKhoaRole) {
                        throw new vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException(
                                        "Chỉ QUAN_TRI_VIEN_KHOA hoặc TRUONG_KHOA mới có quyền truy cập");
                }

                if (currentUser.getDepartment() == null) {
                        throw new vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException(
                                        "Người dùng không thuộc phòng ban nào");
                }

                Specification<Innovation> spec = buildFilterSpecification(filterRequest);

                Specification<Innovation> departmentSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .equal(root.get("department").get("id"), currentUser.getDepartment().getId());

                Specification<Innovation> notDraftSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .notEqual(root.get("status"), InnovationStatusEnum.DRAFT);

                if (spec != null) {
                        spec = spec.and(departmentSpec).and(notDraftSpec);
                } else {
                        spec = departmentSpec.and(notDraftSpec);
                }

                Page<Innovation> innovations = innovationRepository.findAll(spec, pageable);

                List<String> innovationIds = innovations.getContent().stream()
                                .map(Innovation::getId)
                                .collect(Collectors.toList());

                List<FormData> allFormData = innovationIds.isEmpty()
                                ? new ArrayList<>()
                                : formDataRepository.findByInnovationIdsWithRelations(innovationIds);

                Map<String, Integer> authorCountMap = allFormData.stream()
                                .filter(fd -> fd.getFormField() != null
                                                && "danh_sach_tac_gia".equals(fd.getFormField().getFieldKey()))
                                .collect(Collectors.groupingBy(
                                                fd -> fd.getInnovation().getId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                list -> list.stream()
                                                                                .mapToInt(this::countAuthorsFromFormData)
                                                                                .max()
                                                                                .orElse(0))));

                Page<MyInnovationResponse> responses = innovations.map(innovation -> {
                        int authorCount = authorCountMap.getOrDefault(innovation.getId(), 0);
                        return toMyInnovationResponse(innovation, authorCount);
                });

                return Utils.toResultPaginationDTO(responses, pageable);
        }

        public ResultPaginationDTO getAllInnovationsForAdminRolesWithFilter(
                        FilterAdminInnovationRequest filterRequest, Pageable pageable) {
                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                User currentUser = userService.getCurrentUser();

                boolean hasQuanTriVienQlkhHtqtRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT);

                boolean hasTvHoiDongTruongRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.TV_HOI_DONG_TRUONG);

                boolean hasQuanTriVienHeThongRole = currentUser.getUserRoles().stream()
                                .anyMatch(userRole -> userRole.getRole()
                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_HE_THONG);

                if (!hasQuanTriVienQlkhHtqtRole && !hasTvHoiDongTruongRole && !hasQuanTriVienHeThongRole) {
                        throw new vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException(
                                        "Chỉ QUAN_TRI_VIEN_QLKH_HTQT, TV_HOI_DONG_TRUONG hoặc QUAN_TRI_VIEN_HE_THONG mới có quyền truy cập");
                }

                Specification<Innovation> filterSpec = buildFilterSpecificationForAdmin(filterRequest);

                Specification<Innovation> notDraftSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                .notEqual(root.get("status"), InnovationStatusEnum.DRAFT);

                Specification<Innovation> combinedSpec = filterSpec != null
                                ? notDraftSpec.and(filterSpec)
                                : notDraftSpec;

                Page<Innovation> innovations = innovationRepository.findAll(combinedSpec, pageable);

                Page<InnovationResponse> responses = innovations.map(innovation -> {
                        InnovationResponse response = innovationMapper.toInnovationResponse(innovation);
                        response.setSubmissionTimeRemainingSeconds(getSubmissionTimeRemainingSeconds(innovation));
                        return response;
                });

                return Utils.toResultPaginationDTO(responses, pageable);
        }

        public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {
                if (pageable.getSort().isUnsorted()) {
                        pageable = PageRequest.of(
                                        pageable.getPageNumber(),
                                        pageable.getPageSize(),
                                        Sort.by("createdAt").descending());
                }

                Page<Innovation> innovations = innovationRepository.findAll(specification, pageable);
                Page<InnovationResponse> responses = innovations.map(innovation -> {
                        InnovationResponse response = innovationMapper.toInnovationResponse(innovation);
                        response.setSubmissionTimeRemainingSeconds(getSubmissionTimeRemainingSeconds(innovation));
                        return response;
                });
                return Utils.toResultPaginationDTO(responses, pageable);
        }

        private Specification<Innovation> buildFilterSpecification(FilterMyInnovationRequest filterRequest) {
                if (filterRequest == null) {
                        return null;
                }

                Specification<Innovation> spec = null;

                if (filterRequest.getSearchText() != null && !filterRequest.getSearchText().trim().isEmpty()) {
                        String searchText = "%" + filterRequest.getSearchText().trim().toLowerCase() + "%";
                        Specification<Innovation> searchSpec = (root, query, criteriaBuilder) -> {
                                jakarta.persistence.criteria.Predicate innovationNamePredicate = criteriaBuilder
                                                .like(criteriaBuilder.lower(root.get("innovationName")), searchText);

                                jakarta.persistence.criteria.Predicate authorNamePredicate = criteriaBuilder
                                                .like(criteriaBuilder.lower(root.get("user").get("fullName")),
                                                                searchText);

                                jakarta.persistence.criteria.Subquery<String> coAuthorSubquery = query
                                                .subquery(String.class);
                                jakarta.persistence.criteria.Root<CoInnovation> coInnovationRoot = coAuthorSubquery
                                                .from(CoInnovation.class);
                                coAuthorSubquery.select(coInnovationRoot.get("innovation").get("id"))
                                                .where(criteriaBuilder.and(
                                                                criteriaBuilder.equal(coInnovationRoot.get("innovation")
                                                                                .get("id"), root.get("id")),
                                                                criteriaBuilder.like(
                                                                                criteriaBuilder.lower(coInnovationRoot
                                                                                                .get("coInnovatorFullName")),
                                                                                searchText)));

                                jakarta.persistence.criteria.Predicate coAuthorPredicate = criteriaBuilder
                                                .exists(coAuthorSubquery);

                                return criteriaBuilder.or(innovationNamePredicate, authorNamePredicate,
                                                coAuthorPredicate);
                        };
                        spec = spec == null ? searchSpec : spec.and(searchSpec);
                }

                if (filterRequest.getStatus() != null) {
                        Specification<Innovation> statusSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("status"), filterRequest.getStatus());
                        spec = spec == null ? statusSpec : spec.and(statusSpec);
                }

                if (filterRequest.getInnovationRoundId() != null
                                && !filterRequest.getInnovationRoundId().trim().isEmpty()) {
                        Specification<Innovation> roundSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("innovationRound").get("id"),
                                                        filterRequest.getInnovationRoundId());
                        spec = spec == null ? roundSpec : spec.and(roundSpec);
                }

                if (filterRequest.getIsScore() != null) {
                        Specification<Innovation> isScoreSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("isScore"), filterRequest.getIsScore());
                        spec = spec == null ? isScoreSpec : spec.and(isScoreSpec);
                }

                return spec;
        }

        private Specification<Innovation> buildFilterSpecificationForAdmin(FilterAdminInnovationRequest filterRequest) {
                if (filterRequest == null) {
                        return null;
                }

                Specification<Innovation> spec = null;

                if (filterRequest.getSearchText() != null && !filterRequest.getSearchText().trim().isEmpty()) {
                        String searchText = "%" + filterRequest.getSearchText().trim().toLowerCase() + "%";
                        Specification<Innovation> searchSpec = (root, query, criteriaBuilder) -> {
                                jakarta.persistence.criteria.Predicate innovationNamePredicate = criteriaBuilder
                                                .like(criteriaBuilder.lower(root.get("innovationName")), searchText);

                                jakarta.persistence.criteria.Predicate authorNamePredicate = criteriaBuilder
                                                .like(criteriaBuilder.lower(root.get("user").get("fullName")),
                                                                searchText);

                                jakarta.persistence.criteria.Subquery<String> coAuthorSubquery = query
                                                .subquery(String.class);
                                jakarta.persistence.criteria.Root<CoInnovation> coInnovationRoot = coAuthorSubquery
                                                .from(CoInnovation.class);
                                coAuthorSubquery.select(coInnovationRoot.get("innovation").get("id"))
                                                .where(criteriaBuilder.and(
                                                                criteriaBuilder.equal(coInnovationRoot.get("innovation")
                                                                                .get("id"), root.get("id")),
                                                                criteriaBuilder.like(
                                                                                criteriaBuilder.lower(coInnovationRoot
                                                                                                .get("coInnovatorFullName")),
                                                                                searchText)));

                                jakarta.persistence.criteria.Predicate coAuthorPredicate = criteriaBuilder
                                                .exists(coAuthorSubquery);

                                return criteriaBuilder.or(innovationNamePredicate, authorNamePredicate,
                                                coAuthorPredicate);
                        };
                        spec = spec == null ? searchSpec : spec.and(searchSpec);
                }

                if (filterRequest.getStatus() != null) {
                        Specification<Innovation> statusSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("status"), filterRequest.getStatus());
                        spec = spec == null ? statusSpec : spec.and(statusSpec);
                }

                if (filterRequest.getInnovationRoundId() != null
                                && !filterRequest.getInnovationRoundId().trim().isEmpty()) {
                        Specification<Innovation> roundSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("innovationRound").get("id"),
                                                        filterRequest.getInnovationRoundId());
                        spec = spec == null ? roundSpec : spec.and(roundSpec);
                }

                if (filterRequest.getDepartmentId() != null
                                && !filterRequest.getDepartmentId().trim().isEmpty()) {
                        Specification<Innovation> departmentSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                                        .equal(root.get("department").get("id"),
                                                        filterRequest.getDepartmentId());
                        spec = spec == null ? departmentSpec : spec.and(departmentSpec);
                }

                return spec;
        }

        private Long getSubmissionTimeRemainingSeconds(Innovation innovation) {
                if (innovation == null || innovation.getDepartment() == null
                                || innovation.getInnovationRound() == null) {
                        return null;
                }

                try {
                        Optional<DepartmentPhase> departmentPhaseOpt = departmentPhaseRepository
                                        .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                        innovation.getDepartment().getId(),
                                                        innovation.getInnovationRound().getId(),
                                                        InnovationPhaseTypeEnum.SUBMISSION);

                        if (departmentPhaseOpt.isEmpty()) {
                                return null;
                        }

                        DepartmentPhase departmentPhase = departmentPhaseOpt.get();
                        LocalDate deadlineDate = departmentPhase.getPhaseEndDate();
                        return calculateTimeRemainingSeconds(deadlineDate);
                } catch (Exception e) {
                        logger.warn("Lỗi khi tính số giây deadline cho innovation {}: {}", innovation.getId(),
                                        e.getMessage());
                        return null;
                }
        }

        private Long calculateTimeRemainingSeconds(LocalDate deadlineDate) {
                if (deadlineDate == null) {
                        return null;
                }

                LocalDateTime deadlineDateTime = deadlineDate.atTime(LocalTime.MAX);
                LocalDateTime now = LocalDateTime.now();

                long secondsBetween = ChronoUnit.SECONDS.between(deadlineDateTime, now);

                if (secondsBetween <= 0) {
                        return 0L;
                } else {
                        return secondsBetween;
                }
        }

        private int countAuthorsFromFormData(FormData formData) {
                if (formData == null || formData.getFieldValue() == null) {
                        return 0;
                }

                try {
                        JsonNode fieldValue = formData.getFieldValue();
                        JsonNode valueNode = null;

                        if (fieldValue.has("value")) {
                                valueNode = fieldValue.get("value");
                        } else if (fieldValue.isArray()) {
                                valueNode = fieldValue;
                        }

                        if (valueNode != null && valueNode.isArray()) {
                                return valueNode.size();
                        }
                } catch (Exception e) {
                        logger.warn("Lỗi khi đếm số tác giả từ FormData: {}", e.getMessage());
                }

                return 0;
        }

        private MyInnovationResponse toMyInnovationResponse(Innovation innovation, int authorCount) {
                MyInnovationResponse response = new MyInnovationResponse();
                response.setInnovationId(innovation.getId());
                response.setInnovationName(innovation.getInnovationName());
                response.setStatus(innovation.getStatus());
                response.setSubmissionTimeRemainingSeconds(getSubmissionTimeRemainingSeconds(innovation));
                response.setIsScore(innovation.getIsScore());

                if (innovation.getUser() != null) {
                        response.setAuthorName(innovation.getUser().getFullName());
                }

                if (innovation.getInnovationRound() != null) {
                        response.setAcademicYear(innovation.getInnovationRound().getAcademicYear());
                        response.setInnovationRoundName(innovation.getInnovationRound().getName());
                }

                response.setIsCoAuthor(authorCount >= 2);

                response.setCreatedAt(innovation.getCreatedAt());
                response.setUpdatedAt(innovation.getUpdatedAt());

                return response;
        }
}
