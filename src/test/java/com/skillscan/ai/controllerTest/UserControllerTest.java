package com.skillscan.ai.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillscan.ai.controller.UserController;
import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SkillScanAIMetrics skillScanAIMetrics;

    //  GET ALL USERS
    @Test
    void shouldReturnAllUsers() throws Exception {
        UserResponseDTO user1 = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@mail.com")
                .createdAt(LocalDateTime.now())
                .build();

        UserResponseDTO user2 = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@mail.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("alice@mail.com"))
                .andExpect(jsonPath("$[1].email").value("bob@mail.com"));
    }

    @Test
    void shouldReturnEmptyList_whenNoUsersExist() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    //  GET USER BY ID
    @Test
    void shouldReturnUserById() throws Exception {
        UUID id = UUID.randomUUID();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(id)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@mail.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(id)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    //  CREATE USER
    @Test
    void shouldCreateUser() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setFirstName("Alice");
        request.setEmail("alice@mail.com");

        UserResponseDTO response = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .email("alice@mail.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void shouldReturn400_whenFirstNameIsBlank() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setFirstName("");               // violates @NotBlank
        request.setEmail("alice@mail.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setFirstName("Alice");
        request.setEmail("not-a-valid-email");  // violates @Email

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //  DELETE USER
    @Test
    void shouldDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }
}