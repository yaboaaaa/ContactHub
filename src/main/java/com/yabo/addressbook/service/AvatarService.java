package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.util.ImageUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Transactional
public class AvatarService {

    private final UserRepository userRepository;

    public AvatarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Upload and resize avatar for the given username.
     *
     * @param username the username
     * @param file     the uploaded image file
     * @throws BusinessException if user not found or image validation fails
     */
    public void uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在: " + username));

        byte[] resizedBytes = ImageUtil.resizeAvatar(file);

        user.setAvatarData(resizedBytes);
        user.setAvatarContentType(file.getContentType());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    /**
     * Get avatar data for the given username.
     *
     * @param username the username
     * @return avatar byte array, or null if not found
     */
    public byte[] getAvatar(String username) {
        return userRepository.findByUsername(username)
                .map(User::getAvatarData)
                .orElse(null);
    }

    /**
     * Get avatar data for the given user id.
     *
     * @param userId the user id
     * @return avatar byte array, or null if not found
     */
    public byte[] getAvatarById(Long userId) {
        return userRepository.findById(userId)
                .map(User::getAvatarData)
                .orElse(null);
    }

    /**
     * Get avatar content type for the given username.
     *
     * @param username the username
     * @return content type string, or null if not found
     */
    public String getAvatarContentType(String username) {
        return userRepository.findByUsername(username)
                .map(User::getAvatarContentType)
                .orElse(null);
    }
}