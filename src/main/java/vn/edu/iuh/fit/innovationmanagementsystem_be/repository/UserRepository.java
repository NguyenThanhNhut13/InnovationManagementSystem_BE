package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
}
