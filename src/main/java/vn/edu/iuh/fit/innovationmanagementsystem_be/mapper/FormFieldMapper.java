package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;

@Mapper(componentModel = "spring")
public interface FormFieldMapper {

    FormFieldMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(FormFieldMapper.class);

    @Mapping(target = "formTemplateId", source = "formTemplate.id")
    @Mapping(target = "tableConfig", source = "tableConfig")
    @Mapping(target = "options", source = "options")
    @Mapping(target = "repeatable", source = "repeatable")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "referenceConfig", source = "referenceConfig")
    @Mapping(target = "userDataConfig", source = "userDataConfig")
    @Mapping(target = "signingRole", source = "signingRole")
    FormFieldResponse toFormFieldResponse(FormField formField);

}
