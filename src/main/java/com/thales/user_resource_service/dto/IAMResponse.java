package com.thales.user_resource_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IAMResponse {

    private Long id;

    private String username;

    private String email;
}
