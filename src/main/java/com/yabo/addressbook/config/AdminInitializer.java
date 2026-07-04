package com.yabo.addressbook.config;

import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository, ContactGroupRepository contactGroupRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactGroupRepository = contactGroupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setNickname("管理员");
            userRepository.save(admin);
            log.info("Admin account created: {}", adminUsername);

            // 为管理员创建默认分组
            ContactGroup defaultGroup = new ContactGroup();
            defaultGroup.setName("默认分组");
            defaultGroup.setUser(admin);
            defaultGroup.setIsDefault(true);
            defaultGroup.setSortOrder(0);
            contactGroupRepository.save(defaultGroup);
            log.info("Default group created for admin");
        }
    }
}