package vn.edu.iuh.fit.innovationmanagementsystem_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InnovationRoundPublishedEvent extends ApplicationEvent {
    private final String roundId;
    private final String roundName;

    public InnovationRoundPublishedEvent(Object source, String roundId, String roundName) {
        super(source);
        this.roundId = roundId;
        this.roundName = roundName;
    }
}
