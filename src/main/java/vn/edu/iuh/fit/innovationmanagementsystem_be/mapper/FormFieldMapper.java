package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;

@Mapper(componentModel = "spring")
public interface FormFieldMapper {

    FormFieldMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(FormFieldMapper.class);

    @Mapping(target = "type", source = "fieldType")
    CreateTemplateResponse.FieldResponse toCreateTemplateFieldResponse(FormField formField);

    @Mapping(target = "type", source = "fieldType")
    CreateTemplateWithFieldsResponse.FieldResponse toCreateTemplateWithFieldsFieldResponse(FormField formField);
}
