package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "departmentId", source = "id")
    @Mapping(target = "totalUsers", source = "users", qualifiedByName = "countTotalUsers")
    @Mapping(target = "activeUsers", source = "users", qualifiedByName = "countActiveUsers")
    @Mapping(target = "inactiveUsers", source = "users", qualifiedByName = "countInactiveUsers")
    @Mapping(target = "suspendedUsers", source = "users", qualifiedByName = "countSuspendedUsers")
    @Mapping(target = "innovationCount", source = "innovations", qualifiedByName = "countInnovations")
    DepartmentResponse toDepartmentResponse(Department department);

    @Named("countTotalUsers")
    default Long countTotalUsers(List<User> users) {
        return users != null ? (long) users.size() : 0L;
    }

    @Named("countActiveUsers")
    default Long countActiveUsers(List<User> users) {
        if (users == null)
            return 0L;
        return users.stream()
                .filter(user -> user.getStatus() != null &&
                        user.getStatus().name().equalsIgnoreCase("ACTIVE"))
                .count();
    }

    @Named("countInactiveUsers")
    default Long countInactiveUsers(List<User> users) {
        if (users == null)
            return 0L;
        return users.stream()
                .filter(user -> user.getStatus() != null &&
                        user.getStatus().name().equalsIgnoreCase("INACTIVE"))
                .count();
    }

    @Named("countSuspendedUsers")
    default Long countSuspendedUsers(List<User> users) {
        if (users == null)
            return 0L;
        return users.stream()
                .filter(user -> user.getStatus() != null &&
                        user.getStatus().name().equalsIgnoreCase("SUSPENDED"))
                .count();
    }

    @Named("countInnovations")
    default Integer countInnovations(List<?> innovations) {
        return innovations != null ? innovations.size() : 0;
    }
}
