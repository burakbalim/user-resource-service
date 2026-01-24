package com.thales.user_resource_service.controller;

import com.thales.user_resource_service.dto.IAMResponse;
import com.thales.user_resource_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class UserPrivateController {

    private final UserService userService;

    @GetMapping("/iam")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<IAMResponse> getIamResponse() {
        return ResponseEntity.ok(userService.getIAMResponse());
    }
}
