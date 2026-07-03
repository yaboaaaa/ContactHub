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
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository,
                          ContactGroupRepository contactGroupRepository,
                          UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.contactGroupRepository = contactGroupRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private String escapeLike(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Transactional(readOnly = true)
    public Page<Contact> searchContacts(Long userId, String keyword, String phone,
                                        String province, String city, String district,
                                        String company, String jobTitle, Long groupId,
                                        int page, int size) {
        Specification<Contact> spec = (root, query, cb) -> {
            // Eagerly fetch group to avoid LazyInitializationException during JSON serialization
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("group", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + escapeLike(keyword) + "%";
                predicates.add(cb.like(root.get("name"), pattern, '\\'));
            }

            if (StringUtils.hasText(phone)) {
                String pattern = "%" + escapeLike(phone) + "%";
                predicates.add(cb.or(
                        cb.like(root.get("phoneMobile"), pattern, '\\'),
                        cb.like(root.get("phoneHome"), pattern, '\\'),
                        cb.like(root.get("phoneWork"), pattern, '\\')
                ));
            }

            if (StringUtils.hasText(province)) {
                predicates.add(cb.equal(root.get("province"), province));
            }

            if (StringUtils.hasText(city)) {
                predicates.add(cb.equal(root.get("city"), city));
            }

            if (StringUtils.hasText(district)) {
                predicates.add(cb.equal(root.get("district"), district));
            }

            if (StringUtils.hasText(company)) {
                String pattern = "%" + escapeLike(company) + "%";
                predicates.add(cb.like(root.get("company"), pattern, '\\'));
            }

            if (StringUtils.hasText(jobTitle)) {
                String pattern = "%" + escapeLike(jobTitle) + "%";
                predicates.add(cb.like(root.get("jobTitle"), pattern, '\\'));
            }

            if (groupId != null) {
                predicates.add(cb.equal(root.get("group").get("id"), groupId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return contactRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public List<Contact> getAllContacts(Long userId) {
        return contactRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public Page<Contact> getContactsByGroup(Long userId, Long groupId, int page, int size) {
        Specification<Contact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.equal(root.get("group").get("id"), groupId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return contactRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Contact createContact(ContactDTO dto, Long userId) {
        Contact contact = new Contact();
        applyDtoToContact(contact, dto);

        User userRef = new User();
        userRef.setId(userId);
        contact.setUser(userRef);

        if (dto.getGroupId() == null) {
            ContactGroup defaultGroup = contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)
                    .orElseThrow(() -> new BusinessException("默认分组不存在"));
            contact.setGroup(defaultGroup);
        } else {
            ContactGroup group = contactGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
            if (!group.getUser().getId().equals(userId)) {
                throw new BusinessException("无权操作该分组");
            }
            contact.setGroup(group);
        }

        contact.setIsDeleted(false);
        contact.setCreatedAt(LocalDateTime.now());

        Contact saved = contactRepository.save(contact);
        log.info("Created contact: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return saved;
    }

    public Contact updateContact(Long contactId, ContactDTO dto, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        applyDtoToContact(contact, dto);

        if (dto.getGroupId() != null && !dto.getGroupId().equals(
                contact.getGroup() != null ? contact.getGroup().getId() : null)) {
            ContactGroup group = contactGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
            if (!group.getUser().getId().equals(userId)) {
                throw new BusinessException("无权操作该分组");
            }
            contact.setGroup(group);
        }

        contact.setUpdatedAt(LocalDateTime.now());

        Contact saved = contactRepository.save(contact);
        log.info("Updated contact: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return saved;
    }

    public void softDelete(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        contact.setIsDeleted(true);
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);
        log.info("Soft deleted contact: id={}, userId={}", contactId, userId);
    }

    public void restore(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        contact.setIsDeleted(false);
        contact.setDeletedAt(null);
        contactRepository.save(contact);
        log.info("Restored contact: id={}, userId={}", contactId, userId);
    }

    public void permanentDelete(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该联系人");
        }

        contactRepository.delete(contact);
        log.info("Permanently deleted contact: id={}, userId={}", contactId, userId);
    }

    public void emptyRecycleBin(Long userId) {
        contactRepository.deleteByUserIdAndIsDeletedTrue(userId);
        log.info("Emptied recycle bin for userId={}", userId);
    }

    @Transactional(readOnly = true)
    public Page<Contact> getRecycleBin(Long userId, int page, int size) {
        return contactRepository.findByUserIdAndIsDeletedTrue(userId, PageRequest.of(page, size, Sort.by("deletedAt").descending()));
    }

    @Transactional(readOnly = true)
    public Optional<Contact> getById(Long contactId) {
        return contactRepository.findById(contactId);
    }

    private void applyDtoToContact(Contact contact, ContactDTO dto) {
        String familyName = dto.getFamilyName();
        String givenName = dto.getGivenName();

        contact.setFamilyName(familyName);
        contact.setGivenName(givenName);

        // Build name from familyName + givenName
        contact.setName(familyName + givenName);

        contact.setGender(dto.getGender());
        contact.setPhoneMobile(dto.getPhoneMobile());
        contact.setPhoneHome(dto.getPhoneHome());
        contact.setPhoneWork(dto.getPhoneWork());
        contact.setEmail(dto.getEmail());
        contact.setCompany(dto.getCompany());
        contact.setJobTitle(dto.getJobTitle());
        contact.setProvince(dto.getProvince());
        contact.setCity(dto.getCity());
        contact.setDistrict(dto.getDistrict());
        contact.setAddressDetail(dto.getAddressDetail());
        contact.setNotes(dto.getNotes());

        if (StringUtils.hasText(dto.getBirthday())) {
            try {
                contact.setBirthday(LocalDate.parse(dto.getBirthday()));
            } catch (Exception e) {
                log.warn("Invalid birthday format: {}", dto.getBirthday());
            }
        } else {
            contact.setBirthday(null);
        }
    }
}