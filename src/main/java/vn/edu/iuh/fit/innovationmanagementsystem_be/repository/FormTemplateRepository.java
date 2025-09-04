package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.util.List;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, String> {

        @Query("SELECT COUNT(fd) > 0 FROM FormData fd JOIN fd.formField ff JOIN ff.formTemplate ft WHERE ft.id = :templateId")
        boolean isTemplateUsedByInnovation(@Param("templateId") String templateId);

        List<FormTemplate> findByInnovationRoundIdOrderByName(String innovationRoundId);

        Page<FormTemplate> findAll(Specification<FormTemplate> specification,
                        Pageable pageable);
}