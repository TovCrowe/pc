package com.pc.pc.service;

import com.pc.pc.dto.ClientRequestDTO;
import com.pc.pc.dto.ClientResponseDTO;
import com.pc.pc.entity.AppUser;
import com.pc.pc.entity.Client;
import com.pc.pc.exception.ResourceNotFoundException;
import com.pc.pc.repository.AppUserRepository;
import com.pc.pc.repository.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ClientService clientService;

    private AppUser owner;
    private Client client;
    private ClientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        owner = new AppUser(1L, "admin", "hashed", "ADMIN");
        client = new Client(1L, "Jane", "Smith", "jane@example.com", "555-9999", owner, null);
        requestDTO = new ClientRequestDTO("Jane", "Smith", "jane@example.com", "555-9999");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(owner));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAll_returnsMappedList() {
        when(clientRepository.findByOwner(owner)).thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane");
        assertThat(result.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findById_returnsResponse_whenFound() {
        when(clientRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        when(clientRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");
    }

    @Test
    void save_persistsAndReturnsResponse() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponseDTO result = clientService.save(requestDTO);

        assertThat(result.getName()).isEqualTo("Jane");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_setsIdAndSaves() {
        when(clientRepository.existsByIdAndOwner(1L, owner)).thenReturn(true);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponseDTO result = clientService.update(1L, requestDTO);

        assertThat(result.getId()).isEqualTo(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_throwsException_whenNotFound() {
        when(clientRepository.existsByIdAndOwner(99L, owner)).thenReturn(false);

        assertThatThrownBy(() -> clientService.update(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");
    }

    @Test
    void delete_callsDeleteById() {
        when(clientRepository.existsByIdAndOwner(1L, owner)).thenReturn(true);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void delete_throwsException_whenNotFound() {
        when(clientRepository.existsByIdAndOwner(99L, owner)).thenReturn(false);

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");
    }
}
