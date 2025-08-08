package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, String> {

        // Tìm council member theo council
        @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId")
        List<CouncilMember> findByCouncilId(@Param("councilId") String councilId);

        // Tìm council member theo user
        @Query("SELECT cm FROM CouncilMember cm WHERE cm.user.id = :userId")
        List<CouncilMember> findByUserId(@Param("userId") String userId);

        // Tìm council member theo council và user
        @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id = :councilId AND cm.user.id = :userId")
        Optional<CouncilMember> findByCouncilIdAndUserId(@Param("councilId") String councilId,
                        @Param("userId") String userId);

        // Đếm council member theo council
        @Query("SELECT COUNT(cm) FROM CouncilMember cm WHERE cm.council.id = :councilId")
        long countByCouncilId(@Param("councilId") String councilId);

        // Kiểm tra user đã là member của council chưa
        @Query("SELECT COUNT(cm) > 0 FROM CouncilMember cm WHERE cm.council.id = :councilId AND cm.user.id = :userId")
        boolean existsByCouncilIdAndUserId(@Param("councilId") String councilId,
                        @Param("userId") String userId);

        // Tìm council member theo innovation round (thông qua council)
        @Query("SELECT cm FROM CouncilMember cm WHERE cm.council.id IN (SELECT c.id FROM Council c JOIN c.councilMembers cm2 WHERE cm2.id = :memberId)")
        List<CouncilMember> findByInnovationRoundId(@Param("memberId") String memberId);
}
