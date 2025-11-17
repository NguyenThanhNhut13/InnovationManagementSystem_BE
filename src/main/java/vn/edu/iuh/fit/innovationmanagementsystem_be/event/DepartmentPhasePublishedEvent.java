package vn.edu.iuh.fit.innovationmanagementsystem_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DepartmentPhasePublishedEvent extends ApplicationEvent {
    private final String departmentId;
    private final String departmentName;
    private final String roundName;
    private final String actorId;
    private final String actorFullName;

    public DepartmentPhasePublishedEvent(Object source,
            String departmentId,
            String departmentName,
            String roundName,
            String actorId,
            String actorFullName) {
        super(source);
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.roundName = roundName;
        this.actorId = actorId;
        this.actorFullName = actorFullName;
    }
}
