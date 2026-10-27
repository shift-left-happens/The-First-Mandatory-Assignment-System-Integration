
package com.example.demo.repository;

import com.example.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query(
        value = "SELECT * FROM tbook ORDER BY nBookID LIMIT :limit OFFSET :offset",
        nativeQuery = true
    )
    List<Book> findBooksWithPagination(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    boolean existsByAuthorId(Long authorId);

    boolean existsByPublishingCompanyId(Long publishingCompanyId);

    List<Book> findByAuthorId(Long authorId);

    List<Book> findByPublishingCompanyId(Long publishingCompanyId);
}
