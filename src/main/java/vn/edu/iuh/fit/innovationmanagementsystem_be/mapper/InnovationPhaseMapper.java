package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;

@Mapper(componentModel = "spring")
public interface InnovationPhaseMapper {

    @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
    @Mapping(target = "innovationDecisionTitle", source = "innovationDecision.title")
    InnovationPhaseResponse toInnovationPhaseResponse(InnovationPhase innovationPhase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "roundStartDate", ignore = true)
    @Mapping(target = "roundEndDate", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "phaseType", source = "phaseType")
    @Mapping(target = "phaseStartDate", source = "phaseStartDate")
    @Mapping(target = "phaseEndDate", source = "phaseEndDate")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "phaseOrder", source = "phaseOrder")
    @Mapping(target = "innovationDecision", ignore = true)
    @Mapping(target = "formTemplates", ignore = true)
    @Mapping(target = "innovations", ignore = true)
    @Mapping(target = "departmentPhases", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    InnovationPhase toInnovationPhase(InnovationPhaseRequest innovationPhaseRequest);
}
