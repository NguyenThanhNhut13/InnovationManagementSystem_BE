package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.AttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateAttachmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AttachmentResponse;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "innovationId", source = "innovation.id")
    @Mapping(target = "innovationName", source = "innovation.innovationName")
    AttachmentResponse toAttachmentResponse(Attachment attachment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "innovation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Attachment toAttachment(AttachmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "innovation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateAttachmentFromRequest(UpdateAttachmentRequest request, @MappingTarget Attachment attachment);
}
