package com.pc.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {
    private String name;
    private String lastName;
    private String email;
    private String phone;
}
