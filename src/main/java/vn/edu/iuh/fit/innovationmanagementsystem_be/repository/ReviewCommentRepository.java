package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewComment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, String> {

        // Tìm review comment theo innovation
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.innovation.id = :innovationId")
        List<ReviewComment> findByInnovationId(@Param("innovationId") String innovationId);

        // Tìm review comment theo council member
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.councilMember.id = :memberId")
        List<ReviewComment> findByCouncilMemberId(@Param("memberId") String memberId);

        // Tìm review comment theo level
        List<ReviewComment> findByReviewsLevel(ReviewLevelEnum level);

        // Tìm review comment theo innovation và council member
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.innovation.id = :innovationId AND rc.councilMember.id = :memberId")
        List<ReviewComment> findByInnovationIdAndCouncilMemberId(@Param("innovationId") String innovationId,
                        @Param("memberId") String memberId);

        // Tìm review comment theo thời gian tạo
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.createdAt BETWEEN :startDate AND :endDate")
        List<ReviewComment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Đếm review comment theo innovation
        @Query("SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.innovation.id = :innovationId")
        long countByInnovationId(@Param("innovationId") String innovationId);

        // Đếm review comment theo council member
        @Query("SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.councilMember.id = :memberId")
        long countByCouncilMemberId(@Param("memberId") String memberId);

        // Tìm review comment theo council (thông qua council member)
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.councilMember.council.id = :councilId")
        List<ReviewComment> findByCouncilId(@Param("councilId") String councilId);

        // Tìm review comment theo level và innovation
        @Query("SELECT rc FROM ReviewComment rc WHERE rc.reviewsLevel = :level AND rc.innovation.id = :innovationId")
        List<ReviewComment> findByReviewsLevelAndInnovationId(@Param("level") ReviewLevelEnum level,
                        @Param("innovationId") String innovationId);
}
