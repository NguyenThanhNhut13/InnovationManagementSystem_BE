package vn.edu.iuh.fit.innovationmanagementsystem_be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = { CouncilMemberMapper.class })
public interface CouncilMapper {

    @Mapping(target = "members", source = "councilMembers")
    @Mapping(target = "innovationCount", source = "innovations", qualifiedByName = "countInnovations")
    CouncilResponse toCouncilResponse(Council council);

    @Named("countInnovations")
    default Integer countInnovations(List<Innovation> innovations) {
        return innovations != null ? innovations.size() : 0;
    }
}
