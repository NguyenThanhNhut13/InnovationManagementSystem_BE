package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ReferenceConfig;

@Mapper(componentModel = "spring")
public interface FormFieldMapper {

    FormFieldMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(FormFieldMapper.class);

    @Mapping(target = "formTemplateId", source = "formTemplate.id")
    @Mapping(target = "tableConfig", source = "tableConfig")
    @Mapping(target = "options", source = "options")
    @Mapping(target = "repeatable", source = "repeatable")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "referenceConfig", source = "referenceConfig", qualifiedByName = "jsonNodeToReferenceConfig")
    FormFieldResponse toFormFieldResponse(FormField formField);

    @org.mapstruct.Named("jsonNodeToReferenceConfig")
    default ReferenceConfig jsonNodeToReferenceConfig(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.treeToValue(jsonNode, ReferenceConfig.class);
        } catch (Exception e) {
            return null;
        }
    }
}
