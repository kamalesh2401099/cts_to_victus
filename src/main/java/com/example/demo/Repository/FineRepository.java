package com.example.demo.Repository;

import com.example.demo.Entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    // Find all fines for a specific member by their ID
    List<Fine> findByMemberMemberId(Long memberId);

    // Find all pending fines for a specific member
    List<Fine> findByMemberMemberIdAndStatus(Long memberId, String status);

    // Find a fine by the associated borrowing transaction ID and status
    Optional<Fine> findByBorrowingTransactionTransactionIdAndStatus(Long transactionId, String status);

    // Find a fine by the associated borrowing transaction ID (regardless of status)
    Optional<Fine> findByBorrowingTransactionTransactionId(Long transactionId);
}
