package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "council_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "VARCHAR(50)")
    private CouncilMemberRoleEnum role;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "councilMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<ReviewScore> reviewScores = new ArrayList<>();

    @OneToMany(mappedBy = "councilMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<ReviewComment> reviewComments = new ArrayList<>();

}