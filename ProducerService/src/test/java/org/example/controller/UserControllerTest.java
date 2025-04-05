package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createShouldSuccess() throws Exception {
        User user = User.builder().name("test").age(18).build();

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.age").value(18));
    }

    @Test
    public void retrieveShouldSuccess() throws Exception {
        mockMvc.perform(get("/v1/user")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.age").value(18));
    }

    @Test
    public void updateShouldSuccess() throws Exception {
        User user = new User(1L, "test", 18);

        mockMvc.perform(put("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("updated"))
                .andExpect(jsonPath("$.data.age").value(18));
    }

    @Test
    public void deleteShouldSuccess() throws Exception {
        mockMvc.perform(delete("/v1/user")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void extractShouldUsers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MvcResult::getAsyncResult)
                .andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString).isEqualTo("""
                {"id":1,"name":"test1","age":1}
                {"id":2,"name":"test2","age":2}
                {"id":3,"name":"test3","age":3}
                {"id":4,"name":"test4","age":4}
                {"id":5,"name":"test5","age":5}
                {"id":6,"name":"test6","age":6}
                {"id":7,"name":"test7","age":7}
                {"id":8,"name":"test8","age":8}
                {"id":9,"name":"test9","age":9}
                {"id":10,"name":"test10","age":10}
                {"id":11,"name":"test11","age":11}
                {"id":12,"name":"test12","age":12}
                {"id":13,"name":"test13","age":13}
                {"id":14,"name":"test14","age":14}
                {"id":15,"name":"test15","age":15}
                {"id":16,"name":"test16","age":16}
                {"id":17,"name":"test17","age":17}
                {"id":18,"name":"test18","age":18}
                """);
    }
}