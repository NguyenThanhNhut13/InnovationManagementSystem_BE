package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

        boolean existsByPersonnelId(String personnelId);

        boolean existsByEmail(String email);

        Optional<User> findByPersonnelId(String personnelId);

        Optional<User> findByEmail(String email);

        @Query("SELECT u FROM User u WHERE " +
                        "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.personnelId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<User> searchUsersByFullNameOrPersonnelId(@Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT u FROM User u WHERE LOWER(TRIM(u.fullName)) = LOWER(TRIM(:fullName))")
        Optional<User> findByFullNameIgnoreCase(@Param("fullName") String fullName);

}