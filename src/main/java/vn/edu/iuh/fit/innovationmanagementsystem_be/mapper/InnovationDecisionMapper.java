package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InnovationDecisionMapper {

    @Mapping(target = "chapterIds", source = "chapters", qualifiedByName = "mapChaptersToIds")
    @Mapping(target = "regulationIds", source = "regulations", qualifiedByName = "mapRegulationsToIds")
    InnovationDecisionResponse toInnovationDecisionResponse(InnovationDecision innovationDecision);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decisionNumber", source = "decisionNumber")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "promulgatedDate", source = "promulgatedDate")
    @Mapping(target = "signedBy", source = "signedBy")
    @Mapping(target = "bases", source = "bases")
    @Mapping(target = "chapters", ignore = true)
    @Mapping(target = "regulations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "innovationPhases", ignore = true)
    @Mapping(target = "reviewScores", ignore = true)
    InnovationDecision toInnovationDecision(InnovationDecisionRequest innovationDecisionRequest);

    @Named("mapChaptersToIds")
    default List<String> mapChaptersToIds(
            List<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter> chapters) {
        if (chapters == null) {
            return Collections.emptyList();
        }
        return chapters.stream()
                .map(chapter -> chapter.getId())
                .collect(Collectors.toList());
    }

    @Named("mapRegulationsToIds")
    default List<String> mapRegulationsToIds(
            List<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation> regulations) {
        if (regulations == null) {
            return Collections.emptyList();
        }
        return regulations.stream()
                .map(regulation -> regulation.getId())
                .collect(Collectors.toList());
    }
}
