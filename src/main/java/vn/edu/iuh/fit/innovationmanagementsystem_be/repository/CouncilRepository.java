package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilRepository extends JpaRepository<Council, String>, JpaSpecificationExecutor<Council> {

        Optional<Council> findByName(String name);

        boolean existsByName(String name);

        @Query("SELECT c FROM Council c WHERE c.name LIKE %:keyword%")
        Page<Council> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT c FROM Council c WHERE c.reviewCouncilLevel = :level")
        Page<Council> findByReviewLevel(@Param("level") ReviewLevelEnum level, Pageable pageable);

        @Query("SELECT c FROM Council c WHERE c.name LIKE %:keyword% AND c.reviewCouncilLevel = :level")
        Page<Council> findByNameContainingAndReviewLevel(@Param("keyword") String keyword,
                        @Param("level") ReviewLevelEnum level,
                        Pageable pageable);

        @Query("SELECT c FROM Council c")
        Page<Council> findActiveCouncils(Pageable pageable);

        @Query("SELECT c FROM Council c WHERE c.reviewCouncilLevel = :level")
        Page<Council> findActiveCouncilsByReviewLevel(@Param("level") ReviewLevelEnum level, Pageable pageable);

        @Query("SELECT c FROM Council c")
        List<Council> findAllActiveCouncils();

        @Query("SELECT c FROM Council c WHERE c.reviewCouncilLevel = :level")
        List<Council> findActiveCouncilsByReviewLevel(@Param("level") ReviewLevelEnum level);
}
