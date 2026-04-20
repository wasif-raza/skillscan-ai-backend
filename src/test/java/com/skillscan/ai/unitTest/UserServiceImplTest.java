package com.skillscan.ai.unitTest;

import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.exception.EmailAlreadyExistsException;
import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.ResumeService;
import com.skillscan.ai.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private UserServiceImpl userService;

    //  CREATE USER SUCCESS
    @Test
    void createUser_shouldSaveUser() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("test@mail.com");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@mail.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals("test@mail.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    //  CREATE USER - DUPLICATE EMAIL
    @Test
    void createUser_shouldThrowException_whenEmailExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("test@mail.com");

        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(dto));
    }

    //  GET USER BY ID
    @Test
    void getUserById_shouldReturnUser() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setEmail("test@mail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserById(id);

        assertEquals("test@mail.com", response.getEmail());
    }

    //  GET USER NOT FOUND
    @Test
    void getUserById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(id));
    }

    //  GET ALL USERS
    @Test
    void getAllUsers_shouldReturnList() {
        User user1 = new User();
        user1.setEmail("a@mail.com");

        User user2 = new User();
        user2.setEmail("b@mail.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    //  DELETE USER SUCCESS
    @Test
    void deleteUser_shouldDeleteUserAndResumes() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Resume resume = new Resume();
        resume.setFilePath("file1.pdf");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserId(userId)).thenReturn(List.of(resume));

        userService.deleteUser(userId);

        verify(resumeService).deleteResumeFile("file1.pdf");
        verify(resumeRepository).deleteAll(anyList());
        verify(userRepository).delete(user);
    }

    //  DELETE USER NOT FOUND
    @Test
    void deleteUser_shouldThrow_whenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(id));
    }

    //  FILE DELETE FAILURE SHOULD NOT BREAK
    @Test
    void deleteUser_shouldContinue_whenFileDeletionFails() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Resume resume = new Resume();
        resume.setFilePath("file1.pdf");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserId(userId)).thenReturn(List.of(resume));

        doThrow(new RuntimeException("fail"))
                .when(resumeService)
                .deleteResumeFile("file1.pdf");

        userService.deleteUser(userId);

        verify(resumeRepository).deleteAll(anyList());
        verify(userRepository).delete(user);
    }
}