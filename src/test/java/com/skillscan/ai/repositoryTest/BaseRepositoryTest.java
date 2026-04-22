package com.skillscan.ai.repositoryTest;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.TimeZone;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

    static{
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    // shared setup if needed
}
