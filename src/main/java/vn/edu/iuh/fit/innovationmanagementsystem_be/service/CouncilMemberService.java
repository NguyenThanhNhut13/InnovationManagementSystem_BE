package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilMemberRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilMemberResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouncilMemberService {

    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilRepository councilRepository;
    private final UserRepository userRepository;

    public List<CouncilMemberResponseDTO> getAllCouncilMembers() {
        return councilMemberRepository.findAll().stream()
                .map(CouncilMemberResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CouncilMemberResponseDTO getCouncilMemberById(UUID id) {
        CouncilMember councilMember = councilMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Council member not found with id: " + id));
        return new CouncilMemberResponseDTO(councilMember);
    }

    public List<CouncilMemberResponseDTO> getCouncilMembersByCouncilId(UUID councilId) {
        return councilMemberRepository.findByCouncilId(councilId).stream()
                .map(CouncilMemberResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<CouncilMemberResponseDTO> getCouncilMembersByUserId(UUID userId) {
        return councilMemberRepository.findByUserId(userId).stream()
                .map(CouncilMemberResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CouncilMemberResponseDTO createCouncilMember(CouncilMemberRequestDTO requestDTO) {
        Council council = councilRepository.findById(requestDTO.getCouncilId())
                .orElseThrow(() -> new RuntimeException("Council not found with id: " + requestDTO.getCouncilId()));

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requestDTO.getUserId()));

        CouncilMember councilMember = new CouncilMember();
        councilMember.setCouncil(council);
        councilMember.setUser(user);

        CouncilMember savedCouncilMember = councilMemberRepository.save(councilMember);
        return new CouncilMemberResponseDTO(savedCouncilMember);
    }

    public CouncilMemberResponseDTO updateCouncilMember(UUID id, CouncilMemberRequestDTO requestDTO) {
        CouncilMember councilMember = councilMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Council member not found with id: " + id));

        Council council = councilRepository.findById(requestDTO.getCouncilId())
                .orElseThrow(() -> new RuntimeException("Council not found with id: " + requestDTO.getCouncilId()));

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requestDTO.getUserId()));

        councilMember.setCouncil(council);
        councilMember.setUser(user);

        CouncilMember updatedCouncilMember = councilMemberRepository.save(councilMember);
        return new CouncilMemberResponseDTO(updatedCouncilMember);
    }

    public void deleteCouncilMember(UUID id) {
        if (!councilMemberRepository.existsById(id)) {
            throw new RuntimeException("Council member not found with id: " + id);
        }
        councilMemberRepository.deleteById(id);
    }
}