package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.util.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ContactGroupRepository contactGroupRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactGroupRepository = contactGroupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password, String email, String nickname) {
        String usernameError = ValidationUtil.validateUsername(username);
        if (usernameError != null) {
            throw new BusinessException(usernameError);
        }

        String passwordError = ValidationUtil.validatePassword(password);
        if (passwordError != null) {
            throw new BusinessException(passwordError);
        }

        String emailError = ValidationUtil.validateEmail(email);
        if (emailError != null) {
            throw new BusinessException(emailError);
        }

        String trimmedUsername = ValidationUtil.getTrimmedUsername(username);
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(ValidationUtil.getTrimmedEmail(email));
        user.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname.trim() : generateRandomNickname());
        user.setRole("USER");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setName("默认分组");
        defaultGroup.setUser(savedUser);
        defaultGroup.setIsDefault(true);
        defaultGroup.setSortOrder(0);
        contactGroupRepository.save(defaultGroup);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void updateProfile(String currentUsername, String newUsername, String email, String nickname) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (newUsername != null && !newUsername.trim().isEmpty()) {
            String usernameError = ValidationUtil.validateUsername(newUsername);
            if (usernameError != null) {
                throw new BusinessException(usernameError);
            }
            String trimmedUsername = ValidationUtil.getTrimmedUsername(newUsername);
            if (!trimmedUsername.equals(currentUsername) && userRepository.existsByUsername(trimmedUsername)) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(trimmedUsername);
        }

        if (email != null) {
            String emailError = ValidationUtil.validateEmail(email);
            if (emailError != null) {
                throw new BusinessException(emailError);
            }
            user.setEmail(ValidationUtil.getTrimmedEmail(email));
        }

        if (nickname != null) {
            user.setNickname(nickname.trim());
        }

        userRepository.save(user);
    }

    public void updatePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private static final String[] ADJECTIVES = {"快乐的", "聪明的", "勇敢的", "温柔的", "活泼的", "神秘的", "优雅的", "酷酷的", "阳光的", "安静的"};
    private static final String[] NOUNS = {"小猫", "小狗", "兔子", "熊猫", "海豚", "松鼠", "狐狸", "企鹅", "小鹿", "蝴蝶"};

    public String generateRandomNickname() {
        String adj = ADJECTIVES[(int) (Math.random() * ADJECTIVES.length)];
        String noun = NOUNS[(int) (Math.random() * NOUNS.length)];
        String suffix = String.format("%04d", (int) (Math.random() * 10000));
        return adj + noun + suffix;
    }
}