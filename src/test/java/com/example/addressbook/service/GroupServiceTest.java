package com.example.addressbook.service;

import com.example.addressbook.entity.ContactGroup;
import com.example.addressbook.entity.User;
import com.example.addressbook.exception.BusinessException;
import com.example.addressbook.repository.ContactGroupRepository;
import com.example.addressbook.repository.ContactRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private ContactRepository contactRepository;

    private GroupService groupService;

    private Long userId;
    private User userRef;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(contactGroupRepository, contactRepository);
        userId = 1L;
        userRef = new User();
        userRef.setId(userId);
    }

    @Test
    void listGroups_shouldReturnGroupsSortedBySortOrderAndCreatedAt() {
        ContactGroup group1 = new ContactGroup();
        group1.setId(1L);
        group1.setName("分组A");
        group1.setSortOrder(1);
        group1.setCreatedAt(LocalDateTime.now().minusDays(1));

        ContactGroup group2 = new ContactGroup();
        group2.setId(2L);
        group2.setName("分组B");
        group2.setSortOrder(0);
        group2.setCreatedAt(LocalDateTime.now());

        when(contactGroupRepository.findByUserIdOrderBySortOrderAscCreatedAtAsc(userId))
                .thenReturn(Arrays.asList(group2, group1));

        List<ContactGroup> result = groupService.listGroups(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("分组B");
        assertThat(result.get(1).getName()).isEqualTo("分组A");
        verify(contactGroupRepository).findByUserIdOrderBySortOrderAscCreatedAtAsc(userId);
    }

    @Test
    void createGroup_shouldCreateGroup_whenNameIsUnique() {
        String groupName = "新分组";
        when(contactGroupRepository.existsByUserIdAndName(userId, groupName)).thenReturn(false);

        ContactGroup savedGroup = new ContactGroup();
        savedGroup.setId(1L);
        savedGroup.setName(groupName);
        savedGroup.setUser(userRef);
        savedGroup.setSortOrder(0);
        savedGroup.setIsDefault(false);
        savedGroup.setCreatedAt(LocalDateTime.now());
        when(contactGroupRepository.save(any(ContactGroup.class))).thenReturn(savedGroup);

        ContactGroup result = groupService.createGroup(userId, groupName);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(groupName);
        assertThat(result.getIsDefault()).isFalse();
        verify(contactGroupRepository).existsByUserIdAndName(userId, groupName);
        verify(contactGroupRepository).save(any(ContactGroup.class));
    }

    @Test
    void createGroup_shouldThrowBusinessException_whenNameAlreadyExists() {
        String groupName = "已存在的分组";
        when(contactGroupRepository.existsByUserIdAndName(userId, groupName)).thenReturn(true);

        assertThatThrownBy(() -> groupService.createGroup(userId, groupName))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组名称已存在");

        verify(contactGroupRepository).existsByUserIdAndName(userId, groupName);
        verify(contactGroupRepository, never()).save(any(ContactGroup.class));
    }

    @Test
    void createGroup_shouldThrowBusinessException_whenUserIdIsNull() {
        assertThatThrownBy(() -> groupService.createGroup(null, "分组"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户ID不能为空");
        verifyNoInteractions(contactGroupRepository);
    }

    @Test
    void createGroup_shouldThrowBusinessException_whenNameIsBlank() {
        assertThatThrownBy(() -> groupService.createGroup(userId, "  "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组名称不能为空");
        verifyNoInteractions(contactGroupRepository);
    }

    @Test
    void deleteGroup_shouldThrowBusinessException_whenGroupIsDefault() {
        Long groupId = 1L;
        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setId(groupId);
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(true);
        defaultGroup.setUser(userRef);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(defaultGroup));

        assertThatThrownBy(() -> groupService.deleteGroup(groupId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("默认分组不可删除");

        verify(contactGroupRepository).findById(groupId);
        verify(contactGroupRepository, never()).delete(any(ContactGroup.class));
    }

    @Test
    void deleteGroup_shouldMoveContactsToDefaultGroupAndDeleteGroup_whenNotDefault() {
        Long groupId = 2L;
        Long defaultGroupId = 1L;

        ContactGroup groupToDelete = new ContactGroup();
        groupToDelete.setId(groupId);
        groupToDelete.setName("普通分组");
        groupToDelete.setIsDefault(false);
        groupToDelete.setUser(userRef);

        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setId(defaultGroupId);
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(true);
        defaultGroup.setUser(userRef);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(groupToDelete));
        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.of(defaultGroup));

        groupService.deleteGroup(groupId, userId);

        verify(contactRepository).updateGroupIdByGroupIdAndIsDeletedFalse(groupId, defaultGroupId);
        verify(contactGroupRepository).delete(groupToDelete);
    }

    @Test
    void deleteGroup_shouldThrowException_whenDefaultGroupNotFound() {
        Long groupId = 2L;
        ContactGroup groupToDelete = new ContactGroup();
        groupToDelete.setId(groupId);
        groupToDelete.setName("普通分组");
        groupToDelete.setIsDefault(false);
        groupToDelete.setUser(userRef);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(groupToDelete));
        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.deleteGroup(groupId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("默认分组不存在");

        verify(contactRepository, never()).updateGroupIdByGroupIdAndIsDeletedFalse(anyLong(), anyLong());
        verify(contactGroupRepository, never()).delete(any(ContactGroup.class));
    }

    @Test
    void deleteGroup_shouldThrowEntityNotFoundException_whenGroupNotFound() {
        Long groupId = 999L;
        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.deleteGroup(groupId, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("分组不存在");
    }

    @Test
    void updateGroup_shouldUpdateGroupName() {
        Long groupId = 1L;
        String newName = "新名称";

        ContactGroup group = new ContactGroup();
        group.setId(groupId);
        group.setName("旧名称");
        group.setUser(userRef);
        group.setIsDefault(false);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(contactGroupRepository.findByUserIdAndName(userId, newName)).thenReturn(Optional.empty());

        ContactGroup updatedGroup = new ContactGroup();
        updatedGroup.setId(groupId);
        updatedGroup.setName(newName);
        updatedGroup.setUser(userRef);
        when(contactGroupRepository.save(any(ContactGroup.class))).thenReturn(updatedGroup);

        ContactGroup result = groupService.updateGroup(groupId, userId, newName);

        assertThat(result.getName()).isEqualTo(newName);
        verify(contactGroupRepository).findById(groupId);
        verify(contactGroupRepository).findByUserIdAndName(userId, newName);
        verify(contactGroupRepository).save(group);
    }

    @Test
    void updateGroup_shouldThrowBusinessException_whenNameAlreadyExists() {
        Long groupId = 1L;
        String newName = "已存在的名称";

        ContactGroup group = new ContactGroup();
        group.setId(groupId);
        group.setName("旧名称");
        group.setUser(userRef);

        ContactGroup existingGroup = new ContactGroup();
        existingGroup.setId(2L);
        existingGroup.setName(newName);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(contactGroupRepository.findByUserIdAndName(userId, newName)).thenReturn(Optional.of(existingGroup));

        assertThatThrownBy(() -> groupService.updateGroup(groupId, userId, newName))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组名称已存在");

        verify(contactGroupRepository, never()).save(any(ContactGroup.class));
    }

    @Test
    void updateGroup_shouldUpdateName_whenSameGroupIdExists() {
        Long groupId = 1L;
        String newName = "新名称";

        ContactGroup group = new ContactGroup();
        group.setId(groupId);
        group.setName("旧名称");
        group.setUser(userRef);

        ContactGroup existingGroupWithSameId = new ContactGroup();
        existingGroupWithSameId.setId(groupId);
        existingGroupWithSameId.setName(newName);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(contactGroupRepository.findByUserIdAndName(userId, newName)).thenReturn(Optional.of(existingGroupWithSameId));

        ContactGroup updatedGroup = new ContactGroup();
        updatedGroup.setId(groupId);
        updatedGroup.setName(newName);
        when(contactGroupRepository.save(any(ContactGroup.class))).thenReturn(updatedGroup);

        ContactGroup result = groupService.updateGroup(groupId, userId, newName);

        assertThat(result.getName()).isEqualTo(newName);
        verify(contactGroupRepository).save(group);
    }

    @Test
    void updateGroup_shouldThrowBusinessException_whenNewNameIsBlank() {
        assertThatThrownBy(() -> groupService.updateGroup(1L, userId, "  "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分组名称不能为空");
        verifyNoInteractions(contactGroupRepository);
    }

    @Test
    void updateGroup_shouldThrowEntityNotFoundException_whenGroupNotFound() {
        when(contactGroupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.updateGroup(999L, userId, "名称"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("分组不存在");
    }

    @Test
    void updateGroup_shouldThrowBusinessException_whenUserNotOwner() {
        Long groupId = 1L;
        Long otherUserId = 2L;

        ContactGroup group = new ContactGroup();
        group.setId(groupId);
        group.setName("分组");
        User otherUser = new User();
        otherUser.setId(otherUserId);
        group.setUser(otherUser);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.updateGroup(groupId, userId, "新名称"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权操作该分组");
    }
}