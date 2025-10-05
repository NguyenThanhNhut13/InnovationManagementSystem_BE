package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId")
    Page<CouncilMember> findByCouncilId(@Param("councilId") String councilId, Pageable pageable);

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId")
    List<CouncilMember> findAllByCouncilId(@Param("councilId") String councilId);

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.user.id = :userId")
    List<CouncilMember> findByUserId(@Param("userId") String userId);

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId AND cm.user.id = :userId")
    Optional<CouncilMember> findByCouncilIdAndUserId(@Param("councilId") String councilId,
            @Param("userId") String userId);

    @Query("SELECT COUNT(cm) FROM CouncilMember cm WHERE cm.council.id = :councilId")
    long countByCouncilId(@Param("councilId") String councilId);

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId AND cm.council.isActive = true")
    List<CouncilMember> findActiveCouncilMembersByCouncilId(@Param("councilId") String councilId);

    @Query("SELECT cm FROM CouncilMember cm WHERE cm.user.id = :userId AND cm.council.isActive = true")
    List<CouncilMember> findActiveCouncilMembersByUserId(@Param("userId") String userId);

    boolean existsByCouncilIdAndUserId(String councilId, String userId);
}
