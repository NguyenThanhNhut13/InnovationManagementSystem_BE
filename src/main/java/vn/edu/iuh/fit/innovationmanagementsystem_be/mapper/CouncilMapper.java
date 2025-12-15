package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilListResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = { CouncilMemberMapper.class })
public interface CouncilMapper {

    @Mapping(target = "members", source = "councilMembers")
    @Mapping(target = "innovationCount", source = "innovations", qualifiedByName = "countInnovations")
    @Mapping(target = "departmentName", source = "department", qualifiedByName = "getDepartmentName")
    @Mapping(target = "roundName", source = "innovations", qualifiedByName = "getRoundName")
    @Mapping(target = "scoringProgress", ignore = true)
    @Mapping(target = "canScore", ignore = true)
    @Mapping(target = "canView", ignore = true)
    @Mapping(target = "scoringStartDate", ignore = true)
    @Mapping(target = "scoringEndDate", ignore = true)
    @Mapping(target = "scoringPeriodStatus", ignore = true)
    CouncilResponse toCouncilResponse(Council council);

    @Mapping(target = "memberCount", source = "councilMembers", qualifiedByName = "countMembers")
    @Mapping(target = "innovationCount", source = "innovations", qualifiedByName = "countInnovations")
    @Mapping(target = "departmentName", source = "department", qualifiedByName = "getDepartmentName")
    @Mapping(target = "roundName", source = "innovations", qualifiedByName = "getRoundName")
    CouncilListResponse toCouncilListResponse(Council council);

    @Named("countInnovations")
    default Integer countInnovations(List<Innovation> innovations) {
        return innovations != null ? innovations.size() : 0;
    }

    @Named("getDepartmentName")
    default String getDepartmentName(vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department department) {
        return department != null ? department.getDepartmentName() : null;
    }

    @Named("getRoundName")
    default String getRoundName(List<Innovation> innovations) {
        if (innovations == null || innovations.isEmpty()) {
            return null;
        }
        // Lấy roundName từ innovation đầu tiên
        Innovation firstInnovation = innovations.get(0);
        if (firstInnovation.getInnovationRound() != null) {
            return firstInnovation.getInnovationRound().getName();
        }
        return null;
    }

    @Named("countMembers")
    default Integer countMembers(List<CouncilMember> councilMembers) {
        return councilMembers != null ? councilMembers.size() : 0;
    }
}
