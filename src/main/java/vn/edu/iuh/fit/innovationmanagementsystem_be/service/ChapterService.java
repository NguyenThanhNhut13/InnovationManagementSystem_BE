package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.ChapterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.ChapterResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ChapterRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;

    public ChapterService(ChapterRepository chapterRepository,
            InnovationDecisionRepository innovationDecisionRepository) {
        this.chapterRepository = chapterRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
    }

    // 1. Create Chapter
    @Transactional
    public ChapterResponse createChapter(ChapterRequest request) {
        // Kiểm tra InnovationDecision tồn tại
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));

        // Kiểm tra Chapter number đã tồn tại trong InnovationDecision
        if (chapterRepository.existsByChapterNumberAndInnovationDecisionId(request.getChapterNumber(),
                request.getInnovationDecisionId())) {
            throw new IdInvalidException("Số hiệu chương đã tồn tại trong quyết định này");
        }

        Chapter chapter = new Chapter();
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setTitle(request.getTitle());
        chapter.setInnovationDecision(innovationDecision);

        chapterRepository.save(chapter);
        return toChapterResponse(chapter);
    }

    // 2. Get All Chapters
    public ResultPaginationDTO getAllChapters(Specification<Chapter> specification, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findAll(specification, pageable);
        Page<ChapterResponse> responses = chapters.map(this::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get Chapter by Id
    public ChapterResponse getChapterById(String id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));
        return toChapterResponse(chapter);
    }

    // 4. Update Chapter
    @Transactional
    public ChapterResponse updateChapter(String id, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chương có ID: " + id));

        if (request.getChapterNumber() != null && !request.getChapterNumber().equals(chapter.getChapterNumber())) {
            if (chapterRepository.existsByChapterNumberAndInnovationDecisionId(request.getChapterNumber(),
                    chapter.getInnovationDecision().getId())) {
                throw new IdInvalidException("Số hiệu chương đã tồn tại trong quyết định này");
            }
            chapter.setChapterNumber(request.getChapterNumber());
        }

        if (request.getTitle() != null) {
            chapter.setTitle(request.getTitle());
        }

        chapterRepository.save(chapter);
        return toChapterResponse(chapter);
    }

    // 5. Delete Chapter
    @Transactional
    public void deleteChapter(String id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy chương có ID: " + id));
        chapterRepository.delete(chapter);
    }

    // 6. Get Chapters by InnovationDecision
    public ResultPaginationDTO getChaptersByInnovationDecision(String innovationDecisionId, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByInnovationDecisionId(innovationDecisionId, pageable);
        Page<ChapterResponse> responses = chapters.map(this::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 7. Search Chapters by keyword
    public ResultPaginationDTO searchChapters(String keyword, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByTitleContaining(keyword, pageable);
        Page<ChapterResponse> responses = chapters.map(this::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 8. Search Chapters by InnovationDecision and keyword
    public ResultPaginationDTO searchChaptersByInnovationDecision(String innovationDecisionId, String keyword,
            Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByInnovationDecisionIdAndTitleContaining(innovationDecisionId,
                keyword, pageable);
        Page<ChapterResponse> responses = chapters.map(this::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // Convert to Response DTO
    private ChapterResponse toChapterResponse(Chapter chapter) {
        ChapterResponse response = new ChapterResponse();
        response.setId(chapter.getId());
        response.setChapterNumber(chapter.getChapterNumber());
        response.setTitle(chapter.getTitle());
        response.setInnovationDecisionId(chapter.getInnovationDecision().getId());

        // Set related regulation IDs
        if (chapter.getRegulations() != null) {
            response.setRegulationIds(chapter.getRegulations().stream()
                    .map(regulation -> regulation.getId())
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
