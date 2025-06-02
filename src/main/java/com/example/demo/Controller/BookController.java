package com.example.demo.Controller;

import com.example.demo.Entity.Book;
import com.example.demo.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/books") // Base path for all endpoints in this controller
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500"}) // Allow requests from your frontend origins
public class BookController {

    @Autowired // Injects the BookService dependency
    private BookService bookService;

    /**
     * GET /api/books
     * Retrieves all books.
     * @return A list of all books.
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    /**
     * GET /api/books/{id}
     * Retrieves a book by its ID.
     * @param id The ID of the book.
     * @return The book if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /api/books
     * Adds a new book.
     * @param book The book object to add.
     * @return The created book with HTTP status 201 Created.
     */
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book newBook = bookService.addBook(book);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    /**
     * PUT /api/books/{id}
     * Updates an existing book.
     * @param id The ID of the book to update.
     * @param bookDetails The updated book details.
     * @return The updated book with HTTP status 200 OK, or 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Optional<Book> updatedBook = bookService.updateBook(id, bookDetails);
        return updatedBook.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE /api/books/{id}
     * Deletes a book by its ID.
     * @param id The ID of the book to delete.
     * @return HTTP status 204 No Content if deleted, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        boolean deleted = bookService.deleteBook(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * PATCH /api/books/{id}/copies
     * Updates the available copies of a book.
     * @param id The ID of the book.
     * @param payload A map containing the "change" in copies (e.g., {"change": 1} or {"change": -1}).
     * @return The updated book with HTTP status 200 OK, or 400 Bad Request/404 Not Found.
     */
    @PatchMapping("/{id}/copies")
    public ResponseEntity<Book> updateBookCopies(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer change = payload.get("change");
        if (change == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Book> updatedBook = bookService.updateBookCopies(id, change);
        return updatedBook.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST)); // Bad request if copies go negative or book not found
    }

    /**
     * GET /api/books/search?query={searchTerm}
     * Searches for books by title or author.
     * @param query The search term.
     * @return A list of books matching the query.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        List<Book> books = bookService.searchBooks(query);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
}
