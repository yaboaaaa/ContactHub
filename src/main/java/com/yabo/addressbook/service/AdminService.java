package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        ContactGroupRepository contactGroupRepository,
                        ContactRepository contactRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactGroupRepository = contactGroupRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User createUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("USER");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // 为新用户创建默认分组
        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setName("默认分组");
        defaultGroup.setUser(savedUser);
        defaultGroup.setIsDefault(true);
        defaultGroup.setSortOrder(0);
        contactGroupRepository.save(defaultGroup);

        return savedUser;
    }

    public User updateUser(Long userId, String email, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 不允许将角色改为 ADMIN
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (enabled != null) {
            user.setEnabled(enabled);
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }

        // 级联删除：先删联系人，再删分组，最后删用户
        contactRepository.deleteByUserId(userId);
        contactGroupRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    public User toggleEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setEnabled(!Boolean.TRUE.equals(user.getEnabled()));
        return userRepository.save(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}