package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChapterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ChapterResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
    @Mapping(target = "regulationIds", source = "regulations", qualifiedByName = "mapRegulationsToIds")
    ChapterResponse toChapterResponse(Chapter chapter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapterNumber", source = "chapterNumber")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "innovationDecision", ignore = true) // Will be set separately in service
    @Mapping(target = "regulations", ignore = true)
    Chapter toChapter(ChapterRequest chapterRequest);

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
