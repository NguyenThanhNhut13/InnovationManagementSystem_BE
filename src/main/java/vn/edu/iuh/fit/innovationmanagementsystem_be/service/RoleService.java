package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RoleRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RoleResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<RoleResponseDTO> getRoleById(UUID id) {
        return roleRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Optional<RoleResponseDTO> getRoleByName(UserRoleEnum roleName) {
        return roleRepository.findByRoleName(roleName)
                .map(this::convertToResponseDTO);
    }

    public RoleResponseDTO createRole(RoleRequestDTO requestDTO) {
        // Validate unique constraint
        if (roleRepository.existsByRoleName(requestDTO.getRoleName())) {
            throw new RuntimeException("Role name already exists");
        }

        Role role = new Role();
        role.setRoleName(requestDTO.getRoleName());

        Role savedRole = roleRepository.save(role);
        return convertToResponseDTO(savedRole);
    }

    public RoleResponseDTO updateRole(UUID id, RoleRequestDTO requestDTO) {
        Optional<Role> existingRole = roleRepository.findById(id);
        if (existingRole.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        Role role = existingRole.get();

        // Check if new role name is unique (if changed)
        if (!role.getRoleName().equals(requestDTO.getRoleName()) &&
                roleRepository.existsByRoleName(requestDTO.getRoleName())) {
            throw new RuntimeException("Role name already exists");
        }

        role.setRoleName(requestDTO.getRoleName());

        Role savedRole = roleRepository.save(role);
        return convertToResponseDTO(savedRole);
    }

    public void deleteRole(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found");
        }
        roleRepository.deleteById(id);
    }

    private RoleResponseDTO convertToResponseDTO(Role role) {
        return new RoleResponseDTO(
                role.getId(),
                role.getRoleName());
    }
}