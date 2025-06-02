package com.example.demo.Repository;

import com.example.demo.Entity.BorrowingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Long> {
    // JpaRepository provides standard CRUD operations:
    // save(), findById(), findAll(), deleteById(), existsById(), etc.

    /**
     * Finds all borrowing transactions for a specific member that are currently in a 'BORROWED' status.
     * This is useful for displaying a member's currently borrowed books.
     * @param memberId The ID of the member.
     * @param status The status of the transaction (e.g., "BORROWED").
     * @return A list of BorrowingTransaction objects.
     */
    List<BorrowingTransaction> findByMemberMemberIdAndStatus(Long memberId, String status);

    /**
     * Finds a specific borrowing transaction by book ID and member ID that is currently borrowed.
     * This helps prevent a member from borrowing the same book multiple times if they haven't returned it.
     * @param bookId The ID of the book.
     * @param memberId The ID of the member.
     * @param status The status of the transaction (e.g., "BORROWED").
     * @return An Optional containing the BorrowingTransaction if found.
     */
    Optional<BorrowingTransaction> findByBookBookIdAndMemberMemberIdAndStatus(Long bookId, Long memberId, String status);

    /**
     * Counts the number of books currently borrowed by a specific member.
     * Used to enforce borrowing limits.
     * @param memberId The ID of the member.
     * @param status The status of the transaction (e.g., "BORROWED").
     * @return The count of borrowed books.
     */
    long countByMemberMemberIdAndStatus(Long memberId, String status);

    /**
     * Finds all borrowing transactions with a specific status.
     * Useful for scheduled tasks (e.g., finding all 'BORROWED' books to check for overdue status).
     * @param status The status of the transaction (e.g., "BORROWED", "OVERDUE", "RETURNED").
     * @return A list of BorrowingTransaction objects.
     */
    List<BorrowingTransaction> findByStatus(String status);
}
