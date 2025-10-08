package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;

@Mapper(componentModel = "spring")
public interface InnovationPhaseMapper {

    InnovationPhaseResponse toInnovationPhaseResponse(InnovationPhase innovationPhase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "innovationRound", ignore = true)
    @Mapping(target = "phaseStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    InnovationPhase toInnovationPhase(InnovationPhaseRequest innovationPhaseRequest);
}
