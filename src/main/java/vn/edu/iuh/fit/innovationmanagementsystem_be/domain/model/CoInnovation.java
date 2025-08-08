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
    @Column(name = "id")
    private String id;

    @Column(name = "coInnovator_full_name", nullable = false)
    private String coInnovatorFullName;

    @Column(name = "coInnovator_department_name", nullable = false)
    private String coInnovatorDepartmentName;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;

}