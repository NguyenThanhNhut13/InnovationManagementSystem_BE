package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouncilService {

    private final CouncilRepository councilRepository;

    public List<CouncilResponseDTO> getAllCouncils() {
        return councilRepository.findAll().stream()
                .map(CouncilResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CouncilResponseDTO getCouncilById(UUID id) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Council not found with id: " + id));
        return new CouncilResponseDTO(council);
    }

    public CouncilResponseDTO createCouncil(CouncilRequestDTO requestDTO) {
        Council council = new Council();
        council.setName(requestDTO.getName());
        council.setReviewCouncilLevel(requestDTO.getReviewCouncilLevel());

        Council savedCouncil = councilRepository.save(council);
        return new CouncilResponseDTO(savedCouncil);
    }

    public CouncilResponseDTO updateCouncil(UUID id, CouncilRequestDTO requestDTO) {
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Council not found with id: " + id));

        council.setName(requestDTO.getName());
        council.setReviewCouncilLevel(requestDTO.getReviewCouncilLevel());

        Council updatedCouncil = councilRepository.save(council);
        return new CouncilResponseDTO(updatedCouncil);
    }

    public void deleteCouncil(UUID id) {
        if (!councilRepository.existsById(id)) {
            throw new RuntimeException("Council not found with id: " + id);
        }
        councilRepository.deleteById(id);
    }
}