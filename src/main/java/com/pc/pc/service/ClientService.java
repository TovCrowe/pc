package com.pc.pc.service;

import com.pc.pc.dto.ClientRequestDTO;
import com.pc.pc.dto.ClientResponseDTO;
import com.pc.pc.entity.AppUser;
import com.pc.pc.entity.Client;
import com.pc.pc.exception.ResourceNotFoundException;
import com.pc.pc.repository.AppUserRepository;
import com.pc.pc.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;

    public List<ClientResponseDTO> findAll() {
        return clientRepository.findByOwner(getCurrentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    public ClientResponseDTO findById(Long id) {
        return clientRepository.findByIdAndOwner(id, getCurrentUser())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    public ClientResponseDTO save(ClientRequestDTO dto) {
        Client client = toEntity(dto);
        client.setOwner(getCurrentUser());
        return toResponse(clientRepository.save(client));
    }

    public ClientResponseDTO update(Long id, ClientRequestDTO dto) {
        AppUser owner = getCurrentUser();
        if (!clientRepository.existsByIdAndOwner(id, owner)) {
            throw new ResourceNotFoundException("Client", id);
        }
        Client client = toEntity(dto);
        client.setId(id);
        client.setOwner(owner);
        return toResponse(clientRepository.save(client));
    }

    public void delete(Long id) {
        if (!clientRepository.existsByIdAndOwner(id, getCurrentUser())) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.deleteById(id);
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Client toEntity(ClientRequestDTO dto) {
        Client client = new Client();
        client.setName(dto.getName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        return client;
    }

    private ClientResponseDTO toResponse(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone()
        );
    }
}
