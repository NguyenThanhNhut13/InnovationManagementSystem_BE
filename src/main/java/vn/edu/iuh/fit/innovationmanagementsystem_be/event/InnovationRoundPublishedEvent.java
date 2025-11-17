package vn.edu.iuh.fit.innovationmanagementsystem_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InnovationRoundPublishedEvent extends ApplicationEvent {
    private final String roundId;
    private final String roundName;
    private final String actorId;
    private final String actorFullName;

    public InnovationRoundPublishedEvent(Object source, String roundId, String roundName, String actorId,
            String actorFullName) {
        super(source);
        this.roundId = roundId;
        this.roundName = roundName;
        this.actorId = actorId;
        this.actorFullName = actorFullName;
    }
}
