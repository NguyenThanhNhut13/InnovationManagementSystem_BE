package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    @Named("mapUserRolesToStrings")
    default List<String> mapUserRolesToStrings(List<UserRole> userRoles) {
        if (userRoles == null) {
            return Collections.emptyList();
        }
        return userRoles.stream()
                .map(userRole -> userRole.getRole() != null && userRole.getRole().getRoleName() != null
                        ? userRole.getRole().getRoleName().name()
                        : null)
                .filter(roleName -> roleName != null)
                .collect(Collectors.toList());
    }

    @Named("mapUserRolesToStringsWithDefault")
    default List<String> mapUserRolesToStringsWithDefault(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.singletonList(UserRoleEnum.GIANG_VIEN.name());
        }
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleName().name())
                .collect(Collectors.toList());
    }
}
