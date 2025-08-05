package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRoleRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserRoleService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-roles")
@CrossOrigin(origins = "*")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<List<UserRoleResponseDTO>> getAllUserRoles() {
        List<UserRoleResponseDTO> userRoles = userRoleService.getAllUserRoles();
        return ResponseEntity.ok(userRoles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRoleResponseDTO> getUserRoleById(@PathVariable UUID id) {
        Optional<UserRoleResponseDTO> userRole = userRoleService.getUserRoleById(id);
        return userRole.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRoleResponseDTO>> getUserRolesByUserId(@PathVariable UUID userId) {
        List<UserRoleResponseDTO> userRoles = userRoleService.getUserRolesByUserId(userId);
        return ResponseEntity.ok(userRoles);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserRoleResponseDTO>> getUserRolesByRoleId(@PathVariable UUID roleId) {
        List<UserRoleResponseDTO> userRoles = userRoleService.getUserRolesByRoleId(roleId);
        return ResponseEntity.ok(userRoles);
    }

    @PostMapping
    public ResponseEntity<UserRoleResponseDTO> createUserRole(@RequestBody UserRoleRequestDTO requestDTO) {
        try {
            UserRoleResponseDTO createdUserRole = userRoleService.createUserRole(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUserRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable UUID id) {
        try {
            userRoleService.deleteUserRole(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{userId}/role/{roleId}")
    public ResponseEntity<Void> deleteUserRoleByUserIdAndRoleId(@PathVariable UUID userId, @PathVariable UUID roleId) {
        try {
            userRoleService.deleteUserRoleByUserIdAndRoleId(userId, roleId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}