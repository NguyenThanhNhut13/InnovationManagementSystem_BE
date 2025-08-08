package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;

import java.util.List;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, String> {

        // Tìm form data theo innovation
        @Query("SELECT fd FROM FormData fd WHERE fd.innovation.id = :innovationId")
        List<FormData> findByInnovationId(@Param("innovationId") String innovationId);

        // Tìm form data theo form template (thông qua form field)
        @Query("SELECT fd FROM FormData fd WHERE fd.formField.formTemplate.id = :templateId")
        List<FormData> findByFormTemplateId(@Param("templateId") String templateId);
}
