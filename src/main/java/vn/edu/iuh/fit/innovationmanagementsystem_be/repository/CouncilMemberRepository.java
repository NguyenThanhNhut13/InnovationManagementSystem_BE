package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;

import java.util.List;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {

    List<CouncilMember> findByCouncilId(String councilId);

    Long countByCouncilIdAndRole(String councilId, CouncilMemberRoleEnum role);
}
