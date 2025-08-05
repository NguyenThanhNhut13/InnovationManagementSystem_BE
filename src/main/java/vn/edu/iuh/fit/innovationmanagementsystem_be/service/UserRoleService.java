package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRoleRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<UserRoleResponseDTO> getAllUserRoles() {
        return userRoleRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserRoleResponseDTO> getUserRoleById(UUID id) {
        return userRoleRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public List<UserRoleResponseDTO> getUserRolesByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UserRoleResponseDTO> getUserRolesByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleId(roleId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserRoleResponseDTO createUserRole(UserRoleRequestDTO requestDTO) {
        // Validate that user and role exist
        Optional<User> user = userRepository.findById(requestDTO.getUserId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Optional<Role> role = roleRepository.findById(requestDTO.getRoleId());
        if (role.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        // Check if user-role combination already exists
        if (userRoleRepository.existsByUserIdAndRoleId(requestDTO.getUserId(), requestDTO.getRoleId())) {
            throw new RuntimeException("User already has this role");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user.get());
        userRole.setRole(role.get());

        UserRole savedUserRole = userRoleRepository.save(userRole);
        return convertToResponseDTO(savedUserRole);
    }

    public void deleteUserRole(UUID id) {
        if (!userRoleRepository.existsById(id)) {
            throw new RuntimeException("User role not found");
        }
        userRoleRepository.deleteById(id);
    }

    public void deleteUserRoleByUserIdAndRoleId(UUID userId, UUID roleId) {
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new RuntimeException("User role not found");
        }
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    private UserRoleResponseDTO convertToResponseDTO(UserRole userRole) {
        return new UserRoleResponseDTO(
                userRole.getId(),
                userRole.getUser().getId(),
                userRole.getUser().getUserName(),
                userRole.getUser().getFullName(),
                userRole.getUser().getEmail(),
                userRole.getRole().getId(),
                userRole.getRole().getRoleName().toString());
    }
}