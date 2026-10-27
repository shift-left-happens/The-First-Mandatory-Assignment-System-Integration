package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tBook")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nBookID")
    private Long id;

    @Column(name = "cTitle")
    private String title;

    @Column(name = "nAuthorID")
    private Long authorId;

    @Column(name = "nPublishingCompanyID")
    private Long publishingCompanyId;

    @Column(name = "nPublishingYear")
    private Integer publishingYear;

    public Book() {
    }

    public Book(
            String title,
            Long authorId,
            Long publishingCompanyId,
            Integer publishingYear) {

        this.title = title;
        this.authorId = authorId;
        this.publishingCompanyId = publishingCompanyId;
        this.publishingYear = publishingYear;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getPublishingCompanyId() {
        return publishingCompanyId;
    }

    public void setPublishingCompanyId(Long publishingCompanyId) {
        this.publishingCompanyId = publishingCompanyId;
    }

    public Integer getPublishingYear() {
        return publishingYear;
    }

    public void setPublishingYear(Integer publishingYear) {
        this.publishingYear = publishingYear;
    }
}