package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.mapper.UserMapper;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {

       final User user = UserMapper.toEntity(dto);
        try {
           final User savedUser = userRepository.save(user);
            return UserMapper.toDTO(savedUser);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

    }



    @Override
    public UserResponseDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("User not found with id: " + id));
        return UserMapper.toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
