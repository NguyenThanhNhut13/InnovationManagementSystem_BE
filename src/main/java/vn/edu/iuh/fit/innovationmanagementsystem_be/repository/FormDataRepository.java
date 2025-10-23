package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, String> {

        @Query("SELECT fd FROM FormData fd " +
                        "LEFT JOIN FETCH fd.formField ff " +
                        "LEFT JOIN FETCH ff.formTemplate ft " +
                        "LEFT JOIN FETCH fd.innovation i " +
                        "WHERE fd.id = :id")
        Optional<FormData> findByIdWithRelations(@Param("id") String id);

        @Query("SELECT fd FROM FormData fd " +
                        "LEFT JOIN FETCH fd.formField ff " +
                        "LEFT JOIN FETCH ff.formTemplate ft " +
                        "LEFT JOIN FETCH fd.innovation i " +
                        "WHERE fd.innovation.id = :innovationId")
        List<FormData> findByInnovationIdWithRelations(@Param("innovationId") String innovationId);

        @Query("SELECT fd FROM FormData fd " +
                        "LEFT JOIN FETCH fd.formField ff " +
                        "LEFT JOIN FETCH ff.formTemplate ft " +
                        "LEFT JOIN FETCH fd.innovation i " +
                        "WHERE fd.innovation.id = :innovationId AND ff.formTemplate.id = :templateId")
        List<FormData> findByInnovationIdAndTemplateIdWithRelations(@Param("innovationId") String innovationId,
                        @Param("templateId") String templateId);

}