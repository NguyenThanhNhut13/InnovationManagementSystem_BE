package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilStatusEnum;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private CouncilStatusEnum status = CouncilStatusEnum.CON_HIEU_LUC; // Mặc định còn hiệu lực

    // Relationships
    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouncilMember> councilMembers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "council_innovations", joinColumns = @JoinColumn(name = "council_id"), inverseJoinColumns = @JoinColumn(name = "innovation_id"))
    private List<Innovation> innovations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true) // Nullable vì cấp trường không có department
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    private InnovationRound innovationRound;

}