package com.example.demo.Controller;

import com.example.demo.Entity.BorrowingTransaction;
import com.example.demo.Service.BorrowingTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowingTransactionController {

    @Autowired
    private BorrowingTransactionService transactionService;

    /**
     * Endpoint to handle a book borrowing request.
     * Requires bookId and memberId in the request body.
     * Example Body: {"bookId": 1, "memberId": 1}
     */
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody Map<String, Long> request) {
        Long bookId = request.get("bookId");
        Long memberId = request.get("memberId");

        if (bookId == null || memberId == null) {
            return new ResponseEntity<>("Book ID and Member ID are required.", HttpStatus.BAD_REQUEST);
        }

        BorrowingTransaction transaction = transactionService.borrowBook(bookId, memberId);

        if (transaction != null) {
            return new ResponseEntity<>(transaction, HttpStatus.CREATED);
        } else {
            // More specific error messages can be implemented in service
            return new ResponseEntity<>("Failed to borrow book. Check book availability, member status, or borrowing limits.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint to handle a book return request.
     * Requires transactionId in the path variable.
     */
    @PostMapping("/return/{transactionId}")
    public ResponseEntity<?> returnBook(@PathVariable Long transactionId) {
        BorrowingTransaction updatedTransaction = transactionService.returnBook(transactionId);

        if (updatedTransaction != null) {
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to return book. Transaction not found or already returned.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint to get all currently borrowed books for a specific member.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowingTransaction>> getBorrowedBooksByMember(@PathVariable Long memberId) {
        List<BorrowingTransaction> borrowedBooks = transactionService.getBorrowedBooksByMember(memberId);
        return new ResponseEntity<>(borrowedBooks, HttpStatus.OK);
    }

    /**
     * Endpoint to check if a specific transaction is overdue.
     */
    @GetMapping("/overdue/check/{transactionId}")
    public ResponseEntity<?> checkOverdue(@PathVariable Long transactionId) {
        boolean isOverdue = transactionService.isOverdue(transactionId);
        if (isOverdue) {
            long overdueDays = transactionService.getOverdueDays(transactionId);
            return new ResponseEntity<>(Map.of("overdue", true, "overdueDays", overdueDays), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("overdue", false), HttpStatus.OK);
        }
    }

    /**
     * Endpoint to get a specific borrowing transaction by ID.
     * Useful for fetching details before returning or checking status.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<BorrowingTransaction> getBorrowingTransactionById(@PathVariable Long transactionId) {
        Optional<BorrowingTransaction> transaction = transactionService.getBorrowingTransactionById(transactionId);
        return transaction.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
