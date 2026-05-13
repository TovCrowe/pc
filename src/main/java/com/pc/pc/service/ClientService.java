package com.pc.pc.service;

import com.pc.pc.dto.ClientRequestDTO;
import com.pc.pc.dto.ClientResponseDTO;
import com.pc.pc.entity.Client;
import com.pc.pc.exception.ResourceNotFoundException;
import com.pc.pc.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public List<ClientResponseDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ClientResponseDTO findById(Long id) {
        return clientRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    public ClientResponseDTO save(ClientRequestDTO dto) {
        return toResponse(clientRepository.save(toEntity(dto)));
    }

    public ClientResponseDTO update(Long id, ClientRequestDTO dto) {
        Client client = toEntity(dto);
        client.setId(id);
        return toResponse(clientRepository.save(client));
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
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
