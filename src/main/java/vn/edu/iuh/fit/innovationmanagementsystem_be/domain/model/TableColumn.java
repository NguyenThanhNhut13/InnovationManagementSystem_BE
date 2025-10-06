package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

@Entity
@Table(name = "table_columns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "column_key", nullable = false)
    private String key;

    @Column(name = "label", nullable = false, columnDefinition = "TEXT")
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "column_type")
    private FieldTypeEnum type;

    @Column(name = "is_required")
    private Boolean required = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_config_id", nullable = false)
    private TableConfig tableConfig;
}
