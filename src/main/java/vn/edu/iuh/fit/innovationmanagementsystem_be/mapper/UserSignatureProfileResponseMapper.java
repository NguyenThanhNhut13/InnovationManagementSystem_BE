package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserSignatureProfileResponse;

@Mapper(componentModel = "spring")
public interface UserSignatureProfileResponseMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userPersonnelId", source = "user.personnelId")
    UserSignatureProfileResponse toUserSignatureProfileResponse(UserSignatureProfile userSignatureProfile);
}
