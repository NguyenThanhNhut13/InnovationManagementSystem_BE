package vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;

public interface AiProvider {
    String generateContent(String prompt);

    AiAnalysisResponse analyze(String innovationId, String innovationName, String content);

    String getProviderName();

    boolean isAvailable();
}
