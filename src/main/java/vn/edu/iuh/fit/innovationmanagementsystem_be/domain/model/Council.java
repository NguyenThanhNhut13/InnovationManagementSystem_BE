package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "councils")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Council extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_council_level", nullable = false, columnDefinition = "VARCHAR(50)")
    private ReviewLevelEnum reviewCouncilLevel;

    // Thêm relationship với InnovationDecision
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_decision_id")
    private InnovationDecision innovationDecision;

    // Relationships
    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CouncilMember> councilMembers = new ArrayList<>();
}