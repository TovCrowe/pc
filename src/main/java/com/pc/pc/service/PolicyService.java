package com.pc.pc.service;

import com.pc.pc.dto.PolicyRequestDTO;
import com.pc.pc.dto.PolicyResponseDTO;
import com.pc.pc.entity.Client;
import com.pc.pc.entity.Policy;
import com.pc.pc.repository.ClientRepository;
import com.pc.pc.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;

    public List<PolicyResponseDTO> findAll() {
        return policyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<PolicyResponseDTO> findById(Long id) {
        return policyRepository.findById(id).map(this::toResponse);
    }

    public PolicyResponseDTO save(PolicyRequestDTO dto) {
        return toResponse(policyRepository.save(toEntity(dto)));
    }

    public PolicyResponseDTO update(Long id, PolicyRequestDTO dto) {
        Policy policy = toEntity(dto);
        policy.setId(id);
        return toResponse(policyRepository.save(policy));
    }

    public void delete(Long id) {
        policyRepository.deleteById(id);
    }

    private Policy toEntity(PolicyRequestDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found: " + dto.getClientId()));

        Policy policy = new Policy();
        policy.setType(dto.getType());
        policy.setStatus(dto.getStatus());
        policy.setPolicyNumber(dto.getPolicyNumber());
        policy.setPremium(dto.getPremium());
        policy.setStartDate(dto.getStartDate());
        policy.setEndDate(dto.getEndDate());
        policy.setClient(client);
        return policy;
    }

    private PolicyResponseDTO toResponse(Policy policy) {
        return new PolicyResponseDTO(
                policy.getId(),
                policy.getType(),
                policy.getStatus(),
                policy.getPolicyNumber(),
                policy.getPremium(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getClient().getId(),
                policy.getClient().getName()
        );
    }
}
