package com.pc.pc.repository;

import com.pc.pc.entity.AppUser;
import com.pc.pc.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByOwner(AppUser owner);
    Optional<Client> findByIdAndOwner(Long id, AppUser owner);
    boolean existsByIdAndOwner(Long id, AppUser owner);
}
