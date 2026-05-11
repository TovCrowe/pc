package com.pc.pc.controller;

import com.pc.pc.entity.Policy;
import com.pc.pc.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping
    public List<Policy> findAll() {
        return policyService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Policy> findById(@PathVariable Long id) {
        return policyService.findById(id);
    }

    @PostMapping
    public Policy save(@RequestBody Policy policy) {
        return policyService.save(policy);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        policyService.delete(id);
    }
}
