package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;

@Mapper(componentModel = "spring", uses = { UserRoleMapper.class })
public interface AuthenticationMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStringsWithDefault")
    @Mapping(target = "accessToken", ignore = true) // Will be set separately
    @Mapping(target = "refreshToken", ignore = true) // Will be set separately
    LoginResponse toLoginResponse(User user);
}
