package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "form_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "label", nullable = false, columnDefinition = "TEXT")
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type")
    private FieldTypeEnum fieldType;

    @Column(name = "is_required", nullable = false)
    private Boolean required = false;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "placeholder", columnDefinition = "TEXT")
    private String placeholder;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_template_id", nullable = false)
    private FormTemplate formTemplate;

    @OneToMany(mappedBy = "formField", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormData> formDataList = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "table_config", columnDefinition = "JSON")
    private JsonNode tableConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "JSON")
    private JsonNode options;

    @Column(name = "is_repeatable")
    private Boolean repeatable = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "children", columnDefinition = "JSON")
    private JsonNode children;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reference_config", columnDefinition = "JSON")
    private JsonNode referenceConfig;

}