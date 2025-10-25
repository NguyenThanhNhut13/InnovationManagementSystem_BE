package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;

import java.util.List;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, String> {

        List<FormTemplate> findByInnovationRoundIdOrderByTemplateType(String innovationRoundId);

        List<FormTemplate> findByInnovationRoundIdAndTargetRoleOrderByTemplateType(String innovationRoundId,
                        TargetRoleCode targetRole);

        // Method để lấy templates từ template library (innovation_round_id = null)
        @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound IS NULL AND ft.targetRole = :targetRole ORDER BY ft.templateType")
        List<FormTemplate> findByInnovationRoundIsNullAndTargetRoleOrderByTemplateType(
                        @Param("targetRole") TargetRoleCode targetRole);

        // Method để lấy tất cả templates từ template library (innovation_round_id =
        // null)
        @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound IS NULL ORDER BY ft.templateType")
        List<FormTemplate> findByInnovationRoundIsNullOrderByTemplateType();

        Page<FormTemplate> findAll(Specification<FormTemplate> specification,
                        Pageable pageable);

        // Method để lấy templates theo innovation round và template types
        @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound.id = :innovationRoundId AND ft.templateType IN :templateTypes ORDER BY ft.templateType")
        List<FormTemplate> findByInnovationRoundIdAndTemplateTypeIn(
                        @Param("innovationRoundId") String innovationRoundId,
                        @Param("templateTypes") List<TemplateTypeEnum> templateTypes);

        // Method để lấy templates từ template library theo template types
        @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound IS NULL AND ft.templateType IN :templateTypes ORDER BY ft.templateType")
        List<FormTemplate> findByInnovationRoundIsNullAndTemplateTypeIn(
                        @Param("templateTypes") List<TemplateTypeEnum> templateTypes);
}