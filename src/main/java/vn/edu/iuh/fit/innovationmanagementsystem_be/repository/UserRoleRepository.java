package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    // Kiểm tra UserRole có tồn tại không
    boolean existsByUserIdAndRoleId(String userId, String roleId);

    // Xóa UserRole theo userId và roleId
    void deleteByUserIdAndRoleId(String userId, String roleId);

    // Tìm UserRole theo roleId
    Page<UserRole> findByRoleId(String roleId, Pageable pageable);

}