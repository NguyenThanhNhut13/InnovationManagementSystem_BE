package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;

import java.util.List;

@Repository
public interface CoInnovationRepository extends JpaRepository<CoInnovation, String> {

    List<CoInnovation> findByInnovationId(String innovationId);

    void deleteByInnovationId(String innovationId);
}
