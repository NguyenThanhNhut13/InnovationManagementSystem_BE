package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;

@Mapper(componentModel = "spring", uses = { TableConfigMapper.class })
public interface FormFieldMapper {

    FormFieldMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(FormFieldMapper.class);

    @Mapping(target = "formTemplateId", source = "formTemplate.id")
    @Mapping(target = "formTemplateName", source = "formTemplate.name")
    FormFieldResponse toFormFieldResponse(FormField formField);
}
