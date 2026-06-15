package com.pc.pc.service;

import com.pc.pc.dto.PolicyRequestDTO;
import com.pc.pc.dto.PolicyResponseDTO;
import com.pc.pc.entity.AppUser;
import com.pc.pc.entity.Client;
import com.pc.pc.entity.Policy;
import com.pc.pc.enums.Status;
import com.pc.pc.exception.ResourceNotFoundException;
import com.pc.pc.repository.AppUserRepository;
import com.pc.pc.repository.ClientRepository;
import com.pc.pc.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;

    public List<PolicyResponseDTO> findAll() {
        return policyRepository.findByClientOwner(getCurrentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    public PolicyResponseDTO findById(Long id) {
        return policyRepository.findByIdAndClientOwner(id, getCurrentUser())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id));
    }

    public PolicyResponseDTO save(PolicyRequestDTO dto) {
        return toResponse(policyRepository.save(toEntity(dto)));
    }

    public PolicyResponseDTO update(Long id, PolicyRequestDTO dto) {
        AppUser owner = getCurrentUser();
        if (!policyRepository.existsByIdAndClientOwner(id, owner)) {
            throw new ResourceNotFoundException("Policy", id);
        }
        Policy policy = toEntity(dto);
        policy.setId(id);
        return toResponse(policyRepository.save(policy));
    }

    public void delete(Long id) {
        if (!policyRepository.existsByIdAndClientOwner(id, getCurrentUser())) {
            throw new ResourceNotFoundException("Policy", id);
        }
        policyRepository.deleteById(id);
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Policy toEntity(PolicyRequestDTO dto) {
        AppUser owner = getCurrentUser();
        Client client = clientRepository.findByIdAndOwner(dto.getClientId(), owner)
                .orElseThrow(() -> new ResourceNotFoundException("Client", dto.getClientId()));

        Policy policy = new Policy();
        policy.setVehicleMake(dto.getVehicleMake());
        policy.setVehicleModel(dto.getVehicleModel());
        policy.setVehicleYear(dto.getVehicleYear());
        policy.setVin(dto.getVin());
        policy.setLicensePlate(dto.getLicensePlate());
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
                policy.getVehicleMake(),
                policy.getVehicleModel(),
                policy.getVehicleYear(),
                policy.getVin(),
                policy.getLicensePlate(),
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
