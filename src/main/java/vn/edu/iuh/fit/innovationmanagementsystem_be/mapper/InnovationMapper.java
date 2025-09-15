package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;

@Mapper(componentModel = "spring")
public interface InnovationMapper {

    InnovationMapper INSTANCE = Mappers.getMapper(InnovationMapper.class);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "departmentCode", source = "department.departmentCode")
    @Mapping(target = "innovationPhaseId", source = "innovationPhase.id")
    @Mapping(target = "innovationDecisionId", source = "innovationPhase.innovationDecision.id")
    InnovationResponse toInnovationResponse(Innovation innovation);
}
