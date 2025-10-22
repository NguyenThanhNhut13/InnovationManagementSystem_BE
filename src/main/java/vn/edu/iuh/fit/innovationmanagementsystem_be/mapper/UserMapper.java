package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStrings")
    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "personnelId", source = "personnelId")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "qualification", source = "qualification")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "password", ignore = true) // Will be set separately in service
    @Mapping(target = "department", ignore = true) // Will be set separately in service
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "innovations", ignore = true)
    @Mapping(target = "coInnovations", ignore = true)
    @Mapping(target = "councilMembers", ignore = true)
    @Mapping(target = "digitalSignatures", ignore = true)
    @Mapping(target = "userSignatureProfiles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toUser(UserRequest userRequest);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    UserRoleResponse toUserRoleResponse(UserRole userRole);

    @Named("mapUserRolesToStrings")
    default List<String> mapUserRolesToStrings(List<UserRole> userRoles) {
        if (userRoles == null) {
            return Collections.emptyList();
        }
        return userRoles.stream()
                .map(userRole -> userRole.getRole() != null && userRole.getRole().getRoleName() != null
                        ? userRole.getRole().getRoleName().name()
                        : null)
                .collect(Collectors.toList());
    }
}
