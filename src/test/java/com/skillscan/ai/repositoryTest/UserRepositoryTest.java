package com.skillscan.ai.repositoryTest;

import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnTrue_whenEmailExists() {

        String email = "wasif@test.com";

        User user = User.builder()
                .name("Wasif")
                .email(email)
                .build();

        userRepository.saveAndFlush(user);

        boolean exists = userRepository.existsByEmail(email);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEmailNotExists() {

        String email = "unknown@test.com";

        boolean exists = userRepository.existsByEmail(email);

        assertThat(exists).isFalse();
    }
}
