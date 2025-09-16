package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.util.List;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, String> {

        List<FormTemplate> findByInnovationRoundIdOrderByName(String innovationRoundId);

        Page<FormTemplate> findAll(Specification<FormTemplate> specification,
                        Pageable pageable);
}