// package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import jakarta.transaction.Transactional;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UserRequest;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserResponse;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
// import
// vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

// @Service
// @Transactional
// public class UserService {
// private final UserRepository userRepository;
// private final DepartmentRepository departmentRepository;
// private final PasswordEncoder passwordEncoder;

// public UserService(UserRepository userRepository, DepartmentRepository
// departmentRepository,
// PasswordEncoder passwordEncoder) {
// this.userRepository = userRepository;
// this.departmentRepository = departmentRepository;
// this.passwordEncoder = passwordEncoder;
// }

// // Cretae User
// public UserResponse createUser(UserRequest userRequest) {
// if (userRepository.existsByPersonnelId(userRequest.getPersonnelId())) {
// throw new IdInvalidException("Mã nhân viên đã tồn tại");
// }
// if (userRepository.existsByEmail(userRequest.getEmail())) {
// throw new IdInvalidException("Email đã tồn tại");
// }
// Department department =
// departmentRepository.findById(userRequest.getDepartmentId())
// .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
// User user = new User();
// user.setPersonnelId(userRequest.getPersonnelId());
// user.setFullName(userRequest.getFullName());
// user.setEmail(userRequest.getEmail());
// user.setPhoneNumber(userRequest.getPhoneNumber());
// user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
// user.setDepartment(department);
// user.setCreatedAt(LocalDateTime.now());
// user.setUpdatedAt(LocalDateTime.now());
// userRepository.save(user);
// return toUserResponse(user);

// }

// // Mapper
// private UserResponse toUserResponse(User user) {
// UserResponse userResponse = new UserResponse();
// userResponse.setId(user.getId());
// userResponse.setPersonnelId(user.getPersonnelId());
// userResponse.setFullName(user.getFullName());
// userResponse.setEmail(user.getEmail());
// userResponse.setPhoneNumber(user.getPhoneNumber());
// userResponse.setDepartmentId(user.getDepartment().getId());
// userResponse.setDepartmentName(user.getDepartment().getDepartmentName());
// userResponse.setDepartmentCode(user.getDepartment().getDepartmentCode());
// userResponse.setInnovationCount(user.getInnovations().size());
// userResponse.setCoInnovationCount(user.getCoInnovations().size());
// userResponse.setRoleNames(user.getUserRoles().stream()
// .map(UserRole::getRoleName)
// .collect(Collectors.toList()));
// userResponse.setCreatedAt(user.getCreatedAt());
// userResponse.setUpdatedAt(user.getUpdatedAt());
// return userResponse;
// }

// }
