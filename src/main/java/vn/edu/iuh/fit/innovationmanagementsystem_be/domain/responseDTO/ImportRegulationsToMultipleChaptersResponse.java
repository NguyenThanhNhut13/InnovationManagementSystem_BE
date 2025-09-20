package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRegulationsToMultipleChaptersResponse {

    private String innovationDecisionId;
    private List<ChapterImportResult> chapterResults;
    private int totalChapters;
    private int totalRegulations;
    private String message;

    public ImportRegulationsToMultipleChaptersResponse(String innovationDecisionId,
            List<ChapterImportResult> chapterResults) {
        this.innovationDecisionId = innovationDecisionId;
        this.chapterResults = chapterResults;
        this.totalChapters = chapterResults.size();
        this.totalRegulations = chapterResults.stream()
                .mapToInt(result -> result.getImportedRegulations().size())
                .sum();
        this.message = "Import thành công " + this.totalRegulations + " điều khoản vào " +
                this.totalChapters + " chương";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterImportResult {
        private String chapterId;
        private String chapterTitle;
        private List<RegulationResponse> importedRegulations;
        private int totalImported;

        public ChapterImportResult(String chapterId, String chapterTitle,
                List<RegulationResponse> importedRegulations) {
            this.chapterId = chapterId;
            this.chapterTitle = chapterTitle;
            this.importedRegulations = importedRegulations;
            this.totalImported = importedRegulations.size();
        }
    }
}
