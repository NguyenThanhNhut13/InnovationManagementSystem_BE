package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;

@Mapper(componentModel = "spring")
public interface InnovationRoundMapper {

    @Mapping(target = "innovationDecision", source = "innovationDecision")
    @Mapping(target = "innovationPhase", source = "innovationPhases")
    @Mapping(target = "submissionCount", ignore = true)
    @Mapping(target = "reviewedCount", ignore = true)
    @Mapping(target = "approvedCount", ignore = true)
    InnovationRoundResponse toInnovationRoundResponse(InnovationRound innovationRound);

}
