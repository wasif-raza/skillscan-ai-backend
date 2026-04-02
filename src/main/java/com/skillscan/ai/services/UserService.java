package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO dto);

    UserResponseDTO getUserById(UUID id);

    List<UserResponseDTO> getAllUsers();

    void deleteUser(UUID id);
}
