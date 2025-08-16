package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;

@Repository
public interface InnovationRepository extends JpaRepository<Innovation, String>, JpaSpecificationExecutor<Innovation> {

}
