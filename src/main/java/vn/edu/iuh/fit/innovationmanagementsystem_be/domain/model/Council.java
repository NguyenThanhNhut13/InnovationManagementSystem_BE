package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "councils")
@Data
@EqualsAndHashCode(callSuper = false)
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

    // Relationships
    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouncilMember> councilMembers = new ArrayList<>();

}