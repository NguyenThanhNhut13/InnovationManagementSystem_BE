package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilMemberResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CouncilMapper {

    @Mapping(target = "councilMembers", source = "councilMembers", qualifiedByName = "mapCouncilMembersToResponse")
    CouncilResponse toCouncilResponse(Council council);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "councilMembers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Council toCouncil(CouncilRequest councilRequest);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userPersonnelId", source = "user.personnelId")
    @Mapping(target = "departmentName", source = "user.department.departmentName")
    CouncilMemberResponse toCouncilMemberResponse(CouncilMember councilMember);

    @Named("mapCouncilMembersToResponse")
    default List<CouncilMemberResponse> mapCouncilMembersToResponse(List<CouncilMember> councilMembers) {
        if (councilMembers == null) {
            return Collections.emptyList();
        }
        return councilMembers.stream()
                .map(this::toCouncilMemberResponse)
                .collect(Collectors.toList());
    }
}
