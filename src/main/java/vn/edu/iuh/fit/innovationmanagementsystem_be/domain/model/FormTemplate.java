package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "form_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_content", columnDefinition = "JSON")
    private String templateContent;

    // Thêm relationship với InnovationDecision
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_decision_id")
    private InnovationDecision innovationDecision;

    // Relationships
    @OneToMany(mappedBy = "formTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormField> formFields = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    private InnovationRound innovationRound;
}