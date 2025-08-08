package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.util.List;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, String> {

        // Tìm template theo tên
        List<FormTemplate> findByTemplateNameContaining(String templateName);

        // Tìm template theo innovation round
        @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound.id = :roundId")
        List<FormTemplate> findByInnovationRoundId(@Param("roundId") String roundId);

        // Đếm template theo innovation round
        @Query("SELECT COUNT(ft) FROM FormTemplate ft WHERE ft.innovationRound.id = :roundId")
        long countByInnovationRoundId(@Param("roundId") String roundId);

}
