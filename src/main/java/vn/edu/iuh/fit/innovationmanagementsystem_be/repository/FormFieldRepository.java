package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, String> {

    @Query("SELECT ff FROM FormField ff " +
            "LEFT JOIN FETCH ff.formTemplate ft " +
            "WHERE ff.id = :id")
    Optional<FormField> findByIdWithTemplate(@Param("id") String id);

    @Query("SELECT ff FROM FormField ff " +
            "LEFT JOIN FETCH ff.formTemplate ft " +
            "WHERE ff.fieldKey = :fieldKey AND ft.id = :templateId")
    Optional<FormField> findByFieldKeyAndTemplateId(@Param("fieldKey") String fieldKey,
            @Param("templateId") String templateId);

    @Query("SELECT ff FROM FormField ff " +
            "LEFT JOIN FETCH ff.formTemplate ft " +
            "WHERE ft.id = :templateId")
    List<FormField> findByTemplateId(@Param("templateId") String templateId);

}