package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.DepartmentRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.DepartmentResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<DepartmentResponseDTO> getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Optional<DepartmentResponseDTO> getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode)
                .map(this::convertToResponseDTO);
    }

    public Optional<DepartmentResponseDTO> getDepartmentByName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName)
                .map(this::convertToResponseDTO);
    }

    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO) {
        // Validate unique constraints
        if (departmentRepository.existsByDepartmentCode(requestDTO.getDepartmentCode())) {
            throw new RuntimeException("Department code already exists");
        }
        if (departmentRepository.existsByDepartmentName(requestDTO.getDepartmentName())) {
            throw new RuntimeException("Department name already exists");
        }

        Department department = new Department();
        department.setDepartmentName(requestDTO.getDepartmentName());
        department.setDepartmentCode(requestDTO.getDepartmentCode());

        Department savedDepartment = departmentRepository.save(department);
        return convertToResponseDTO(savedDepartment);
    }

    public DepartmentResponseDTO updateDepartment(UUID id, DepartmentRequestDTO requestDTO) {
        Optional<Department> existingDepartment = departmentRepository.findById(id);
        if (existingDepartment.isEmpty()) {
            throw new RuntimeException("Department not found");
        }

        Department department = existingDepartment.get();

        // Check if new department code is unique (if changed)
        if (!department.getDepartmentCode().equals(requestDTO.getDepartmentCode()) &&
                departmentRepository.existsByDepartmentCode(requestDTO.getDepartmentCode())) {
            throw new RuntimeException("Department code already exists");
        }

        // Check if new department name is unique (if changed)
        if (!department.getDepartmentName().equals(requestDTO.getDepartmentName()) &&
                departmentRepository.existsByDepartmentName(requestDTO.getDepartmentName())) {
            throw new RuntimeException("Department name already exists");
        }

        department.setDepartmentName(requestDTO.getDepartmentName());
        department.setDepartmentCode(requestDTO.getDepartmentCode());

        Department savedDepartment = departmentRepository.save(department);
        return convertToResponseDTO(savedDepartment);
    }

    public void deleteDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponseDTO convertToResponseDTO(Department department) {
        DepartmentResponseDTO responseDTO = new DepartmentResponseDTO();
        responseDTO.setId(department.getId());
        responseDTO.setDepartmentName(department.getDepartmentName());
        responseDTO.setDepartmentCode(department.getDepartmentCode());
        return responseDTO;
    }
}