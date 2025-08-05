package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.PageRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.PageResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.InnovationStatusUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InnovationService {

    private final InnovationRepository innovationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // Create innovation
    public InnovationResponseDTO createInnovation(InnovationRequestDTO requestDTO, UUID userId) {
        Innovation innovation = new Innovation();
        innovation.setInnovationName(requestDTO.getInnovationName());
        innovation.setIsScore(requestDTO.getIsScore());
        innovation
                .setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : Innovation.InnovationStatus.DRAFT);
        innovation.setCreatedBy(userId.toString());
        innovation.setUpdatedBy(userId.toString());

        // Set user if provided
        if (requestDTO.getUserId() != null) {
            userRepository.findById(requestDTO.getUserId()).ifPresent(innovation::setUser);
        } else {
            userRepository.findById(userId).ifPresent(innovation::setUser);
        }

        // Set department if provided
        if (requestDTO.getDepartmentId() != null) {
            departmentRepository.findById(requestDTO.getDepartmentId()).ifPresent(innovation::setDepartment);
        }

        Innovation savedInnovation = innovationRepository.save(innovation);
        return new InnovationResponseDTO(savedInnovation);
    }

    // Get innovation by ID
    public Optional<InnovationResponseDTO> getInnovationById(UUID id) {
        return innovationRepository.findById(id)
                .map(InnovationResponseDTO::new);
    }

    // Get all innovations with pagination
    public PageResponseDTO<InnovationResponseDTO> getAllInnovations(PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findAll(pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get innovations by user ID with pagination
    public PageResponseDTO<InnovationResponseDTO> getInnovationsByUserId(UUID userId, PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findByUserId(userId, pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get innovations by department ID with pagination
    public PageResponseDTO<InnovationResponseDTO> getInnovationsByDepartmentId(UUID departmentId,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findByDepartmentId(departmentId, pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get innovations by status with pagination
    public PageResponseDTO<InnovationResponseDTO> getInnovationsByStatus(Innovation.InnovationStatus status,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findByStatus(status, pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Get innovations by status group with pagination
    public PageResponseDTO<InnovationResponseDTO> getInnovationsByStatusGroup(String statusGroup,
            PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findByStatusGroup(statusGroup, pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Search innovations by name with pagination
    public PageResponseDTO<InnovationResponseDTO> searchInnovationsByName(String keyword, PageRequestDTO pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<Innovation> innovationPage = innovationRepository.findByInnovationNameContainingIgnoreCase(keyword,
                pageable);

        List<InnovationResponseDTO> content = innovationPage.getContent().stream()
                .map(InnovationResponseDTO::new)
                .toList();

        return new PageResponseDTO<>(content, pageRequest.getPage(), pageRequest.getSize(),
                innovationPage.getTotalElements(), pageRequest.getSortBy(), pageRequest.getSortDirection());
    }

    // Update innovation
    public Optional<InnovationResponseDTO> updateInnovation(UUID id, InnovationRequestDTO requestDTO, UUID userId) {
        return innovationRepository.findById(id)
                .map(innovation -> {
                    innovation.setInnovationName(requestDTO.getInnovationName());
                    innovation.setIsScore(requestDTO.getIsScore());
                    if (requestDTO.getStatus() != null) {
                        innovation.setStatus(requestDTO.getStatus());
                    }
                    innovation.setUpdatedBy(userId.toString());
                    innovation.setUpdatedAt(LocalDateTime.now());

                    // Update department if provided
                    if (requestDTO.getDepartmentId() != null) {
                        departmentRepository.findById(requestDTO.getDepartmentId())
                                .ifPresent(innovation::setDepartment);
                    }

                    Innovation savedInnovation = innovationRepository.save(innovation);
                    return new InnovationResponseDTO(savedInnovation);
                });
    }

    // Change innovation status
    public Optional<InnovationResponseDTO> changeInnovationStatus(UUID id, Innovation.InnovationStatus newStatus,
            String userId) {
        return innovationRepository.findById(id)
                .map(innovation -> {
                    Innovation.InnovationStatus currentStatus = innovation.getStatus();

                    if (!InnovationStatusUtils.isValidTransition(currentStatus, newStatus)) {
                        throw new IllegalArgumentException(
                                "Invalid status transition from " + currentStatus + " to " + newStatus);
                    }

                    innovation.setStatus(newStatus);
                    innovation.setUpdatedBy(userId);
                    innovation.setUpdatedAt(LocalDateTime.now());

                    Innovation savedInnovation = innovationRepository.save(innovation);
                    return new InnovationResponseDTO(savedInnovation);
                });
    }

    // Get valid next statuses
    public List<Innovation.InnovationStatus> getValidNextStatuses(UUID id) {
        return innovationRepository.findById(id)
                .map(innovation -> InnovationStatusUtils.getValidNextStatuses(innovation.getStatus()))
                .map(statusSet -> statusSet.stream().toList())
                .orElse(List.of());
    }

    // Delete innovation
    public boolean deleteInnovation(UUID id) {
        if (innovationRepository.existsById(id)) {
            innovationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Count operations
    public long countInnovationsByStatus(Innovation.InnovationStatus status) {
        return innovationRepository.countByStatus(status);
    }

    public long countInnovationsByUserId(UUID userId) {
        return innovationRepository.countByUserId(userId);
    }

    public long countInnovationsByDepartmentId(UUID departmentId) {
        return innovationRepository.countByDepartmentId(departmentId);
    }

    // Helper method to create Pageable
    private Pageable createPageable(PageRequestDTO pageRequest) {
        Sort sort = Sort.by(
                pageRequest.getSortDirection().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                pageRequest.getSortBy());
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }
}