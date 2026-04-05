package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.mapper.UserMapper;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.ResumeService;
import com.skillscan.ai.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;

    // Create User
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

    // Get User by ID
    @Override
    public UserResponseDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return UserMapper.toDTO(user);
    }

    // Get All Users
    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    //  DELETE USER (App-level cleanup)
    @Override
    @Transactional
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        //  Fetch all resumes
        List<Resume> resumes = resumeRepository.findByUserId(id);

        //  Delete files from disk
        for (Resume resume : resumes) {
            try {
                resumeService.deleteResumeFile(resume.getFilePath());
            } catch (Exception ex) {
                log.error("Failed to delete resume file: {}", resume.getFilePath());
            }
        }

        // Delete resume records
        resumeRepository.deleteAll(resumes);

        // Delete user
        userRepository.delete(user);
    }
}