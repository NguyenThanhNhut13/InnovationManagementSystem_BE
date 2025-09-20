package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Repository
public interface InnovationRepository extends JpaRepository<Innovation, String>, JpaSpecificationExecutor<Innovation> {

    Page<Innovation> findByUserId(String userId, Pageable pageable);

    Page<Innovation> findByUserIdAndStatus(String userId, InnovationStatusEnum status, Pageable pageable);
}
