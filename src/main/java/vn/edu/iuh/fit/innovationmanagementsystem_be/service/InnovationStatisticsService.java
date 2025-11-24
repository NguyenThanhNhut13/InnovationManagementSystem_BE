package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlineResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InnovationStatisticsService {

        private final InnovationRepository innovationRepository;
        private final InnovationPhaseRepository innovationPhaseRepository;
        private final InnovationRoundService innovationRoundService;
        private final UserService userService;

        public InnovationStatisticsService(
                        InnovationRepository innovationRepository,
                        InnovationPhaseRepository innovationPhaseRepository,
                        InnovationRoundService innovationRoundService,
                        UserService userService) {
                this.innovationRepository = innovationRepository;
                this.innovationPhaseRepository = innovationPhaseRepository;
                this.innovationRoundService = innovationRoundService;
                this.userService = userService;
        }

        public InnovationStatisticsDTO getInnovationStatisticsForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();

                long totalInnovations = innovationRepository.countByUserId(userId);

                List<InnovationStatusEnum> submittedStatuses = Arrays.asList(
                                InnovationStatusEnum.DRAFT,
                                InnovationStatusEnum.SUBMITTED,
                                InnovationStatusEnum.PENDING_KHOA_REVIEW,
                                InnovationStatusEnum.KHOA_REVIEWED,
                                InnovationStatusEnum.KHOA_APPROVED,
                                InnovationStatusEnum.PENDING_TRUONG_REVIEW,
                                InnovationStatusEnum.TRUONG_REVIEWED);

                List<InnovationStatusEnum> approvedStatuses = Arrays.asList(
                                InnovationStatusEnum.TRUONG_APPROVED,
                                InnovationStatusEnum.FINAL_APPROVED);

                List<InnovationStatusEnum> rejectedStatuses = Arrays.asList(
                                InnovationStatusEnum.TRUONG_REJECTED,
                                InnovationStatusEnum.KHOA_REJECTED);

                long submittedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, submittedStatuses);
                long approvedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, approvedStatuses);
                long rejectedInnovations = innovationRepository.countByUserIdAndStatusIn(userId, rejectedStatuses);

                double achievedPercentage = totalInnovations > 0 ? (double) approvedInnovations / totalInnovations * 100
                                : 0.0;
                double notAchievedPercentage = totalInnovations > 0
                                ? (double) rejectedInnovations / totalInnovations * 100
                                : 0.0;
                double pendingPercentage = totalInnovations > 0 ? (double) submittedInnovations / totalInnovations * 100
                                : 0.0;

                return InnovationStatisticsDTO.builder()
                                .totalInnovations(totalInnovations)
                                .submittedInnovations(submittedInnovations)
                                .approvedInnovations(approvedInnovations)
                                .rejectedInnovations(rejectedInnovations)
                                .achievedPercentage(Math.round(achievedPercentage * 100.0) / 100.0)
                                .notAchievedPercentage(Math.round(notAchievedPercentage * 100.0) / 100.0)
                                .pendingPercentage(Math.round(pendingPercentage * 100.0) / 100.0)
                                .build();
        }

        public InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYearForCurrentUser() {
                User currentUser = userService.getCurrentUser();
                String userId = currentUser.getId();
                return getInnovationStatisticsByAcademicYear(userId);
        }

        private InnovationAcademicYearStatisticsDTO getInnovationStatisticsByAcademicYear(String userId) {
                List<Object[]> totalInnovationsByYear = innovationRepository
                                .countInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> submittedInnovationsByYear = innovationRepository
                                .countSubmittedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> approvedInnovationsByYear = innovationRepository
                                .countApprovedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> rejectedInnovationsByYear = innovationRepository
                                .countRejectedInnovationsByAcademicYearAndUserId(userId);
                List<Object[]> pendingInnovationsByYear = innovationRepository
                                .countPendingInnovationsByAcademicYearAndUserId(userId);

                Map<String, Long> totalMap = totalInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> submittedMap = submittedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> approvedMap = approvedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> rejectedMap = rejectedInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Map<String, Long> pendingMap = pendingInnovationsByYear.stream()
                                .collect(Collectors.toMap(
                                                arr -> (String) arr[0],
                                                arr -> (Long) arr[1]));

                Set<String> allAcademicYears = new java.util.HashSet<>();
                allAcademicYears.addAll(totalMap.keySet());
                allAcademicYears.addAll(submittedMap.keySet());
                allAcademicYears.addAll(approvedMap.keySet());
                allAcademicYears.addAll(rejectedMap.keySet());
                allAcademicYears.addAll(pendingMap.keySet());

                List<String> sortedAcademicYears = allAcademicYears.stream()
                                .sorted()
                                .collect(Collectors.toList());

                List<InnovationAcademicYearStatisticsDTO.AcademicYearData> academicYearDataList = sortedAcademicYears
                                .stream()
                                .map(academicYear -> {
                                        long totalInnovations = totalMap.getOrDefault(academicYear, 0L);
                                        long submittedInnovations = submittedMap.getOrDefault(academicYear, 0L);
                                        long approvedInnovations = approvedMap.getOrDefault(academicYear, 0L);
                                        long rejectedInnovations = rejectedMap.getOrDefault(academicYear, 0L);
                                        long pendingInnovations = pendingMap.getOrDefault(academicYear, 0L);

                                        double approvedPercentage = totalInnovations > 0
                                                        ? Math.round((double) approvedInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;
                                        double rejectedPercentage = totalInnovations > 0
                                                        ? Math.round((double) rejectedInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;
                                        double pendingPercentage = totalInnovations > 0
                                                        ? Math.round((double) pendingInnovations / totalInnovations
                                                                        * 100 * 100.0) / 100.0
                                                        : 0.0;

                                        return InnovationAcademicYearStatisticsDTO.AcademicYearData.builder()
                                                        .academicYear(academicYear)
                                                        .totalInnovations(totalInnovations)
                                                        .submittedInnovations(submittedInnovations)
                                                        .approvedInnovations(approvedInnovations)
                                                        .rejectedInnovations(rejectedInnovations)
                                                        .pendingInnovations(pendingInnovations)
                                                        .approvedPercentage(approvedPercentage)
                                                        .rejectedPercentage(rejectedPercentage)
                                                        .pendingPercentage(pendingPercentage)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                long totalInnovations = totalMap.values().stream().mapToLong(Long::longValue).sum();

                return InnovationAcademicYearStatisticsDTO.builder()
                                .academicYearData(academicYearDataList)
                                .totalInnovations(totalInnovations)
                                .totalAcademicYears(sortedAcademicYears.size())
                                .build();
        }

        public UpcomingDeadlinesResponse getUpcomingDeadlines() {
                var currentRound = innovationRoundService.getCurrentRound();
                if (currentRound == null) {
                        return UpcomingDeadlinesResponse.builder()
                                        .upcomingDeadlines(List.of())
                                        .totalDeadlines(0)
                                        .currentRoundName("Không có đợt sáng kiến nào đang diễn ra")
                                        .academicYear("")
                                        .build();
                }

                List<InnovationPhase> phases = innovationPhaseRepository
                                .findByInnovationRoundIdOrderByPhaseOrder(currentRound.getId());

                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                List<UpcomingDeadlineResponse> upcomingDeadlines = phases.stream()
                                .filter(phase -> phase.getPhaseEndDate().isAfter(today)
                                                || phase.getPhaseEndDate().isEqual(today))
                                .map(phase -> {
                                        long daysRemaining = ChronoUnit.DAYS.between(today, phase.getPhaseEndDate());

                                        return UpcomingDeadlineResponse.builder()
                                                        .id(phase.getId())
                                                        .title(generateDeadlineTitle(phase))
                                                        .deadlineDate(phase.getPhaseEndDate())
                                                        .formattedDate(phase.getPhaseEndDate().format(formatter))
                                                        .daysRemaining(daysRemaining)
                                                        .phaseType(phase.getPhaseType().getValue())
                                                        .level(phase.getLevel() != null ? phase.getLevel().getValue()
                                                                        : "")
                                                        .description(phase.getDescription())
                                                        .isDeadline(phase.getIsDeadline())
                                                        .build();
                                })
                                .sorted((a, b) -> a.getDeadlineDate().compareTo(b.getDeadlineDate()))
                                .collect(Collectors.toList());

                return UpcomingDeadlinesResponse.builder()
                                .upcomingDeadlines(upcomingDeadlines)
                                .totalDeadlines(upcomingDeadlines.size())
                                .currentRoundName(currentRound.getName())
                                .academicYear(currentRound.getAcademicYear())
                                .build();
        }

        private String generateDeadlineTitle(InnovationPhase phase) {
                String baseTitle = phase.getName();

                switch (phase.getPhaseType()) {
                        case SUBMISSION:
                                return "Hạn nộp " + baseTitle;
                        case SCORING:
                                return "Hạn chấm điểm " + baseTitle;
                        case ANNOUNCEMENT:
                                return "Hạn công bố " + baseTitle;
                        default:
                                return baseTitle;
                }
        }
}
