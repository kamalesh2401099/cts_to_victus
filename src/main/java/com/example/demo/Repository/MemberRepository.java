package com.example.demo.Repository;

import com.example.demo.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // JpaRepository provides standard CRUD operations:
    // save(), findById(), findAll(), deleteById(), existsById(), etc.

    // Custom method to find a member by email (for login and registration checks)
    Optional<Member> findByEmail(String email);

    // Custom method to check if a member with the given email already exists
    boolean existsByEmail(String email);
}
