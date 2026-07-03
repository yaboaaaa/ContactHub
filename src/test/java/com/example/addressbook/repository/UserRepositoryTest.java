package com.example.addressbook.repository;

import com.example.addressbook.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("testuser@example.com");
        user.setRole("USER");
        user.setEnabled(true);
        savedUser = entityManager.persistAndFlush(user);
    }

    @Test
    void findByUsername_shouldReturnUser() {
        Optional<User> result = userRepository.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("testuser@example.com");
        assertThat(result.get().getRole()).isEqualTo("USER");
        assertThat(result.get().getEnabled()).isTrue();
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenExists() {
        boolean exists = userRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void findById_shouldReturnSavedUser() {
        Optional<User> result = userRepository.findById(savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByRoleNot_shouldExcludeSpecifiedRole() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("adminPass");
        adminUser.setRole("ADMIN");
        adminUser.setEnabled(true);
        entityManager.persistAndFlush(adminUser);

        var users = userRepository.findByRoleNot("ADMIN");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser");
    }
}