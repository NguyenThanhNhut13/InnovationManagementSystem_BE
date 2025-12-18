package vn.edu.iuh.fit.innovationmanagementsystem_be.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.InnovationSubmittedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AiService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisEventListener {

    private final AiService aiService;

    @Async
    @EventListener
    public void handleInnovationSubmitted(InnovationSubmittedEvent event) {
        log.info("Pre-computing AI analysis cho innovation: {} (ID: {})",
                event.getInnovationName(), event.getInnovationId());
        try {
            aiService.preComputeAnalysis(event.getInnovationId());
            log.info("Pre-compute AI analysis thành công cho innovation: {}", event.getInnovationId());
        } catch (Exception e) {
            log.error("Lỗi khi pre-compute AI analysis cho innovation: {} - {}",
                    event.getInnovationId(), e.getMessage());
        }
    }
}
