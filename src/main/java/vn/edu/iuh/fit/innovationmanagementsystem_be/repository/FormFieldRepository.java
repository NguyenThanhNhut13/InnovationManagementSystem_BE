package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, String> {

        // Tìm field theo tên
        List<FormField> findByFieldNameContaining(String fieldName);

        // Đếm field theo form template
        @Query("SELECT COUNT(ff) FROM FormField ff WHERE ff.formTemplate.id = :templateId")
        long countByFormTemplateId(@Param("templateId") String templateId);

}
