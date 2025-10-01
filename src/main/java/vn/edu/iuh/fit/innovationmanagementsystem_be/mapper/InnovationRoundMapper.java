package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;

@Mapper(componentModel = "spring")
public interface InnovationRoundMapper {

    InnovationRoundMapper INSTANCE = Mappers.getMapper(InnovationRoundMapper.class);

//    @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
//    @Mapping(target = "innovationDecisionTitle", source = "innovationDecision.title")
    InnovationRoundResponse toInnovationRoundResponse(InnovationRound innovationRound);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "innovationDecision", ignore = true)
    @Mapping(target = "innovationPhases", ignore = true)
    @Mapping(target = "formTemplates", ignore = true)
    @Mapping(target = "innovations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    InnovationRound toInnovationRound(InnovationRoundRequest innovationRoundRequest);
}
