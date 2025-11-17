package vn.edu.iuh.fit.innovationmanagementsystem_be.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.DepartmentPhaseClosedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.DepartmentPhasePublishedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.InnovationRoundClosedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.InnovationRoundPublishedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleInnovationRoundPublished(InnovationRoundPublishedEvent event) {
        log.info("Handling InnovationRoundPublishedEvent for round: {}", event.getRoundName());
        try {
            notificationService.notifyRoundPublished(event.getRoundId(), event.getRoundName(), event.getActorId(),
                    event.getActorFullName());
        } catch (Exception e) {
            throw new IdInvalidException("Error handling InnovationRoundPublishedEvent: " + e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleInnovationRoundClosed(InnovationRoundClosedEvent event) {
        log.info("Handling InnovationRoundClosedEvent for round: {}", event.getRoundName());
        try {
            notificationService.notifyRoundClosed(event.getRoundId(), event.getRoundName(), event.getActorId(),
                    event.getActorFullName());
        } catch (Exception e) {
            throw new IdInvalidException("Error handling InnovationRoundClosedEvent: " + e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleDepartmentPhasePublished(DepartmentPhasePublishedEvent event) {
        log.info("Handling DepartmentPhasePublishedEvent for department: ", event.getDepartmentName());
        try {
            notificationService.notifyDepartmentPhasePublished(
                    event.getDepartmentId(),
                    event.getDepartmentName(),
                    event.getRoundName());
        } catch (Exception e) {
            throw new IdInvalidException("Error handling DepartmentPhasePublishedEvent: " + e.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleDepartmentPhaseClosed(DepartmentPhaseClosedEvent event) {
        log.info("Handling DepartmentPhaseClosedEvent for department: ", event.getDepartmentName());
        try {
            notificationService.notifyDepartmentPhaseClosed(
                    event.getDepartmentId(),
                    event.getDepartmentName(),
                    event.getRoundName());
        } catch (Exception e) {
            throw new IdInvalidException("Error handling DepartmentPhaseClosedEvent: " + e.getMessage());
        }
    }
}
