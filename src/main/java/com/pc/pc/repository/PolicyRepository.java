package com.pc.pc.repository;

import com.pc.pc.entity.AppUser;
import com.pc.pc.entity.Client;
import com.pc.pc.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByClient(Client client);
    List<Policy> findByClientOwner(AppUser owner);
    Optional<Policy> findByIdAndClientOwner(Long id, AppUser owner);
    boolean existsByIdAndClientOwner(Long id, AppUser owner);
}
