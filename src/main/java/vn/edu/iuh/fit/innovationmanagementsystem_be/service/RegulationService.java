package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.RegulationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RegulationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ChapterRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegulationService {

    private final RegulationRepository regulationRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final ChapterRepository chapterRepository;

    public RegulationService(RegulationRepository regulationRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            ChapterRepository chapterRepository) {
        this.regulationRepository = regulationRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.chapterRepository = chapterRepository;
    }

    // 1. Create Regulation
    @Transactional
    public RegulationResponse createRegulation(RegulationRequest request) {
        // Kiểm tra InnovationDecision tồn tại
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));

        // Kiểm tra Chapter tồn tại nếu có
        Chapter chapter = null;
        if (request.getChapterId() != null && !request.getChapterId().isEmpty()) {
            chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));

            // Kiểm tra Chapter có thuộc InnovationDecision không
            if (!chapter.getInnovationDecision().getId().equals(request.getInnovationDecisionId())) {
                throw new IdInvalidException("Chương không thuộc quyết định này");
            }
        }

        // Kiểm tra Regulation number đã tồn tại trong InnovationDecision
        if (regulationRepository.existsByClauseNumberAndInnovationDecisionId(request.getClauseNumber(),
                request.getInnovationDecisionId())) {
            throw new IdInvalidException("Số hiệu điều đã tồn tại trong quyết định này");
        }

        Regulation regulation = new Regulation();
        regulation.setClauseNumber(request.getClauseNumber());
        regulation.setTitle(request.getTitle());
        regulation.setContent(request.getContent());
        regulation.setInnovationDecision(innovationDecision);
        regulation.setChapter(chapter);

        regulationRepository.save(regulation);
        return toRegulationResponse(regulation);
    }

    // 2. Get All Regulations
    public ResultPaginationDTO getAllRegulations(Specification<Regulation> specification, Pageable pageable) {
        Page<Regulation> regulations = regulationRepository.findAll(specification, pageable);
        Page<RegulationResponse> responses = regulations.map(this::toRegulationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get Regulation by Id
    public RegulationResponse getRegulationById(String id) {
        Regulation regulation = regulationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Điều không tồn tại"));
        return toRegulationResponse(regulation);
    }

    // 4. Update Regulation
    @Transactional
    public RegulationResponse updateRegulation(String id, RegulationRequest request) {
        Regulation regulation = regulationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy điều có ID: " + id));

        if (request.getClauseNumber() != null && !request.getClauseNumber().equals(regulation.getClauseNumber())) {
            if (regulationRepository.existsByClauseNumberAndInnovationDecisionId(request.getClauseNumber(),
                    regulation.getInnovationDecision().getId())) {
                throw new IdInvalidException("Số hiệu điều đã tồn tại trong quyết định này");
            }
            regulation.setClauseNumber(request.getClauseNumber());
        }

        if (request.getTitle() != null) {
            regulation.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            regulation.setContent(request.getContent());
        }

        // Cập nhật Chapter nếu có thay đổi
        if (request.getChapterId() != null) {
            if (request.getChapterId().isEmpty()) {
                regulation.setChapter(null);
            } else {
                Chapter chapter = chapterRepository.findById(request.getChapterId())
                        .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));

                // Kiểm tra Chapter có thuộc InnovationDecision không
                if (!chapter.getInnovationDecision().getId().equals(regulation.getInnovationDecision().getId())) {
                    throw new IdInvalidException("Chương không thuộc quyết định này");
                }
                regulation.setChapter(chapter);
            }
        }

        regulationRepository.save(regulation);
        return toRegulationResponse(regulation);
    }

    // 5. Get Regulations by InnovationDecision
    public ResultPaginationDTO getRegulationsByInnovationDecision(String innovationDecisionId, Pageable pageable) {
        Page<Regulation> regulations = regulationRepository.findByInnovationDecisionId(innovationDecisionId, pageable);
        Page<RegulationResponse> responses = regulations.map(this::toRegulationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 6. Get Regulations by Chapter
    public ResultPaginationDTO getRegulationsByChapter(String chapterId, Pageable pageable) {
        Page<Regulation> regulations = regulationRepository.findByChapterId(chapterId, pageable);
        Page<RegulationResponse> responses = regulations.map(this::toRegulationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 7. Get Regulations not in any Chapter
    public ResultPaginationDTO getRegulationsNotInChapter(Pageable pageable) {
        Page<Regulation> regulations = regulationRepository.findByChapterIdIsNull(pageable);
        Page<RegulationResponse> responses = regulations.map(this::toRegulationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // Convert to Response DTO
    private RegulationResponse toRegulationResponse(Regulation regulation) {
        RegulationResponse response = new RegulationResponse();
        response.setId(regulation.getId());
        response.setClauseNumber(regulation.getClauseNumber());
        response.setTitle(regulation.getTitle());
        response.setContent(regulation.getContent());
        response.setInnovationDecisionId(regulation.getInnovationDecision().getId());
        response.setChapterId(regulation.getChapter() != null ? regulation.getChapter().getId() : null);
        return response;
    }
}
