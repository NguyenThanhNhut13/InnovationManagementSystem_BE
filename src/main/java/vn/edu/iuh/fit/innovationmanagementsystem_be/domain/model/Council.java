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
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_council_level", nullable = false)
    private ReviewLevelEnum reviewCouncilLevel;

    // Relationships
    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CouncilMember> councilMembers = new ArrayList<>();
}