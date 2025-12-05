package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {

    List<CouncilMember> findByCouncilId(String councilId);

    List<CouncilMember> findByCouncilIdIn(List<String> councilIds);

    Long countByCouncilIdAndRole(String councilId, CouncilMemberRoleEnum role);

    List<CouncilMember> findByUserId(String userId);

    Optional<CouncilMember> findByCouncilIdAndUserIdAndRole(String councilId, String userId,
            CouncilMemberRoleEnum role);

    boolean existsByCouncilIdAndUserIdAndRole(String councilId, String userId, CouncilMemberRoleEnum role);
}
