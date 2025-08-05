package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDTO> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Optional<UserResponseDTO> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .map(this::convertToResponseDTO);
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponseDTO);
    }

    public List<UserResponseDTO> getUsersByRole(UserRoleEnum role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getUsersByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        // Validate unique constraints
        if (userRepository.existsByUserName(requestDTO.getUserName())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (requestDTO.getPersonnelId() != null && userRepository.existsByPersonnelId(requestDTO.getPersonnelId())) {
            throw new RuntimeException("Personnel ID already exists");
        }

        User user = new User();
        user.setUserName(requestDTO.getUserName());
        user.setFullName(requestDTO.getFullName());
        user.setEmail(requestDTO.getEmail());
        user.setPhoneNumber(requestDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(requestDTO.getRole());
        user.setPersonnelId(requestDTO.getPersonnelId());

        // Set department if provided
        if (requestDTO.getDepartmentId() != null) {
            Optional<Department> department = departmentRepository.findById(requestDTO.getDepartmentId());
            if (department.isPresent()) {
                user.setDepartment(department.get());
            }
        }

        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = existingUser.get();

        // Check if new username is unique (if changed)
        if (!user.getUserName().equals(requestDTO.getUserName()) &&
                userRepository.existsByUserName(requestDTO.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if new email is unique (if changed)
        if (!user.getEmail().equals(requestDTO.getEmail()) &&
                userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if new personnel ID is unique (if changed)
        if (requestDTO.getPersonnelId() != null &&
                !requestDTO.getPersonnelId().equals(user.getPersonnelId()) &&
                userRepository.existsByPersonnelId(requestDTO.getPersonnelId())) {
            throw new RuntimeException("Personnel ID already exists");
        }

        user.setUserName(requestDTO.getUserName());
        user.setFullName(requestDTO.getFullName());
        user.setEmail(requestDTO.getEmail());
        user.setPhoneNumber(requestDTO.getPhoneNumber());
        user.setRole(requestDTO.getRole());
        user.setPersonnelId(requestDTO.getPersonnelId());

        // Update password only if provided
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }

        // Set department if provided
        if (requestDTO.getDepartmentId() != null) {
            Optional<Department> department = departmentRepository.findById(requestDTO.getDepartmentId());
            if (department.isPresent()) {
                user.setDepartment(department.get());
            }
        }

        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(user.getId());
        responseDTO.setUserName(user.getUserName());
        responseDTO.setFullName(user.getFullName());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setPhoneNumber(user.getPhoneNumber());
        responseDTO.setRole(user.getRole());
        responseDTO.setPersonnelId(user.getPersonnelId());
        responseDTO.setCreatedAt(user.getCreatedAt());
        responseDTO.setUpdatedAt(user.getUpdatedAt());

        if (user.getDepartment() != null) {
            responseDTO.setDepartmentId(user.getDepartment().getId());
            responseDTO.setDepartmentName(user.getDepartment().getDepartmentName());
            responseDTO.setDepartmentCode(user.getDepartment().getDepartmentCode());
        }

        return responseDTO;
    }
}