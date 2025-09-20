package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentSplitHistory;

import java.util.List;

@Repository
public interface DepartmentSplitHistoryRepository extends JpaRepository<DepartmentSplitHistory, String> {

    List<DepartmentSplitHistory> findBySourceDepartmentId(String sourceDepartmentId);

    List<DepartmentSplitHistory> findByNewDepartmentIdsContaining(String newDepartmentId);

    List<DepartmentSplitHistory> findByIsActiveTrue();
}