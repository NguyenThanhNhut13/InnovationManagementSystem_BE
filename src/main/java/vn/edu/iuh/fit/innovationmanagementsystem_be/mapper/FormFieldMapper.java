package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;

@Mapper(componentModel = "spring")
public interface FormFieldMapper {

    @Mapping(target = "type", source = "fieldType")
    CreateTemplateResponse.FieldResponse toCreateTemplateFieldResponse(FormField formField);

    @Mapping(target = "type", source = "fieldType")
    CreateTemplateWithFieldsResponse.FieldResponse toCreateTemplateWithFieldsFieldResponse(FormField formField);

    @Mapping(target = "fieldType", source = "fieldType")
    @Mapping(target = "formTemplateId", source = "formTemplate.id")
    FormFieldResponse toFormFieldResponse(FormField formField);
}
