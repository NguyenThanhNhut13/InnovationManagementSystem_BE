package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Base64Utils;

@Mapper(componentModel = "spring", uses = { FormFieldMapper.class })
public interface FormTemplateMapper {

    @Mapping(target = "innovationRoundId", source = "innovationRound.id")
    @Mapping(target = "innovationRoundName", source = "innovationRound.name", defaultValue = "")
    FormTemplateResponse toFormTemplateResponse(FormTemplate formTemplate);

    @Mapping(target = "roundId", source = "innovationRound.id")
    @Mapping(target = "fields", source = "formFields")
    CreateTemplateResponse toCreateTemplateResponse(FormTemplate formTemplate);

    @Mapping(target = "roundId", source = "innovationRound.id")
    @Mapping(target = "fields", source = "formFields")
    CreateTemplateWithFieldsResponse toCreateTemplateWithFieldsResponse(FormTemplate formTemplate);

    @AfterMapping
    default void decodeTemplateContent(@MappingTarget FormTemplateResponse response, FormTemplate template) {
        if (response != null && template != null) {
            response.setTemplateContent(Base64Utils.decode(template.getTemplateContent()));
        }
    }

    @AfterMapping
    default void decodeTemplateContent(@MappingTarget CreateTemplateResponse response, FormTemplate template) {
        if (response != null && template != null) {
            response.setTemplateContent(Base64Utils.decode(template.getTemplateContent()));
        }
    }

    @AfterMapping
    default void decodeTemplateContent(@MappingTarget CreateTemplateWithFieldsResponse response,
            FormTemplate template) {
        if (response != null && template != null) {
            response.setTemplateContent(Base64Utils.decode(template.getTemplateContent()));
        }
    }

}
