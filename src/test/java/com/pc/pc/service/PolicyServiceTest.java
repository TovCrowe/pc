package com.pc.pc.service;

import com.pc.pc.dto.PolicyRequestDTO;
import com.pc.pc.dto.PolicyResponseDTO;
import com.pc.pc.entity.Client;
import com.pc.pc.entity.Policy;
import com.pc.pc.enums.Status;
import com.pc.pc.exception.ResourceNotFoundException;
import com.pc.pc.repository.ClientRepository;
import com.pc.pc.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private PolicyService policyService;

    private Client client;
    private Policy policy;
    private PolicyRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        client = new Client(1L, "John", "Doe", "john@example.com", "555-1234", null);

        Date start = new Date();
        Date end = new Date();

        requestDTO = new PolicyRequestDTO(
                "Toyota", "Camry", 2022,
                "1HGBH41JXMN109186", "ABC-123",
                Status.ACTIVE, "POL-001", 1200.00,
                start, end, 1L
        );

        policy = new Policy(
                1L, "Toyota", "Camry", 2022,
                "1HGBH41JXMN109186", "ABC-123",
                Status.ACTIVE, "POL-001", 1200.00,
                start, end, client
        );
    }

    @Test
    void findAll_returnsMappedList() {
        when(policyRepository.findAll()).thenReturn(List.of(policy));

        List<PolicyResponseDTO> result = policyService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPolicyNumber()).isEqualTo("POL-001");
        assertThat(result.get(0).getClientName()).isEqualTo("John");
    }

    @Test
    void findById_returnsResponse_whenFound() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        PolicyResponseDTO result = policyService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVehicleMake()).isEqualTo("Toyota");
        assertThat(result.getClientId()).isEqualTo(1L);
    }

    @Test
    void findById_throwsException_whenNotFound() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Policy");
    }

    @Test
    void save_persistsAndReturnsResponse() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        PolicyResponseDTO result = policyService.save(requestDTO);

        assertThat(result.getPolicyNumber()).isEqualTo("POL-001");
        assertThat(result.getClientId()).isEqualTo(1L);
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void save_throwsException_whenClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.save(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");
    }

    @Test
    void update_setsIdAndSaves() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        PolicyResponseDTO result = policyService.update(1L, requestDTO);

        assertThat(result.getId()).isEqualTo(1L);
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void delete_callsDeleteById() {
        policyService.delete(1L);

        verify(policyRepository).deleteById(1L);
    }
}