package com.example.demo;

import com.example.demo.repository.BookRepository;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class ApiNavigationTests {
    @Autowired WebApplicationContext context;
    @Autowired BookRepository books;
    @Autowired AuthorRepository authors;
    @Autowired PublisherRepository publishers;

    @Test
    void rootExposesCollections() throws Exception {
        MockMvc mvc = webAppContextSetup(context).build();
        mvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.books.href", containsString("/books")))
                .andExpect(jsonPath("$._links.authors.href", containsString("/authors")))
                .andExpect(jsonPath("$._links.publishers.href", containsString("/publishers")));
    }

    @Test
    void finalBookPageHasNoNextLink() throws Exception {
        MockMvc mvc = webAppContextSetup(context).build();
        long count = books.count();
        mvc.perform(get("/books").param("limit", "1").param("offset", Long.toString(count)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.next").doesNotExist());
    }

    @Test
    void missingUpdateReturnsNotFound() throws Exception {
        MockMvc mvc = webAppContextSetup(context).build();
        mvc.perform(put("/books/999999999").contentType("application/json")
                        .content("{\"title\":\"Unknown\",\"authorId\":1,\"publishingCompanyId\":1,\"publishingYear\":2024}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void authorAndPublisherExposeTheirBookCollections() throws Exception {
        MockMvc mvc = webAppContextSetup(context).build();
        authors.findAll().stream().findFirst().ifPresent(author -> {
            try {
                mvc.perform(get("/authors/{id}", author.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._links.books.href",
                                containsString("/authors/" + author.getId() + "/books")));
                mvc.perform(get("/authors/{id}/books", author.getId()))
                        .andExpect(status().isOk());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        publishers.findAll().stream().findFirst().ifPresent(publisher -> {
            try {
                mvc.perform(get("/publishers/{id}", publisher.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._links.books.href",
                                containsString("/publishers/" + publisher.getId() + "/books")));
                mvc.perform(get("/publishers/{id}/books", publisher.getId()))
                        .andExpect(status().isOk());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    @Transactional
    void creationReturnsLocationAndLinkedResource() throws Exception {
        MockMvc mvc = webAppContextSetup(context).build();
        mvc.perform(post("/publishers").contentType("application/json")
                        .content("{\"name\":\"Navigation Test Publisher\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/publishers/")))
                .andExpect(jsonPath("$._links.self.href", containsString("/publishers/")));
    }
}
