package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserDepartmentResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "departmentId", source = "id")
    @Mapping(target = "totalUsers", expression = "java(department.getUsers() != null ? (long) department.getUsers().size() : 0L)")
    @Mapping(target = "activeUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"ACTIVE\")).count() : 0L)")
    @Mapping(target = "inactiveUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"INACTIVE\")).count() : 0L)")
    @Mapping(target = "suspendedUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"SUSPENDED\")).count() : 0L)")
    @Mapping(target = "innovationCount", expression = "java(department.getInnovations() != null ? department.getInnovations().size() : 0)")
    DepartmentResponse toDepartmentResponse(Department department);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentName", source = "departmentName")
    @Mapping(target = "departmentCode", source = "departmentCode")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "innovations", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletionReason", ignore = true)
    Department toDepartment(DepartmentRequest departmentRequest);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapUserRolesToStrings")
    UserDepartmentResponse toUserDepartmentResponse(User user);

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
