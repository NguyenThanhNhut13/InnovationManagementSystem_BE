package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

        boolean existsByPersonnelId(String personnelId);

        boolean existsByEmail(String email);

        Optional<User> findByPersonnelId(String personnelId);

        Optional<User> findByEmail(String email);

}