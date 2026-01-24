package com.thales.user_resource_service.mapper;

import com.thales.user_resource_service.dto.PortalUserCreateRequest;
import com.thales.user_resource_service.dto.PortalUserResponse;
import com.thales.user_resource_service.model.PortalUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class PortalUserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    public abstract PortalUser toEntity(PortalUserCreateRequest request);

    public abstract PortalUserResponse toResponse(PortalUser portalUser);

    @Named("encodePassword")
    protected String encodePassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
} 
