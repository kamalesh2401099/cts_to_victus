package com.example.demo.Service;

import com.example.demo.Entity.BorrowingTransaction;
import com.example.demo.Entity.Fine;
// import com.example.demo.Entity.Member; // REMOVE THIS LINE - it's no longer directly used
import com.example.demo.Repository.FineRepository;
import com.example.demo.Service.NotificationService; // CORRECT THIS IMPORT: Ensure it's from com.library.app.service, not com.example.demo.Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
@Service
public class FineService {

    @Autowired
    private FineRepository fineRepository;

    // REMOVED: @Autowired private MemberService memberService; // This field was reported as unused

    @Autowired
    private NotificationService notificationService;

    // Fine rate per day
    private static final BigDecimal FINE_RATE_PER_DAY = new BigDecimal("0.50");

    /**
     * Calculates the fine amount for a given overdue transaction.
     * @param transaction The overdue borrowing transaction.
     * @return The calculated fine amount.
     */
    public BigDecimal calculateFineAmount(BorrowingTransaction transaction) {
        if (transaction.getDueDate() == null || transaction.getReturnDate() == null) {
            return BigDecimal.ZERO;
        }

        LocalDate actualReturnDate = transaction.getReturnDate();
        LocalDate dueDate = transaction.getDueDate();

        if (actualReturnDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, actualReturnDate);
            return FINE_RATE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Creates a new fine record for an overdue transaction, or updates an existing pending one.
     * This method is typically called when a book is returned overdue or when an overdue check is performed.
     * @param transaction The borrowing transaction that is overdue.
     * @return The created Fine object, or existing pending fine if already created.
     */
    @Transactional
    public Fine createOrUpdateFine(BorrowingTransaction transaction) {
        // Check if a pending fine already exists for this transaction
        Optional<Fine> existingFineOptional = fineRepository.findByBorrowingTransactionTransactionIdAndStatus(transaction.getTransactionId(), "PENDING");

        if (existingFineOptional.isPresent()) {
            Fine existingFine = existingFineOptional.get();
            BigDecimal newAmount = calculateFineAmount(transaction);
            if (newAmount.compareTo(existingFine.getAmount()) != 0) {
                existingFine.setAmount(newAmount);
                existingFine.setFineDate(LocalDate.now()); // Update fine date to reassessment date
                Fine updatedFine = fineRepository.save(existingFine);
                // Update existing fine notification if amount changed
                notificationService.createFineNotification(
                    transaction.getMember().getMemberId(),
                    updatedFine.getFineId(),
                    updatedFine.getAmount(),
                    transaction.getBook().getTitle()
                );
                return updatedFine;
            }
            return existingFine; // No change needed
        } else {
            // Create a new fine
            BigDecimal fineAmount = calculateFineAmount(transaction);
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) { // Only create if fine amount is greater than zero
                Fine newFine = new Fine(transaction.getMember(), transaction, fineAmount, LocalDate.now(), "PENDING");
                Fine savedFine = fineRepository.save(newFine);
                // Create a new fine notification
                notificationService.createFineNotification(
                    transaction.getMember().getMemberId(),
                    savedFine.getFineId(),
                    savedFine.getAmount(),
                    transaction.getBook().getTitle()
                );
                return savedFine;
            }
        }
        return null; // No fine created (e.g., not overdue or amount is zero)
    }

    /**
     * Retrieves all fines for a specific member.
     * @param memberId The ID of the member.
     * @return A list of Fine objects.
     */
    public List<Fine> getFinesByMemberId(Long memberId) {
        return fineRepository.findByMemberMemberId(memberId);
    }

    /**
     * Retrieves all pending fines for a specific member.
     * @param memberId The ID of the member.
     * @return A list of pending Fine objects.
     */
    public List<Fine> getPendingFinesByMemberId(Long memberId) {
        return fineRepository.findByMemberMemberIdAndStatus(memberId, "PENDING");
    }

    /**
     * Marks a fine as paid.
     * @param fineId The ID of the fine to mark as paid.
     * @return The updated Fine object, or null if not found or already paid.
     */
    @Transactional
    public Optional<Fine> payFine(Long fineId) {
        return fineRepository.findById(fineId)
                .map(fine -> {
                    if ("PENDING".equalsIgnoreCase(fine.getStatus())) {
                        fine.setStatus("PAID");
                        Fine paidFine = fineRepository.save(fine);
                        // Delete the corresponding fine notification
                        notificationService.deleteFineNotification(paidFine.getFineId());
                        return paidFine;
                    }
                    return null; // Already paid or not pending
                });
    }

    /**
     * Marks a fine as paid by transaction ID.
     * This is useful when a member pays a fine directly linked to a specific transaction.
     * @param transactionId The ID of the borrowing transaction associated with the fine.
     * @return The updated Fine object, or null if not found or already paid.
     */
    @Transactional
    public Optional<Fine> payFineByBorrowingTransactionId(Long transactionId) {
        return fineRepository.findByBorrowingTransactionTransactionIdAndStatus(transactionId, "PENDING")
                .map(fine -> {
                    fine.setStatus("PAID");
                    Fine paidFine = fineRepository.save(fine);
                    // Delete the corresponding fine notification
                    notificationService.deleteFineNotification(paidFine.getFineId());
                    return paidFine;
                });
    }

    // @Scheduled(cron = "0 5 0 * * ?") // Example of a scheduled task for proactive fine assessment
    // @Transactional
    // public void assessOverdueFines() {
    //     // This would typically involve fetching all 'OVERDUE' transactions
    //     // and then calling createOrUpdateFine for each.
    // }
}
