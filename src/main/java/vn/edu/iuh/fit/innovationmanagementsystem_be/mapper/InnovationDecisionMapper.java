package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;

@Mapper(componentModel = "spring")
public interface InnovationDecisionMapper {

    InnovationDecisionResponse toInnovationDecisionResponse(InnovationDecision innovationDecision);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decisionNumber", source = "decisionNumber")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "promulgatedDate", source = "promulgatedDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "innovationRounds", ignore = true)
    @Mapping(target = "reviewScores", ignore = true)
    InnovationDecision toInnovationDecision(InnovationDecisionRequest innovationDecisionRequest);

}
