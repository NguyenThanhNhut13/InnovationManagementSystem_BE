package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RegulationResponse;

@Mapper(componentModel = "spring")
public interface RegulationMapper {

    @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
    @Mapping(target = "chapterId", source = "chapter.id")
    RegulationResponse toRegulationResponse(Regulation regulation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clauseNumber", source = "clauseNumber")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "innovationDecision", ignore = true) // Will be set separately in service
    @Mapping(target = "chapter", ignore = true) // Will be set separately in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Regulation toRegulation(RegulationRequest regulationRequest);
}
