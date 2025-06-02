package com.example.demo.Repository;

import com.example.demo.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Marks this interface as a Spring Data JPA repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // JpaRepository provides standard CRUD operations (save, findById, findAll, delete, etc.)

    // Custom query method to find books by genre (Spring Data JPA will implement this automatically)
    List<Book> findByGenre(String genre);

    // Custom query method to find books by title or author (case-insensitive)
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}
