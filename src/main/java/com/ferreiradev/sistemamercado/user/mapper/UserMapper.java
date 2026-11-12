package com.ferreiradev.sistemamercado.user.mapper;

import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

   UserResponse toResponse(User user);
}
