package com.pc.pc.dto;

import com.pc.pc.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponseDTO {
    private Long id;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vin;
    private String licensePlate;
    private Status status;
    private String policyNumber;
    private Double premium;
    private Date startDate;
    private Date endDate;
    private Long clientId;
    private String clientName;
    private Instant createdAt;
    private Instant updatedAt;
}
