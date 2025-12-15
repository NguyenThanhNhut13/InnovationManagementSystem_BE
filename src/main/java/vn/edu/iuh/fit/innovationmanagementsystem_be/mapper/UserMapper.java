package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;

@Mapper(componentModel = "spring", uses = { UserRoleMapper.class })
public interface UserMapper {

    @Mapping(target = "department", source = "department.departmentName")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStrings")
    @Mapping(target = "isChairman", ignore = true)
    @Mapping(target = "isSecretary", ignore = true)
    UserResponse toUserResponse(User user);

    @AfterMapping
    default void setCouncilRoles(@MappingTarget UserResponse response, User user) {
        if (user.getCouncilMembers() != null) {
            response.setIsChairman(user.getCouncilMembers().stream()
                    .anyMatch(cm -> cm.getRole() == CouncilMemberRoleEnum.CHU_TICH));
            response.setIsSecretary(user.getCouncilMembers().stream()
                    .anyMatch(cm -> cm.getRole() == CouncilMemberRoleEnum.THU_KY));
        } else {
            response.setIsChairman(false);
            response.setIsSecretary(false);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "personnelId", source = "personnelId")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "qualification", source = "qualification")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "department", ignore = true)
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
}
