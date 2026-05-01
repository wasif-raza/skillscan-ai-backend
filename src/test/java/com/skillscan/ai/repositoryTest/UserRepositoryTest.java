package com.skillscan.ai.repositoryTest;

import com.skillscan.ai.model.User;
import com.skillscan.ai.model.enums.UserRole;
import com.skillscan.ai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnTrue_whenEmailExists() {

        User user = User.builder()
                .firstName("Wasif")
                .lastName("Raza")
                .email("wasif@test.com")
                .password("test-password")
                .role(UserRole.USER)
                .build();

        userRepository.saveAndFlush(user);

        boolean exists = userRepository.existsByEmail("wasif@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEmailNotExists() {

        boolean exists = userRepository.existsByEmail("unknown@test.com");

        assertThat(exists).isFalse();
    }
}