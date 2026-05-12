package com.pc.pc.controller;

import com.pc.pc.dto.PolicyRequestDTO;
import com.pc.pc.dto.PolicyResponseDTO;
import com.pc.pc.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping
    public ResponseEntity<List<PolicyResponseDTO>> findAll() {
        return ResponseEntity.ok(policyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> findById(@PathVariable Long id) {
        return policyService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PolicyResponseDTO> save(@RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.status(201).body(policyService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> update(@PathVariable Long id, @RequestBody PolicyRequestDTO dto) {
        return ResponseEntity.ok(policyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
