package com.yabo.addressbook.service;

import com.yabo.addressbook.dto.ContactDTO;
import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private ContactService contactService;

    private Long userId;
    private User user;
    private ContactGroup defaultGroup;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, contactGroupRepository, userRepository);
        userId = 1L;

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");

        defaultGroup = new ContactGroup();
        defaultGroup.setId(1L);
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(true);
        defaultGroup.setUser(user);

        // Set up SecurityContextHolder mock
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("testuser");
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- searchContacts ---

    @Test
    void searchContacts_shouldBuildSpecificationWithAllParameters() {
        String keyword = "张三";
        String phone = "13800138000";
        String province = "广东省";
        String city = "深圳市";
        String district = "南山区";
        String company = "科技有限公司";
        String jobTitle = "工程师";
        Long groupId = 2L;
        int page = 0;
        int size = 10;

        Contact contact = new Contact();
        contact.setId(1L);
        contact.setName(keyword);
        contact.setUser(user);

        Page<Contact> expectedPage = new PageImpl<>(List.of(contact));
        when(contactRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Contact> result = contactService.searchContacts(
                userId, keyword, phone, province, city, district,
                company, jobTitle, groupId, page, size);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(keyword);

        ArgumentCaptor<Specification<Contact>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(contactRepository).findAll(specCaptor.capture(), pageCaptor.capture());

        PageRequest capturedPageRequest = pageCaptor.getValue();
        assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
        assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);
    }

    @Test
    void searchContacts_shouldBuildSpecificationWithPartialParameters() {
        String keyword = "李四";
        int page = 0;
        int size = 20;

        Contact contact = new Contact();
        contact.setId(2L);
        contact.setName(keyword);
        contact.setUser(user);

        Page<Contact> expectedPage = new PageImpl<>(List.of(contact));
        when(contactRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Contact> result = contactService.searchContacts(
                userId, keyword, null, null, null, null,
                null, null, null, page, size);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(keyword);
    }

    // --- createContact ---

    @Test
    void createContact_shouldAssignToDefaultGroup_whenGroupIdIsNull() {
        ContactDTO dto = new ContactDTO();
        dto.setName("测试联系人");
        dto.setPhoneMobile("13900139000");

        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(userId))
                .thenReturn(Optional.of(defaultGroup));

        Contact savedContact = new Contact();
        savedContact.setId(1L);
        savedContact.setName(dto.getName());
        savedContact.setPhoneMobile(dto.getPhoneMobile());
        savedContact.setUser(user);
        savedContact.setGroup(defaultGroup);
        savedContact.setIsDeleted(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        Contact result = contactService.createContact(dto, userId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getGroup()).isEqualTo(defaultGroup);
        assertThat(result.getIsDeleted()).isFalse();

        verify(contactGroupRepository).findByUserIdAndIsDefaultTrue(userId);
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void createContact_shouldAssignToSpecifiedGroup_whenGroupIdIsProvided() {
        Long groupId = 2L;
        ContactGroup customGroup = new ContactGroup();
        customGroup.setId(groupId);
        customGroup.setName("自定义分组");
        customGroup.setUser(user);

        ContactDTO dto = new ContactDTO();
        dto.setName("测试联系人");
        dto.setPhoneMobile("13900139000");
        dto.setGroupId(groupId);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(customGroup));

        Contact savedContact = new Contact();
        savedContact.setId(1L);
        savedContact.setName(dto.getName());
        savedContact.setGroup(customGroup);
        savedContact.setUser(user);
        savedContact.setIsDeleted(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        Contact result = contactService.createContact(dto, userId);

        assertThat(result.getGroup().getId()).isEqualTo(groupId);
        verify(contactGroupRepository).findById(groupId);
        verify(contactGroupRepository, never()).findByUserIdAndIsDefaultTrue(anyLong());
    }

    @Test
    void createContact_shouldThrowBusinessException_whenGroupBelongsToOtherUser() {
        Long groupId = 2L;
        User otherUser = new User();
        otherUser.setId(3L);

        ContactGroup otherGroup = new ContactGroup();
        otherGroup.setId(groupId);
        otherGroup.setName("他人的分组");
        otherGroup.setUser(otherUser);

        ContactDTO dto = new ContactDTO();
        dto.setName("测试联系人");
        dto.setPhoneMobile("13900139000");
        dto.setGroupId(groupId);

        when(contactGroupRepository.findById(groupId)).thenReturn(Optional.of(otherGroup));

        assertThatThrownBy(() -> contactService.createContact(dto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权操作该分组");
    }

    @Test
    void createContact_shouldThrowEntityNotFoundException_whenDefaultGroupNotFound() {
        ContactDTO dto = new ContactDTO();
        dto.setName("测试联系人");
        dto.setPhoneMobile("13900139000");

        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.createContact(dto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("默认分组不存在");
    }

    // --- updateContact ---

    @Test
    void updateContact_shouldUpdateContact() {
        Long contactId = 1L;

        Contact existingContact = new Contact();
        existingContact.setId(contactId);
        existingContact.setName("旧名称");
        existingContact.setUser(user);
        existingContact.setGroup(defaultGroup);

        ContactDTO dto = new ContactDTO();
        dto.setName("新名称");
        dto.setPhoneMobile("13800138000");

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(existingContact));

        Contact updatedContact = new Contact();
        updatedContact.setId(contactId);
        updatedContact.setName(dto.getName());
        updatedContact.setPhoneMobile(dto.getPhoneMobile());
        updatedContact.setUser(user);
        updatedContact.setGroup(defaultGroup);
        when(contactRepository.save(any(Contact.class))).thenReturn(updatedContact);

        Contact result = contactService.updateContact(contactId, dto, userId);

        assertThat(result.getName()).isEqualTo("新名称");
        verify(contactRepository).findById(contactId);
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void updateContact_shouldThrowEntityNotFoundException_whenContactNotFound() {
        Long contactId = 999L;
        when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.updateContact(contactId, new ContactDTO(), userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("联系人不存在");
    }

    @Test
    void updateContact_shouldThrowBusinessException_whenUserNotOwner() {
        Long contactId = 1L;
        Long otherUserId = 2L;

        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName("名称");
        User otherUser = new User();
        otherUser.setId(otherUserId);
        contact.setUser(otherUser);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        assertThatThrownBy(() -> contactService.updateContact(contactId, new ContactDTO(), userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权操作该联系人");
    }

    // --- softDelete ---

    @Test
    void softDelete_shouldSetIsDeletedTrueAndDeletedAt() {
        Long contactId = 1L;
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName("测试");
        contact.setUser(user);
        contact.setIsDeleted(false);
        contact.setDeletedAt(null);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        contactService.softDelete(contactId, userId);

        assertThat(contact.getIsDeleted()).isTrue();
        assertThat(contact.getDeletedAt()).isNotNull();
        verify(contactRepository).save(contact);
    }

    @Test
    void softDelete_shouldThrowEntityNotFoundException_whenContactNotFound() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.softDelete(999L, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("联系人不存在");
    }

    // --- restore ---

    @Test
    void restore_shouldSetIsDeletedFalseAndClearDeletedAt() {
        Long contactId = 1L;
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName("测试");
        contact.setUser(user);
        contact.setIsDeleted(true);
        contact.setDeletedAt(LocalDateTime.now());

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        contactService.restore(contactId, userId);

        assertThat(contact.getIsDeleted()).isFalse();
        assertThat(contact.getDeletedAt()).isNull();
        verify(contactRepository).save(contact);
    }

    // --- permanentDelete ---

    @Test
    void permanentDelete_shouldPhysicallyDelete() {
        Long contactId = 1L;
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName("测试");
        contact.setUser(user);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        contactService.permanentDelete(contactId, userId);

        verify(contactRepository).delete(contact);
    }

    // --- emptyRecycleBin ---

    @Test
    void emptyRecycleBin_shouldDeleteAllDeletedContacts() {
        contactService.emptyRecycleBin(userId);

        verify(contactRepository).deleteByUserIdAndIsDeletedTrue(userId);
    }

    // --- getCurrentUser ---

    @Test
    void getCurrentUser_shouldReturnCurrentUser() {
        User result = contactService.getCurrentUser();

        assertThat(result).isEqualTo(user);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCurrentUser_shouldThrowBusinessException_whenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThatThrownBy(() -> contactService.getCurrentUser())
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户未登录");
    }

    @Test
    void getCurrentUser_shouldThrowBusinessException_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThatThrownBy(() -> contactService.getCurrentUser())
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户未登录");
    }

    @Test
    void getCurrentUser_shouldThrowBusinessException_whenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getCurrentUser())
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void getCurrentUser_shouldHandleStringPrincipal() {
        when(authentication.getPrincipal()).thenReturn("testuser");

        User result = contactService.getCurrentUser();
        assertThat(result).isEqualTo(user);
    }

    // --- getRecycleBin ---

    @Test
    void getRecycleBin_shouldReturnDeletedContacts() {
        Contact deletedContact = new Contact();
        deletedContact.setId(1L);
        deletedContact.setName("已删除");
        deletedContact.setUser(user);
        deletedContact.setIsDeleted(true);

        Page<Contact> expectedPage = new PageImpl<>(List.of(deletedContact));
        when(contactRepository.findByUserIdAndIsDeletedTrue(eq(userId), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Contact> result = contactService.getRecycleBin(userId, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsDeleted()).isTrue();
    }

    // --- getById ---

    @Test
    void getById_shouldReturnContact() {
        Long contactId = 1L;
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setName("测试");
        contact.setUser(user);

        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        Optional<Contact> result = contactService.getById(contactId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("测试");
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Contact> result = contactService.getById(999L);

        assertThat(result).isEmpty();
    }

    // --- getContactsByGroup ---

    @Test
    void getContactsByGroup_shouldReturnContactsForGroup() {
        Long groupId = 1L;
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setName("分组联系人");
        contact.setUser(user);

        Page<Contact> expectedPage = new PageImpl<>(List.of(contact));
        when(contactRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(expectedPage);

        Page<Contact> result = contactService.getContactsByGroup(userId, groupId, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }
}