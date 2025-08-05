package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CoInnovationRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CoInnovationResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CoInnovationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/co-innovations")
@CrossOrigin(origins = "*")
public class CoInnovationController {

    @Autowired
    private CoInnovationService coInnovationService;

    @GetMapping
    public ResponseEntity<List<CoInnovationResponseDTO>> getAllCoInnovations() {
        List<CoInnovationResponseDTO> coInnovations = coInnovationService.getAllCoInnovations();
        return ResponseEntity.ok(coInnovations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoInnovationResponseDTO> getCoInnovationById(@PathVariable UUID id) {
        Optional<CoInnovationResponseDTO> coInnovation = coInnovationService.getCoInnovationById(id);
        return coInnovation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/innovation/{innovationId}")
    public ResponseEntity<List<CoInnovationResponseDTO>> getCoInnovationsByInnovationId(
            @PathVariable UUID innovationId) {
        List<CoInnovationResponseDTO> coInnovations = coInnovationService.getCoInnovationsByInnovationId(innovationId);
        return ResponseEntity.ok(coInnovations);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CoInnovationResponseDTO>> getCoInnovationsByUserId(@PathVariable UUID userId) {
        List<CoInnovationResponseDTO> coInnovations = coInnovationService.getCoInnovationsByUserId(userId);
        return ResponseEntity.ok(coInnovations);
    }

    @PostMapping
    public ResponseEntity<CoInnovationResponseDTO> createCoInnovation(@RequestBody CoInnovationRequestDTO requestDTO) {
        try {
            CoInnovationResponseDTO createdCoInnovation = coInnovationService.createCoInnovation(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCoInnovation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoInnovation(@PathVariable UUID id) {
        try {
            coInnovationService.deleteCoInnovation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/innovation/{innovationId}/user/{userId}")
    public ResponseEntity<Void> deleteCoInnovationByInnovationAndUser(@PathVariable UUID innovationId,
            @PathVariable UUID userId) {
        try {
            coInnovationService.deleteCoInnovationByInnovationAndUser(innovationId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}