package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;

@Mapper(componentModel = "spring")
public interface FormDataMapper {

    @Mapping(target = "formFieldId", source = "formField.id")
    @Mapping(target = "formFieldLabel", source = "formField.label")
    @Mapping(target = "formFieldKey", source = "formField.fieldKey")
    @Mapping(target = "fieldType", source = "formField.fieldType")
    @Mapping(target = "required", source = "formField.required")
    @Mapping(target = "placeholder", source = "formField.placeholder")
    @Mapping(target = "templateId", source = "formField.formTemplate.id")
    FormDataResponse toFormDataResponse(FormData formData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fieldValue", source = "fieldValue")
    @Mapping(target = "innovation", ignore = true) // Will be set separately in service
    @Mapping(target = "formField", ignore = true) // Will be set separately in service
    FormData toFormData(FormDataRequest formDataRequest);
}
