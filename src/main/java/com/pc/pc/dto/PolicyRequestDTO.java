package com.pc.pc.dto;

import com.pc.pc.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequestDTO {
    @NotBlank
    private String type;
    @NotNull
    private Status status;
    @NotBlank
    private String policyNumber;
    @NotNull
    private Double premium;
    @NotNull
    private Date startDate;
    @NotNull
    private Date endDate;
    @NotNull
    private Long clientId;
}
