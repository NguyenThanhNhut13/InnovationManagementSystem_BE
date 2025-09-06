package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;

@Mapper(componentModel = "spring", uses = { FormFieldMapper.class })
public interface FormTemplateMapper {

    FormTemplateMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(FormTemplateMapper.class);

    @Mapping(target = "innovationRoundId", source = "innovationRound.id")
    @Mapping(target = "innovationRoundName", source = "innovationRound.name")
    FormTemplateResponse toFormTemplateResponse(FormTemplate formTemplate);
}
