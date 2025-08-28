package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFormFieldRequest {

    private String id;

    private String label;

    private String fieldKey;

    private FieldTypeEnum fieldType;

    private Boolean isRequired;

    private Integer orderInTemplate;

}
