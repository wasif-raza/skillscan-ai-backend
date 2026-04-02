package com.skillscan.ai.mapper;


import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.model.User;

public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}