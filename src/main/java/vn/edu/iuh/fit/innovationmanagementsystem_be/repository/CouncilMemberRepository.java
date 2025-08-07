package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;

import java.util.List;
import java.util.UUID;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, UUID> {
    List<CouncilMember> findByCouncilId(UUID councilId);

    List<CouncilMember> findByUserId(UUID userId);
}