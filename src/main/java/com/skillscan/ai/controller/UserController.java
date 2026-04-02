package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.UserRequestDTO;
import com.skillscan.ai.dto.response.UserResponseDTO;
import com.skillscan.ai.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDTO createUser(@Valid  @RequestBody UserRequestDTO dto){
        return userService.createUser(dto);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable UUID id){
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }
}
