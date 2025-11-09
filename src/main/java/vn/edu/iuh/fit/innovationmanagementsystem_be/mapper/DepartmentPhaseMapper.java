package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentPhaseResponse;

@Mapper(componentModel = "spring")
public interface DepartmentPhaseMapper {

    @Mapping(target = "innovationPhaseId", source = "innovationPhase.id")
    @Mapping(target = "innovationPhaseName", source = "innovationPhase.name")
    @Mapping(target = "isDeadline", source = "innovationPhase.isDeadline")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "academicYear", source = "innovationRound.academicYear")
    DepartmentPhaseResponse toDepartmentPhaseResponse(DepartmentPhase departmentPhase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "innovationPhase", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "innovationRound", ignore = true)
    @Mapping(target = "phaseStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DepartmentPhase toDepartmentPhase(DepartmentPhaseRequest departmentPhaseRequest);
}
