package com.example.demo.Service;

import com.example.demo.Entity.Book;
import com.example.demo.Repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For transactional operations

import java.util.List;
import java.util.Optional;

@Service // Marks this class as a Spring service component
public class BookService {

    @Autowired // Injects the BookRepository dependency
    private BookRepository bookRepository;

    /**
     * Retrieves all books from the database.
     * @return A list of all books.
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Retrieves a book by its ID.
     * @param id The ID of the book to retrieve.
     * @return An Optional containing the book if found, or empty if not found.
     */
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Adds a new book to the database.
     * @param book The book object to be added.
     * @return The saved book object.
     */
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Updates an existing book.
     * @param id The ID of the book to update.
     * @param bookDetails The updated book details.
     * @return An Optional containing the updated book if found, or empty if not found.
     */
    @Transactional // Ensures the entire method runs within a single transaction
    public Optional<Book> updateBook(Long id, Book bookDetails) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    // Update fields of the existing book
                    existingBook.setTitle(bookDetails.getTitle());
                    existingBook.setAuthor(bookDetails.getAuthor());
                    existingBook.setGenre(bookDetails.getGenre());
                    existingBook.setIsbn(bookDetails.getIsbn());
                    existingBook.setYearPublished(bookDetails.getYearPublished());
                    existingBook.setAvailableCopies(bookDetails.getAvailableCopies());
                    return bookRepository.save(existingBook); // Save and return the updated book
                });
    }

    /**
     * Deletes a book by its ID.
     * @param id The ID of the book to delete.
     * @return True if the book was deleted, false otherwise.
     */
    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Updates the number of available copies for a book.
     * @param id The ID of the book to update.
     * @param change The amount to change the copies by (positive for add, negative for remove).
     * @return An Optional containing the updated book if found, or empty if not found.
     */
    @Transactional
    public Optional<Book> updateBookCopies(Long id, int change) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    int currentCopies = existingBook.getAvailableCopies();
                    int newCopies = currentCopies + change;
                    if (newCopies >= 0) { // Ensure copies don't go below zero
                        existingBook.setAvailableCopies(newCopies);
                        return bookRepository.save(existingBook);
                    } else {
                        // Optionally throw an exception or return empty if operation would result in negative copies
                        return null; // Or throw new IllegalArgumentException("Cannot have negative copies");
                    }
                });
    }

    /**
     * Searches for books by title or author.
     * @param query The search term.
     * @return A list of books matching the query.
     */
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }
}
