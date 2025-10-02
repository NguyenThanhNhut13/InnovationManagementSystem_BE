package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChapterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ChapterResponse;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    @Mapping(target = "innovationDecisionId", source = "innovationDecision.id")
    ChapterResponse toChapterResponse(Chapter chapter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapterNumber", source = "chapterNumber")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "innovationDecision", ignore = true) // Will be set separately in service
    Chapter toChapter(ChapterRequest chapterRequest);

}
