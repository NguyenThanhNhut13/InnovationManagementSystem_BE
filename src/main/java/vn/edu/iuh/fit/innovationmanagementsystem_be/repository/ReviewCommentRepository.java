package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewComment;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, String> {

    @Query("SELECT rc FROM ReviewComment rc " +
            "LEFT JOIN FETCH rc.councilMember cm " +
            "LEFT JOIN FETCH cm.user " +
            "LEFT JOIN FETCH cm.council " +
            "WHERE rc.innovation.id = :innovationId " +
            "ORDER BY rc.createdAt DESC")
    List<ReviewComment> findByInnovationId(@Param("innovationId") String innovationId);
}
