package vn.edu.iuh.fit.innovationmanagementsystem_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InnovationSubmittedEvent extends ApplicationEvent {
    private final String innovationId;
    private final String innovationName;

    public InnovationSubmittedEvent(Object source, String innovationId, String innovationName) {
        super(source);
        this.innovationId = innovationId;
        this.innovationName = innovationName;
    }
}
