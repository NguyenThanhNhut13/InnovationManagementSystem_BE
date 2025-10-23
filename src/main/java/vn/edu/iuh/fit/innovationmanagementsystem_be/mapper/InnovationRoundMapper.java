package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;

@Mapper(componentModel = "spring")
public interface InnovationRoundMapper {

    InnovationRoundMapper INSTANCE = Mappers.getMapper(InnovationRoundMapper.class);

    // @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
    // @Mapping(target = "innovationDecisionTitle", source =
    // "innovationDecision.title")
    @Mapping(target = "innovationDecision", source = "innovationDecision")
    @Mapping(target = "innovationPhase", source = "innovationPhases")
    @Mapping(target = "submissionCount", ignore = true)
    @Mapping(target = "reviewedCount", ignore = true)
    @Mapping(target = "approvedCount", ignore = true)
    InnovationRoundResponse toInnovationRoundResponse(InnovationRound innovationRound);

}
