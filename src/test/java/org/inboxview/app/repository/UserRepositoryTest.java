package org.inboxview.app.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;

@DataJpaTest
@Testcontainers
public class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @Test
    public void test() {
        var user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@inboxview.com");
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setGuid(UUID.randomUUID().toString());
        user.setDateAdded(OffsetDateTime.now());

        userRepository.saveAndFlush(user);

        assertThat(user.getId()).isGreaterThan(0L);
    }
}
