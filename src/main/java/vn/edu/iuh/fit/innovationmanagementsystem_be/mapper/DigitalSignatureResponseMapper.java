package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DigitalSignature;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DigitalSignatureResponse;

@Mapper(componentModel = "spring")
public interface DigitalSignatureResponseMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "user.personnelId", target = "userPersonnelId")
    @Mapping(source = "innovation.id", target = "innovationId")
    @Mapping(source = "innovation.innovationName", target = "innovationName")
    @Mapping(source = "userSignatureProfile.certificateSerial", target = "certificateSerial")
    @Mapping(source = "userSignatureProfile.certificateIssuer", target = "certificateIssuer")
    DigitalSignatureResponse toResponse(DigitalSignature signature);
}
