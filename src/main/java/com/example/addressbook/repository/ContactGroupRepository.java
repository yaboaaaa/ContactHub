package com.example.addressbook.repository;

import com.example.addressbook.entity.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactGroupRepository extends JpaRepository<ContactGroup, Long> {

    List<ContactGroup> findByUserIdOrderBySortOrderAscCreatedAtAsc(Long userId);

    Optional<ContactGroup> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<ContactGroup> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);

    void deleteByUserId(Long userId);
}