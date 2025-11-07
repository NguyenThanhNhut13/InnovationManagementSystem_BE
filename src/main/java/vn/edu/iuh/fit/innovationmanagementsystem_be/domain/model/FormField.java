package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "form_fields")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormField extends Auditable {

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

    @Column(name = "is_read_only", nullable = false)
    private Boolean isReadOnly = false;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_data_config", columnDefinition = "JSON")
    private JsonNode userDataConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "innovation_data_config", columnDefinition = "JSON")
    private JsonNode innovationDataConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contribution_config", columnDefinition = "JSON")
    private JsonNode contributionConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "signing_role")
    private UserRoleEnum signingRole;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_template_id", nullable = false)
    private FormTemplate formTemplate;

    @OneToMany(mappedBy = "formField", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormData> formDataList = new ArrayList<>();

}