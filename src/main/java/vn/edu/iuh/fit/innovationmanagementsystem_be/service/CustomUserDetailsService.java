package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;

        /**
         * Lấy role chính (primary role) của user
         */
        private UserRoleEnum getPrimaryRole(String userId) {
                return userRoleRepository.findPrimaryRoleByUserId(userId)
                                .stream()
                                .findFirst()
                                .map(ur -> ur.getRole().getRoleName())
                                .orElseThrow(() -> new UsernameNotFoundException("User không có role nào"));
        }

        @Override
        public UserDetails loadUserByUsername(String emailOrPersonnelId) throws UsernameNotFoundException {
                return userRepository.findByEmailOrPersonnelId(emailOrPersonnelId)
                                .map(user -> {
                                        UserRoleEnum primaryRole = getPrimaryRole(user.getId());
                                        return new User(
                                                        user.getEmail(), // Sử dụng email làm username cho Spring
                                                                         // Security
                                                        user.getPassword(),
                                                        Collections.singletonList(new SimpleGrantedAuthority(
                                                                        "ROLE_" + primaryRole.name())));
                                })
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email or personnel ID: " + emailOrPersonnelId));
        }

        // Method để load user bằng userId cho JWT
        public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
                return userRepository.findById(userId)
                                .map(user -> {
                                        UserRoleEnum primaryRole = getPrimaryRole(user.getId());
                                        return new User(
                                                        user.getEmail(),
                                                        user.getPassword(),
                                                        Collections.singletonList(new SimpleGrantedAuthority(
                                                                        "ROLE_" + primaryRole.name())));
                                })
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        }
}
