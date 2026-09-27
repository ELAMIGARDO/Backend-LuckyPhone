package com.ventas.luckyphonedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    @Builder.Default
    private String tipoToken = "Bearer";
    private String email;
    private String rol;
}
