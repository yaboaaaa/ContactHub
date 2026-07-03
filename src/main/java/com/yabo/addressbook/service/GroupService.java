package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {

    private final ContactGroupRepository contactGroupRepository;
    private final ContactRepository contactRepository;

    public GroupService(ContactGroupRepository contactGroupRepository, ContactRepository contactRepository) {
        this.contactGroupRepository = contactGroupRepository;
        this.contactRepository = contactRepository;
    }

    public List<ContactGroup> listGroups(Long userId) {
        return contactGroupRepository.findByUserIdOrderBySortOrderAscCreatedAtAsc(userId);
    }

    public ContactGroup createGroup(Long userId, String name) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("分组名称不能为空");
        }

        if (contactGroupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException("分组名称已存在");
        }

        ContactGroup group = new ContactGroup();
        group.setName(name.trim());
        User userRef = new User();
        userRef.setId(userId);
        group.setUser(userRef);
        group.setSortOrder(0);
        group.setIsDefault(false);
        group.setCreatedAt(LocalDateTime.now());

        return contactGroupRepository.save(group);
    }

    public ContactGroup updateGroup(Long groupId, Long userId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new BusinessException("分组名称不能为空");
        }

        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));

        if (!group.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该分组");
        }

        // 默认分组可以更新名称
        String trimmedName = newName.trim();

        // 检查名称唯一性（排除当前分组）
        Optional<ContactGroup> existing = contactGroupRepository.findByUserIdAndName(userId, trimmedName);
        if (existing.isPresent() && !existing.get().getId().equals(groupId)) {
            throw new BusinessException("分组名称已存在");
        }

        group.setName(trimmedName);
        return contactGroupRepository.save(group);
    }

    public void deleteGroup(Long groupId, Long userId) {
        // 检查用户分组数量，只剩一个时不允许删除
        if (contactGroupRepository.countByUserId(userId) <= 1) {
            throw new BusinessException("只剩一个分组，无法删除");
        }

        ContactGroup group = contactGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));

        if (!group.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该分组");
        }

        if (Boolean.TRUE.equals(group.getIsDefault())) {
            throw new BusinessException("默认分组不可删除");
        }

        // 查找用户的默认分组
        ContactGroup defaultGroup = contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new BusinessException("默认分组不存在"));

        // 将该分组下所有未删除的联系人移动到默认分组
        if (!defaultGroup.getId().equals(groupId)) {
            contactRepository.updateGroupIdByGroupIdAndIsDeletedFalse(groupId, defaultGroup.getId());
        }

        contactGroupRepository.delete(group);
    }
}