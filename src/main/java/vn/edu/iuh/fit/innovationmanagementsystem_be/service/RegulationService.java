package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.RegulationRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.ImportMultipleRegulationsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.ImportRegulationsToMultipleChaptersRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.RegulationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.ImportMultipleRegulationsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.ImportRegulationsToMultipleChaptersResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RegulationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ChapterRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegulationService {

    private final RegulationRepository regulationRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    public RegulationService(RegulationRepository regulationRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            ChapterRepository chapterRepository,
            ObjectMapper objectMapper) {
        this.regulationRepository = regulationRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.chapterRepository = chapterRepository;
        this.objectMapper = objectMapper;
    }

    // 1. Create Regulation
    @Transactional
    public RegulationResponse createRegulation(RegulationRequest request) {
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));
        Chapter chapter = null;
        if (request.getChapterId() != null && !request.getChapterId().isEmpty()) {
            chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));
            if (!chapter.getInnovationDecision().getId().equals(request.getInnovationDecisionId())) {
                throw new IdInvalidException("Chương không thuộc quyết định này");
            }
        }

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

        // Update Chapter if changed
        if (request.getChapterId() != null) {
            if (request.getChapterId().isEmpty()) {
                regulation.setChapter(null);
            } else {
                Chapter chapter = chapterRepository.findById(request.getChapterId())
                        .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));

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

    // 8. Import Multiple Regulations to Chapter
    @Transactional
    public ImportMultipleRegulationsResponse importMultipleRegulationsToChapter(
            ImportMultipleRegulationsRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IdInvalidException("Chương không tồn tại"));
        if (request.getRegulations() == null || request.getRegulations().isEmpty()) {
            throw new IdInvalidException("Danh sách điều khoản không được để trống");
        }
        List<String> clauseNumbers = request.getRegulations().stream()
                .map(ImportMultipleRegulationsRequest.RegulationData::getClauseNumber)
                .collect(Collectors.toList());

        if (clauseNumbers.size() != clauseNumbers.stream().distinct().count()) {
            throw new IdInvalidException("Danh sách điều khoản có số hiệu trùng lặp");
        }
        String innovationDecisionId = chapter.getInnovationDecision().getId();
        for (ImportMultipleRegulationsRequest.RegulationData regulationData : request.getRegulations()) {
            if (regulationRepository.existsByClauseNumberAndInnovationDecisionId(
                    regulationData.getClauseNumber(), innovationDecisionId)) {
                throw new IdInvalidException("Số hiệu điều khoản '" + regulationData.getClauseNumber() +
                        "' đã tồn tại trong quyết định này");
            }
        }

        List<Regulation> regulations = request.getRegulations().stream()
                .map(regulationData -> {
                    Regulation regulation = new Regulation();
                    regulation.setClauseNumber(regulationData.getClauseNumber());
                    regulation.setTitle(regulationData.getTitle());

                    Object content = regulationData.getContent();
                    if (content instanceof JsonNode) {
                        regulation.setContent((JsonNode) content);
                    } else if (content != null) {
                        // Nếu content là Object khác, convert thành JsonNode
                        regulation.setContent(objectMapper.valueToTree(content));
                    } else {
                        regulation.setContent(null);
                    }

                    regulation.setInnovationDecision(chapter.getInnovationDecision());
                    regulation.setChapter(chapter);
                    return regulation;
                })
                .collect(Collectors.toList());

        List<Regulation> savedRegulations = regulationRepository.saveAll(regulations);

        List<RegulationResponse> regulationResponses = savedRegulations.stream()
                .map(this::toRegulationResponse)
                .collect(Collectors.toList());

        return new ImportMultipleRegulationsResponse(
                request.getChapterId(),
                chapter.getTitle(),
                regulationResponses);
    }

    // 9. Import Regulations to Multiple Chapters
    @Transactional
    public ImportRegulationsToMultipleChaptersResponse importRegulationsToMultipleChapters(
            ImportRegulationsToMultipleChaptersRequest request) {

        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));

        if (request.getChapterRegulations() == null || request.getChapterRegulations().isEmpty()) {
            throw new IdInvalidException("Danh sách chương và điều khoản không được để trống");
        }

        List<String> chapterIds = request.getChapterRegulations().stream()
                .map(ImportRegulationsToMultipleChaptersRequest.ChapterRegulations::getChapterId)
                .collect(Collectors.toList());

        if (chapterIds.size() != chapterIds.stream().distinct().count()) {
            throw new IdInvalidException("Danh sách chương có ID trùng lặp");
        }

        List<String> allClauseNumbers = new ArrayList<>();
        for (ImportRegulationsToMultipleChaptersRequest.ChapterRegulations chapterReg : request
                .getChapterRegulations()) {
            if (chapterReg.getRegulations() == null || chapterReg.getRegulations().isEmpty()) {
                throw new IdInvalidException("Chương " + chapterReg.getChapterId() + " không có điều khoản nào");
            }

            List<String> chapterClauseNumbers = chapterReg.getRegulations().stream()
                    .map(ImportRegulationsToMultipleChaptersRequest.RegulationData::getClauseNumber)
                    .collect(Collectors.toList());

            if (chapterClauseNumbers.size() != chapterClauseNumbers.stream().distinct().count()) {
                throw new IdInvalidException(
                        "Chương " + chapterReg.getChapterId() + " có số hiệu điều khoản trùng lặp");
            }

            allClauseNumbers.addAll(chapterClauseNumbers);
        }

        if (allClauseNumbers.size() != allClauseNumbers.stream().distinct().count()) {
            throw new IdInvalidException("Có số hiệu điều khoản trùng lặp giữa các chương");
        }

        for (String clauseNumber : allClauseNumbers) {
            if (regulationRepository.existsByClauseNumberAndInnovationDecisionId(clauseNumber,
                    request.getInnovationDecisionId())) {
                throw new IdInvalidException(
                        "Số điều khoản '" + clauseNumber + "' đã tồn tại trong quyết định này");
            }
        }

        // Xử lý import từng chương
        List<ImportRegulationsToMultipleChaptersResponse.ChapterImportResult> chapterResults = new ArrayList<>();

        for (ImportRegulationsToMultipleChaptersRequest.ChapterRegulations chapterReg : request
                .getChapterRegulations()) {

            Chapter chapter = chapterRepository.findById(chapterReg.getChapterId())
                    .orElseThrow(
                            () -> new IdInvalidException("Chương " + chapterReg.getChapterId() + " không tồn tại"));

            if (!chapter.getInnovationDecision().getId().equals(request.getInnovationDecisionId())) {
                throw new IdInvalidException("Chương " + chapterReg.getChapterId() + " không thuộc quyết định này");
            }

            // Tạo danh sách điều khoản cho chương này
            List<Regulation> regulations = chapterReg.getRegulations().stream()
                    .map(regulationData -> {
                        Regulation regulation = new Regulation();
                        regulation.setClauseNumber(regulationData.getClauseNumber());
                        regulation.setTitle(regulationData.getTitle());

                        // Xử lý content an toàn - convert thành JsonNode
                        Object content = regulationData.getContent();
                        if (content instanceof JsonNode) {
                            regulation.setContent((JsonNode) content);
                        } else if (content != null) {
                            // Nếu content là Object khác, convert thành JsonNode
                            regulation.setContent(objectMapper.valueToTree(content));
                        } else {
                            regulation.setContent(null);
                        }

                        regulation.setInnovationDecision(innovationDecision);
                        regulation.setChapter(chapter);
                        return regulation;
                    })
                    .collect(Collectors.toList());

            List<Regulation> savedRegulations = regulationRepository.saveAll(regulations);

            // Chuyển đổi sang response
            List<RegulationResponse> regulationResponses = savedRegulations.stream()
                    .map(this::toRegulationResponse)
                    .collect(Collectors.toList());

            // Tạo kết quả cho chương này
            ImportRegulationsToMultipleChaptersResponse.ChapterImportResult chapterResult = new ImportRegulationsToMultipleChaptersResponse.ChapterImportResult(
                    chapterReg.getChapterId(),
                    chapter.getTitle(),
                    regulationResponses);

            chapterResults.add(chapterResult);
        }

        return new ImportRegulationsToMultipleChaptersResponse(
                request.getInnovationDecisionId(),
                chapterResults);
    }

    // Mapper
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
