package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "departmentId", source = "id")
    @Mapping(target = "totalUsers", expression = "java(department.getUsers() != null ? (long) department.getUsers().size() : 0L)")
    @Mapping(target = "activeUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"ACTIVE\")).count() : 0L)")
    @Mapping(target = "inactiveUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"INACTIVE\")).count() : 0L)")
    @Mapping(target = "suspendedUsers", expression = "java(department.getUsers() != null ? department.getUsers().stream().filter(user -> user.getStatus() != null && user.getStatus().name().equalsIgnoreCase(\"SUSPENDED\")).count() : 0L)")
    @Mapping(target = "innovationCount", expression = "java(department.getInnovations() != null ? department.getInnovations().size() : 0)")
    DepartmentResponse toDepartmentResponse(Department department);

}
