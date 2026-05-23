package com.vermann.ecommerce.mapper;

import com.vermann.ecommerce.dto.request.UserRequest;
import com.vermann.ecommerce.dto.response.UserResponse;
import com.vermann.ecommerce.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(UserRequest request, @MappingTarget User user);
}
