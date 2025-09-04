package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChapterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleChaptersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ChapterResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleChaptersResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ChapterRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.ChapterMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final ChapterMapper chapterMapper;

    public ChapterService(ChapterRepository chapterRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            ChapterMapper chapterMapper) {
        this.chapterRepository = chapterRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.chapterMapper = chapterMapper;
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

        Chapter chapter = chapterMapper.toChapter(request);
        chapter.setInnovationDecision(innovationDecision);

        chapterRepository.save(chapter);
        return chapterMapper.toChapterResponse(chapter);
    }

    // 2. Create Multiple Chapters
    @Transactional
    public CreateMultipleChaptersResponse createMultipleChapters(CreateMultipleChaptersRequest request) {
        // Kiểm tra InnovationDecision tồn tại
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));

        // Kiểm tra danh sách chương không được rỗng
        if (request.getChapters() == null || request.getChapters().isEmpty()) {
            throw new IdInvalidException("Danh sách chương không được để trống");
        }

        // Kiểm tra trùng lặp số hiệu chương trong request
        List<String> chapterNumbers = request.getChapters().stream()
                .map(CreateMultipleChaptersRequest.ChapterData::getChapterNumber)
                .collect(Collectors.toList());

        if (chapterNumbers.size() != chapterNumbers.stream().distinct().count()) {
            throw new IdInvalidException("Danh sách chương có số hiệu trùng lặp");
        }

        // Kiểm tra số hiệu chương đã tồn tại trong database
        for (CreateMultipleChaptersRequest.ChapterData chapterData : request.getChapters()) {
            if (chapterRepository.existsByChapterNumberAndInnovationDecisionId(
                    chapterData.getChapterNumber(), request.getInnovationDecisionId())) {
                throw new IdInvalidException("Số hiệu chương '" + chapterData.getChapterNumber() +
                        "' đã tồn tại trong quyết định này");
            }
        }

        // Tạo danh sách chương
        List<Chapter> chapters = request.getChapters().stream()
                .map(chapterData -> {
                    Chapter chapter = new Chapter();
                    chapter.setChapterNumber(chapterData.getChapterNumber());
                    chapter.setTitle(chapterData.getTitle());
                    chapter.setInnovationDecision(innovationDecision);
                    return chapter;
                })
                .collect(Collectors.toList());

        // Lưu tất cả chương
        List<Chapter> savedChapters = chapterRepository.saveAll(chapters);

        // Chuyển đổi sang response
        List<ChapterResponse> chapterResponses = savedChapters.stream()
                .map(chapterMapper::toChapterResponse)
                .collect(Collectors.toList());

        return new CreateMultipleChaptersResponse(request.getInnovationDecisionId(), chapterResponses);
    }

    // 3. Get All Chapters
    public ResultPaginationDTO getAllChapters(Specification<Chapter> specification, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findAll(specification, pageable);
        Page<ChapterResponse> responses = chapters.map(chapterMapper::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 4. Get Chapter by Id
    public ChapterResponse getChapterById(String id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));
        return chapterMapper.toChapterResponse(chapter);
    }

    // 5. Update Chapter
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
        return chapterMapper.toChapterResponse(chapter);
    }

    // 6. Get Chapters by InnovationDecision
    public ResultPaginationDTO getChaptersByInnovationDecision(String innovationDecisionId, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByInnovationDecisionId(innovationDecisionId, pageable);
        Page<ChapterResponse> responses = chapters.map(chapterMapper::toChapterResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

}
