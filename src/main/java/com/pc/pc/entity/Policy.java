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
@EqualsAndHashCode(callSuper = false)
public class Policy extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vin;
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String policyNumber;
    private Double premium;
    private Date startDate;
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}
