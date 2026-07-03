package com.example.addressbook.repository;

import com.example.addressbook.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    Page<Contact> findByUserIdAndIsDeletedTrue(Long userId, Pageable pageable);

    List<Contact> findByUserIdAndIsDeletedTrue(Long userId);

    List<Contact> findByGroupIdAndIsDeletedFalse(Long groupId);

    long countByUserIdAndIsDeletedTrue(Long userId);

    void deleteByUserId(Long userId);

    void deleteByGroupId(Long groupId);

    @Modifying
    @Query("UPDATE Contact c SET c.group.id = :newGroupId WHERE c.group.id = :oldGroupId AND c.isDeleted = false")
    int updateGroupIdByGroupIdAndIsDeletedFalse(Long oldGroupId, Long newGroupId);

    @Modifying
    @Query("DELETE FROM Contact c WHERE c.user.id = :userId AND c.isDeleted = true")
    void deleteByUserIdAndIsDeletedTrue(Long userId);
}