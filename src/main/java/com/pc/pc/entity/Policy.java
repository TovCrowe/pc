package com.pc.pc.entity;

import com.pc.pc.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "policies")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private Status status;
    private String policyNumber;
    private Double premium;
    private Date startDate;
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}
