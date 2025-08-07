package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CouncilRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CouncilService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/councils")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CouncilController {

    private final CouncilService councilService;

    @GetMapping
    public ResponseEntity<List<CouncilResponseDTO>> getAllCouncils() {
        List<CouncilResponseDTO> councils = councilService.getAllCouncils();
        return ResponseEntity.ok(councils);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouncilResponseDTO> getCouncilById(@PathVariable UUID id) {
        CouncilResponseDTO council = councilService.getCouncilById(id);
        return ResponseEntity.ok(council);
    }

    @PostMapping
    public ResponseEntity<CouncilResponseDTO> createCouncil(@Valid @RequestBody CouncilRequestDTO requestDTO) {
        CouncilResponseDTO createdCouncil = councilService.createCouncil(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCouncil);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouncilResponseDTO> updateCouncil(@PathVariable UUID id,
            @Valid @RequestBody CouncilRequestDTO requestDTO) {
        CouncilResponseDTO updatedCouncil = councilService.updateCouncil(id, requestDTO);
        return ResponseEntity.ok(updatedCouncil);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCouncil(@PathVariable UUID id) {
        councilService.deleteCouncil(id);
        return ResponseEntity.noContent().build();
    }
}