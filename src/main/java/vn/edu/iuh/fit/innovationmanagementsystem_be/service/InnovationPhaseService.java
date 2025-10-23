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

        Set<InnovationPhase> phases = new HashSet<>();
        for (InnovationPhaseRequest req : phaseRequests) {
            InnovationPhase phase = createPhaseFromRequest(round, req);
            phases.add(phase);
        }

        return phases;
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

        // Kiểm tra thời gian giai đoạn
        if (!round.isPhaseWithinRoundTimeframe(phaseRequest.getPhaseStartDate(), phaseRequest.getPhaseEndDate())) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                    round.getRegistrationStartDate() + " đến " + round.getRegistrationEndDate());
        }

        return innovationPhaseRepository.save(phase);
    }

}