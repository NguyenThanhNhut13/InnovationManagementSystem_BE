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
    @Mapping(target = "formFieldKey", ignore = true)
    @Mapping(target = "fieldType", ignore = true)
    @Mapping(target = "isRequired", ignore = true)
    @Mapping(target = "orderInTemplate", ignore = true)
    @Mapping(target = "innovationId", ignore = true)
    @Mapping(target = "innovationName", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "formDataList", ignore = true)
    @Mapping(target = "formFields", ignore = true)
    FormDataResponse toFormDataResponse(FormData formData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fieldValue", source = "fieldValue")
    @Mapping(target = "innovation", ignore = true) // Will be set separately in service
    @Mapping(target = "formField", ignore = true) // Will be set separately in service
    FormData toFormData(FormDataRequest formDataRequest);
}
