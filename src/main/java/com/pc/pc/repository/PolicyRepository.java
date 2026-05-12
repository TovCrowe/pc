package com.pc.pc.repository;

import com.pc.pc.entity.Client;
import com.pc.pc.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    public Optional<Policy> findByPolicyNumber(String policyNumber);
    public List<Policy> findByClient(Client client);
}
