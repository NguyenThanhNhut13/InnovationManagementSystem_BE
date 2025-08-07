package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilMemberRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilMemberResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CouncilMemberService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/council-members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CouncilMemberController {

    private final CouncilMemberService councilMemberService;

    @GetMapping
    public ResponseEntity<List<CouncilMemberResponseDTO>> getAllCouncilMembers() {
        List<CouncilMemberResponseDTO> councilMembers = councilMemberService.getAllCouncilMembers();
        return ResponseEntity.ok(councilMembers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouncilMemberResponseDTO> getCouncilMemberById(@PathVariable UUID id) {
        CouncilMemberResponseDTO councilMember = councilMemberService.getCouncilMemberById(id);
        return ResponseEntity.ok(councilMember);
    }

    @GetMapping("/council/{councilId}")
    public ResponseEntity<List<CouncilMemberResponseDTO>> getCouncilMembersByCouncilId(@PathVariable UUID councilId) {
        List<CouncilMemberResponseDTO> councilMembers = councilMemberService.getCouncilMembersByCouncilId(councilId);
        return ResponseEntity.ok(councilMembers);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CouncilMemberResponseDTO>> getCouncilMembersByUserId(@PathVariable UUID userId) {
        List<CouncilMemberResponseDTO> councilMembers = councilMemberService.getCouncilMembersByUserId(userId);
        return ResponseEntity.ok(councilMembers);
    }

    @PostMapping
    public ResponseEntity<CouncilMemberResponseDTO> createCouncilMember(
            @Valid @RequestBody CouncilMemberRequestDTO requestDTO) {
        CouncilMemberResponseDTO createdCouncilMember = councilMemberService.createCouncilMember(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCouncilMember);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouncilMemberResponseDTO> updateCouncilMember(@PathVariable UUID id,
            @Valid @RequestBody CouncilMemberRequestDTO requestDTO) {
        CouncilMemberResponseDTO updatedCouncilMember = councilMemberService.updateCouncilMember(id, requestDTO);
        return ResponseEntity.ok(updatedCouncilMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCouncilMember(@PathVariable UUID id) {
        councilMemberService.deleteCouncilMember(id);
        return ResponseEntity.noContent().build();
    }
}