package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "form_field")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "label", nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type")
    private FieldTypeEnum fieldType;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "field_key")
    private String fieldKey;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_template_id")
    private FormTemplate formTemplate;

    @OneToMany(mappedBy = "formField", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FormData> formDataList;
}