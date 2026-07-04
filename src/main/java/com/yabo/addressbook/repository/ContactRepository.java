package com.yabo.addressbook.repository;

import com.yabo.addressbook.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    @EntityGraph(attributePaths = {"group"})
    Page<Contact> findByUserIdAndIsDeletedTrue(Long userId, Pageable pageable);

    List<Contact> findByUserIdAndIsDeletedTrue(Long userId);

    @Query("SELECT c FROM Contact c JOIN FETCH c.group WHERE c.user.id = :userId AND c.isDeleted = false")
    List<Contact> findByUserIdAndIsDeletedFalse(Long userId);

    List<Contact> findByGroupIdAndIsDeletedFalse(Long groupId);

    long countByUserIdAndIsDeletedTrue(Long userId);

    void deleteByUserId(Long userId);

    void deleteByGroupId(Long groupId);

    Optional<Contact> findByUid(String uid);

    @Modifying
    @Query("UPDATE Contact c SET c.group.id = :newGroupId WHERE c.group.id = :oldGroupId AND c.isDeleted = false")
    int updateGroupIdByGroupIdAndIsDeletedFalse(Long oldGroupId, Long newGroupId);

    @Modifying
    @Query("DELETE FROM Contact c WHERE c.user.id = :userId AND c.isDeleted = true")
    void deleteByUserIdAndIsDeletedTrue(Long userId);
}