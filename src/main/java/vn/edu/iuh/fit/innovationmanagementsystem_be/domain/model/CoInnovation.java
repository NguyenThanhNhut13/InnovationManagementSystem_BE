package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "co_innovation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoInnovation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "coInnovator_full_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String coInnovatorFullName;

    @Column(name = "coInnovator_department_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String coInnovatorDepartmentName;

    @Column(name = "contact_info", nullable = false, columnDefinition = "VARCHAR(500)")
    private String contactInfo;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;
}