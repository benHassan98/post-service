package com.odinbook.postservice;

import com.odinbook.postservice.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class PostTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    public void beforeEach(){
        postRepository.deleteAll();
        testUtils.deleteAccounts();
    }
    @AfterEach
    public void afterEach(){
        postRepository.deleteAll();
        testUtils.deleteAccounts();
    }








}
