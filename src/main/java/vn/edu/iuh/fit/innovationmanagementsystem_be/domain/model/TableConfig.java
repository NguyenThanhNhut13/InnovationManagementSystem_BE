package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "table_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "min_rows")
    private Integer minRows = 1;

    @Column(name = "max_rows")
    private Integer maxRows = 100;

    @Column(name = "allow_add_rows")
    private Boolean allowAddRows = true;

    @Column(name = "allow_delete_rows")
    private Boolean allowDeleteRows = true;

    @OneToMany(mappedBy = "tableConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TableColumn> columns = new ArrayList<>();

    @OneToOne(mappedBy = "tableConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FormField formField;
}
