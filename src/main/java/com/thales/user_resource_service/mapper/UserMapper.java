package com.thales.user_resource_service.mapper;

import com.thales.security.model.JwtUserClaims;
import com.thales.user_resource_service.dto.*;
import com.thales.user_resource_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.http.ResponseEntity;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Converts User entity to UserResponse DTO
     */
    UserResponse toUserResponse(User user);

    /**
     * Converts UserCreateRequest DTO to User entity
     * We exclude the password field because it requires special processing
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "username", source = "username")
    User toUser(UserCreateRequest request);

    /**
     * Converts UserCreateOrGetRequest DTO to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "username", source = "username")
    User toUser(UserCreateOrGetRequest request);

    /**
     * Updates an existing User entity with information from UserCreateOrGetRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", source = "username")
    void updateUserFromRequest(UserCreateOrGetRequest request, @MappingTarget User user);

    /**
     * Converts User entity to UserBaseResponse DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    UserBaseResponse toUserBaseResponse(User user);

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    IAMResponse toIAMResponse(JwtUserClaims currentUser);
}
