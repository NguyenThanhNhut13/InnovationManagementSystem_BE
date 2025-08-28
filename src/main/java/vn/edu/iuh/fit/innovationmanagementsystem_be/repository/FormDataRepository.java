package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;

import java.util.List;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, String> {

    List<FormData> findByInnovationId(String innovationId);

    List<FormData> findByInnovationIdAndFormFieldFormTemplateId(String innovationId, String templateId);

    List<FormData> findByFormFieldFormTemplateId(String templateId);

}