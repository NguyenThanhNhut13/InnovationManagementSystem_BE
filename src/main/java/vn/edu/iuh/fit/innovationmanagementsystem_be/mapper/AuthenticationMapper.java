package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStrings")
    @Mapping(target = "accessToken", ignore = true) // Will be set separately
    @Mapping(target = "refreshToken", ignore = true) // Will be set separately
    LoginResponse toLoginResponse(User user);

    @Named("mapUserRolesToStrings")
    default List<String> mapUserRolesToStrings(
            List<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole> userRoles) {
        if (userRoles == null) {
            return Collections.singletonList(UserRoleEnum.GIANG_VIEN.name());
        }
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleName().name())
                .collect(Collectors.toList());
    }
}
