package com.example.demo.Service;

import com.example.demo.Entity.Book;
import com.example.demo.Entity.BorrowingTransaction;
import com.example.demo.Entity.Member;
import com.example.demo.Repository.BorrowingTransactionRepository;
import com.example.demo.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // ADD THIS IMPORT
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowingTransactionService {

    @Autowired
    private BorrowingTransactionRepository transactionRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private FineService fineService;

    @Autowired
    private NotificationService notificationService;

    private static final int MAX_BORROWED_BOOKS = 5;
    private static final int BORROW_PERIOD_DAYS = 7;
    private static final int REMINDER_DAYS_BEFORE_DUE = 2;

    /**
     * Allows a member to borrow a book.
     * @param bookId The ID of the book to borrow.
     * @param memberId The ID of the member borrowing the book.
     * @return The created BorrowingTransaction, or null if borrowing fails.
     */
    @Transactional
    public BorrowingTransaction borrowBook(Long bookId, Long memberId) {
        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if (bookOptional.isEmpty() || bookOptional.get().getAvailableCopies() <= 0) {
            return null; // Book not found or not available
        }
        Book book = bookOptional.get();

        Optional<Member> memberOptional = memberService.getMemberById(memberId);
        if (memberOptional.isEmpty() || !"ACTIVE".equalsIgnoreCase(memberOptional.get().getMembershipStatus())) {
            return null; // Member not found or not active
        }
        Member member = memberOptional.get(); // 'member' variable is used later, warning should resolve.

        long currentBorrowedCount = transactionRepository.countByMemberMemberIdAndStatus(memberId, "BORROWED");
        if (currentBorrowedCount >= MAX_BORROWED_BOOKS) {
            return null; // Member has reached borrowing limit
        }

        if (transactionRepository.findByBookBookIdAndMemberMemberIdAndStatus(bookId, memberId, "BORROWED").isPresent()) {
            return null; // Member already has this book borrowed
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(BORROW_PERIOD_DAYS);
        BorrowingTransaction transaction = new BorrowingTransaction(book, member, borrowDate, dueDate, "BORROWED");

        bookService.updateBookCopies(bookId, -1);
        memberService.updateMemberLastBorrowDate(memberId, borrowDate);

        return transactionRepository.save(transaction);
    }

    /**
     * Allows a member to return a book.
     * @param transactionId The ID of the borrowing transaction to return.
     * @return The updated BorrowingTransaction, or null if return fails.
     */
    @Transactional
    public BorrowingTransaction returnBook(Long transactionId) {
        Optional<BorrowingTransaction> transactionOptional = transactionRepository.findById(transactionId);

        if (transactionOptional.isEmpty() || !"BORROWED".equalsIgnoreCase(transactionOptional.get().getStatus())) {
            return null; // Transaction not found or book already returned
        }

        BorrowingTransaction transaction = transactionOptional.get();
        Book book = transaction.getBook();
        Member member = transaction.getMember();

        transaction.setReturnDate(LocalDate.now());
        transaction.setStatus("RETURNED");
        BorrowingTransaction updatedTransaction = transactionRepository.save(transaction);

        bookService.updateBookCopies(book.getBookId(), 1);

        if (updatedTransaction.getReturnDate().isAfter(updatedTransaction.getDueDate())) {
            fineService.createOrUpdateFine(updatedTransaction);
        }

        return updatedTransaction;
    }

    /**
     * Retrieves all currently borrowed books for a specific member.
     * @param memberId The ID of the member.
     * @return A list of BorrowingTransaction objects for currently borrowed books.
     */
    public List<BorrowingTransaction> getBorrowedBooksByMember(Long memberId) {
        return transactionRepository.findByMemberMemberIdAndStatus(memberId, "BORROWED");
    }

    /**
     * Retrieves a specific borrowing transaction by its ID.
     * @param transactionId The ID of the transaction.
     * @return An Optional containing the transaction if found.
     */
    public Optional<BorrowingTransaction> getBorrowingTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    /**
     * Checks if a book is overdue for a given transaction and calculates overdue days.
     * Also triggers fine creation if overdue.
     * @param transactionId The ID of the borrowing transaction.
     * @return An Optional containing the number of overdue days, or empty if not overdue or transaction not found/returned.
     */
    @Transactional
    public Optional<Long> checkOverdueAndGetDays(Long transactionId) {
        Optional<BorrowingTransaction> transactionOptional = transactionRepository.findById(transactionId);
        if (transactionOptional.isPresent()) {
            BorrowingTransaction transaction = transactionOptional.get();
            if ("BORROWED".equalsIgnoreCase(transaction.getStatus()) && LocalDate.now().isAfter(transaction.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now());
                LocalDate originalReturnDate = transaction.getReturnDate();
                transaction.setReturnDate(LocalDate.now());
                fineService.createOrUpdateFine(transaction);
                transaction.setReturnDate(originalReturnDate);
                return Optional.of(overdueDays);
            }
        }
        return Optional.empty();
    }

    // ADDED: Method to get overdue days (called from controller)
    public Long getOverdueDays(Long transactionId) {
        return checkOverdueAndGetDays(transactionId).orElse(0L);
    }

    // ADDED: Method to check if overdue (called from controller)
    public boolean isOverdue(Long transactionId) {
        return getOverdueDays(transactionId) > 0;
    }


    /**
     * Scheduled task to update overdue statuses for transactions daily.
     * This ensures the 'OVERDUE' status is reflected in the database.
     * Also creates overdue notifications.
     * Runs every day at midnight (00:00).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateOverdueStatusesAndNotify() {
        List<BorrowingTransaction> borrowedTransactions = transactionRepository.findByStatus("BORROWED");
        LocalDate today = LocalDate.now();

        for (BorrowingTransaction transaction : borrowedTransactions) {
            if (today.isAfter(transaction.getDueDate())) {
                if ("BORROWED".equalsIgnoreCase(transaction.getStatus())) {
                    transaction.setStatus("OVERDUE");
                    transactionRepository.save(transaction);
                    System.out.println("Transaction " + transaction.getTransactionId() + " for Book " + transaction.getBook().getTitle() + " (Member " + transaction.getMember().getName() + ") set to OVERDUE.");

                    notificationService.createOverdueBookNotification(
                        transaction.getMember().getMemberId(),
                        transaction.getBook().getTitle(),
                        transaction.getBook().getBookId()
                    );
                }
            }
        }
    }

    /**
     * Scheduled task to send due date reminders.
     * Runs daily at 01:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void sendDueDateReminders() {
        List<BorrowingTransaction> borrowedTransactions = transactionRepository.findByStatus("BORROWED");
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE_DUE);

        for (BorrowingTransaction transaction : borrowedTransactions) {
            if (transaction.getDueDate().isEqual(reminderDate)) {
                notificationService.createDueDateReminderNotification(
                    transaction.getMember().getMemberId(),
                    transaction.getBook().getTitle(),
                    transaction.getTransactionId(),
                    transaction.getDueDate()
                );
            }
        }
    }
}
