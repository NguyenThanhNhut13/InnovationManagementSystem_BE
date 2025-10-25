package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;

@Mapper(componentModel = "spring", uses = { UserRoleMapper.class })
public interface AuthenticationMapper {

    @Mapping(target = "department", source = "department.departmentName")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStringsWithDefault")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    LoginResponse toLoginResponse(User user);
}
