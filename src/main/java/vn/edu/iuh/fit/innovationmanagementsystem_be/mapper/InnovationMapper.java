package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;

@Mapper(componentModel = "spring")
public interface InnovationMapper {

    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "innovationPhaseId", source = "innovationPhase.id")
    @Mapping(target = "innovationRoundId", source = "innovationRound.id")
    @Mapping(target = "innovationRoundName", source = "innovationRound.name")
    @Mapping(target = "academicYear", source = "innovationRound.academicYear")
    @Mapping(target = "submissionTimeRemainingSeconds", ignore = true)
    InnovationResponse toInnovationResponse(Innovation innovation);
}
