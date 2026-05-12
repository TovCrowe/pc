package com.pc.pc.dto;

import com.pc.pc.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequestDTO {
    private String type;
    private Status status;
    private String policyNumber;
    private Double premium;
    private Date startDate;
    private Date endDate;
    private Long clientId;
}
