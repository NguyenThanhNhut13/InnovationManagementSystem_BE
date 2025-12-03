package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;

@Mapper(componentModel = "spring")
public interface InnovationDecisionMapper {

    InnovationDecisionResponse toInnovationDecisionResponse(InnovationDecision innovationDecision);

}
