package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationRoundService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/innovation-rounds")
public class InnovationRoundController {

    private final InnovationRoundService innovationRoundService;

    public InnovationRoundController(InnovationRoundService innovationRoundService) {
        this.innovationRoundService = innovationRoundService;
    }

    // 1. Create Innovation Round
    @PostMapping
    @ApiMessage("Tạo đợt sáng kiến thành công")
    public ResponseEntity<InnovationRoundResponse> createInnovationRound(
            @Valid @RequestBody InnovationRoundRequest request) {
        InnovationRoundResponse response = innovationRoundService.createInnovationRound(request);
        return ResponseEntity.ok(response);
    }

    // 2. Get All InnovationRounds with pagination and filter
    @GetMapping
    @ApiMessage("Lấy danh sách đợt sáng kiến thành công")
    public ResponseEntity<ResultPaginationDTO> getAllInnovationRounds(
            @Filter Specification<InnovationRound> specification,
            Pageable pageable) {
        ResultPaginationDTO response = innovationRoundService.getAllInnovationRounds(specification, pageable);
        return ResponseEntity.ok(response);
    }

    // 3. Get InnovationRound by Id
    @GetMapping("/{id}")
    @ApiMessage("Lấy đợt sáng kiến theo ID thành công")
    public ResponseEntity<InnovationRoundResponse> getInnovationRoundById(@PathVariable String id) {
        InnovationRoundResponse response = innovationRoundService.getInnovationRoundById(id);
        return ResponseEntity.ok(response);
    }

    // 4. Update InnovationRound
    @PostMapping("/{id}")
    @ApiMessage("Cập nhật đợt sáng kiến thành công")
    public ResponseEntity<InnovationRoundResponse> updateInnovationRound(@PathVariable String id,
            @Valid @RequestBody InnovationRoundRequest request) {
        InnovationRoundResponse response = innovationRoundService.updateInnovationRound(id, request);
        return ResponseEntity.ok(response);
    }
}
