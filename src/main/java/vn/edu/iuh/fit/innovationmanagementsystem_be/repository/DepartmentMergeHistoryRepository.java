package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentMergeHistory;

import java.util.List;

@Repository
public interface DepartmentMergeHistoryRepository extends JpaRepository<DepartmentMergeHistory, String> {

    List<DepartmentMergeHistory> findByMergedDepartmentId(String mergedDepartmentId);

    List<DepartmentMergeHistory> findByIsActiveTrue();

    List<DepartmentMergeHistory> findBySourceDepartmentIdsContaining(String sourceDepartmentId);
}