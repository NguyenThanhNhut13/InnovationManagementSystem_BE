package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportMultipleRegulationsResponse {

    private String chapterId;
    private String chapterTitle;
    private List<RegulationResponse> importedRegulations;
    private int totalImported;
    private String message;

    public ImportMultipleRegulationsResponse(String chapterId, String chapterTitle,
            List<RegulationResponse> importedRegulations) {
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.importedRegulations = importedRegulations;
        this.totalImported = importedRegulations.size();
        this.message = "Import thành công " + this.totalImported + " điều khoản vào chương: " + chapterTitle;
    }
}
