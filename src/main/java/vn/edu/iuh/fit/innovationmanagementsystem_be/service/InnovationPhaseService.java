package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class InnovationPhaseService {

    private final InnovationPhaseRepository innovationPhaseRepository;
    private final InnovationPhaseMapper innovationPhaseMapper;

    public InnovationPhaseService(InnovationPhaseRepository innovationPhaseRepository,
            InnovationPhaseMapper innovationPhaseMapper) {
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationPhaseMapper = innovationPhaseMapper;
    }

    // 1. Tạo phases cho InnovationRound
    public Set<InnovationPhase> createPhasesForRound(InnovationRound round,
            Set<InnovationPhaseRequest> phaseRequests) {

        // Kiểm tra thứ tự phase không được trùng lặp
        validatePhaseOrders(phaseRequests);

        // Kiểm tra các phase không chồng chéo thời gian
        validatePhaseOverlap(phaseRequests);

        // Kiểm tra các phase phải nằm trong thời gian của round
        validatePhasesWithinRound(round, phaseRequests);

        Set<InnovationPhase> phases = new HashSet<>();
        for (InnovationPhaseRequest req : phaseRequests) {
            InnovationPhase phase = createPhaseFromRequest(round, req);
            phases.add(phase);
        }

        return phases;
    }

    /**
     * Kiểm tra thứ tự phase không được trùng lặp
     */
    private void validatePhaseOrders(Set<InnovationPhaseRequest> phaseRequests) {
        Set<Integer> orders = new HashSet<>();
        for (InnovationPhaseRequest req : phaseRequests) {
            if (req.getPhaseOrder() != null) {
                if (orders.contains(req.getPhaseOrder())) {
                    throw new IdInvalidException(
                            "Thứ tự phase không được trùng lặp: phaseOrder=" + req.getPhaseOrder());
                }
                orders.add(req.getPhaseOrder());
            }
        }
    }

    /**
     * Kiểm tra các phase không chồng chéo thời gian
     */
    private void validatePhaseOverlap(Set<InnovationPhaseRequest> phaseRequests) {
        List<InnovationPhaseRequest> sortedPhases = phaseRequests.stream()
                .sorted((p1, p2) -> p1.getPhaseStartDate().compareTo(p2.getPhaseStartDate()))
                .collect(java.util.stream.Collectors.toList());

        for (int i = 0; i < sortedPhases.size(); i++) {
            InnovationPhaseRequest currentPhase = sortedPhases.get(i);

            for (int j = i + 1; j < sortedPhases.size(); j++) {
                InnovationPhaseRequest nextPhase = sortedPhases.get(j);

                if (nextPhase.getPhaseStartDate().isBefore(currentPhase.getPhaseEndDate()) ||
                        nextPhase.getPhaseStartDate().equals(currentPhase.getPhaseEndDate())) {
                    throw new IdInvalidException(
                            "Giai đoạn phải không được chồng chéo thời gian. Giai đoạn '" +
                                    nextPhase.getName() + "' bắt đầu (" + nextPhase.getPhaseStartDate() +
                                    ") phải sau ngày kết thúc của giai đoạn '" + currentPhase.getName() +
                                    "' (" + currentPhase.getPhaseEndDate() + ")");
                }
            }
        }
    }

    /**
     * Kiểm tra các phase phải nằm trong thời gian của round
     */
    private void validatePhasesWithinRound(InnovationRound round, Set<InnovationPhaseRequest> phaseRequests) {
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            if (!round.isPhaseWithinRoundTimeframe(phaseRequest.getPhaseStartDate(),
                    phaseRequest.getPhaseEndDate())) {
                throw new IdInvalidException(
                        "Thời gian giai đoạn phải nằm trong thời gian của InnovationRound. " +
                                "Round: " + round.getRegistrationStartDate() + " đến " +
                                round.getRegistrationEndDate() +
                                ", Phase '" + phaseRequest.getName() + "': " +
                                phaseRequest.getPhaseStartDate() + " đến " + phaseRequest.getPhaseEndDate());
            }
        }
    }

    /**
     * Helper method để tạo phase từ request
     */
    private InnovationPhase createPhaseFromRequest(InnovationRound round, InnovationPhaseRequest phaseRequest) {
        // Set default values if not provided
        if (phaseRequest.getName() == null) {
            phaseRequest.setName(round.getName() + " - " + phaseRequest.getPhaseType().name());
        }

        InnovationPhase phase = innovationPhaseMapper.toInnovationPhase(phaseRequest);
        phase.setInnovationRound(round);

        return innovationPhaseRepository.save(phase);
    }

    /**
     * Cập nhật trạng thái của một phase
     */
    public InnovationPhase updatePhaseStatus(String phaseId,
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum newStatus) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phase với id: " + phaseId));

        phase.setPhaseStatus(newStatus);
        return innovationPhaseRepository.save(phase);
    }

    /**
     * Lấy phase theo id
     */
    public InnovationPhase getPhaseById(String phaseId) {
        return innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phase với id: " + phaseId));
    }

    /**
     * Lấy tất cả phases theo round id
     */
    public List<InnovationPhase> getPhasesByRoundId(String roundId) {
        return innovationPhaseRepository.findByInnovationRoundIdOrderByPhaseOrder(roundId);
    }

}