package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CoInnovationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CoInnovationResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CoInnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoInnovationService {

    @Autowired
    private CoInnovationRepository coInnovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InnovationRepository innovationRepository;

    public List<CoInnovationResponseDTO> getAllCoInnovations() {
        return coInnovationRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<CoInnovationResponseDTO> getCoInnovationById(UUID id) {
        return coInnovationRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public List<CoInnovationResponseDTO> getCoInnovationsByInnovationId(UUID innovationId) {
        return coInnovationRepository.findByInnovationId(innovationId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CoInnovationResponseDTO> getCoInnovationsByUserId(UUID userId) {
        return coInnovationRepository.findByUserId(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CoInnovationResponseDTO createCoInnovation(CoInnovationRequestDTO requestDTO) {
        // Validate that user exists
        Optional<User> user = userRepository.findById(requestDTO.getUserId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Validate that innovation exists
        Optional<Innovation> innovation = innovationRepository.findById(requestDTO.getInnovationId());
        if (innovation.isEmpty()) {
            throw new RuntimeException("Innovation not found");
        }

        // Check if co-innovation already exists
        if (coInnovationRepository.existsByInnovationIdAndUserId(requestDTO.getInnovationId(),
                requestDTO.getUserId())) {
            throw new RuntimeException("Co-innovation already exists for this user and innovation");
        }

        CoInnovation coInnovation = new CoInnovation();
        coInnovation.setUser(user.get());
        coInnovation.setInnovation(innovation.get());

        CoInnovation savedCoInnovation = coInnovationRepository.save(coInnovation);
        return convertToResponseDTO(savedCoInnovation);
    }

    public void deleteCoInnovation(UUID id) {
        if (!coInnovationRepository.existsById(id)) {
            throw new RuntimeException("Co-innovation not found");
        }
        coInnovationRepository.deleteById(id);
    }

    public void deleteCoInnovationByInnovationAndUser(UUID innovationId, UUID userId) {
        coInnovationRepository.deleteByInnovationIdAndUserId(innovationId, userId);
    }

    private CoInnovationResponseDTO convertToResponseDTO(CoInnovation coInnovation) {
        CoInnovationResponseDTO responseDTO = new CoInnovationResponseDTO();
        responseDTO.setId(coInnovation.getId());
        responseDTO.setUserId(coInnovation.getUser().getId());
        responseDTO.setUserName(coInnovation.getUser().getUserName());
        responseDTO.setFullName(coInnovation.getUser().getFullName());
        responseDTO.setEmail(coInnovation.getUser().getEmail());
        responseDTO.setInnovationId(coInnovation.getInnovation().getId());
        responseDTO.setInnovationName(coInnovation.getInnovation().getInnovationName());
        return responseDTO;
    }
}