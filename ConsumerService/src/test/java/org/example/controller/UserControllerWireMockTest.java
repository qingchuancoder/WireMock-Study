package org.example.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.data.User;
import org.example.response.ResultResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock
class UserControllerWireMockMappingTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createUserShouldSuccess() throws Exception {
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
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
    public void retrieveUserShouldSuccess() throws Exception {
        mockMvc.perform(get("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
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
    public void updateUserShouldSuccess() throws Exception {
        mockMvc.perform(put("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("updated"));
    }

    @Test
    public void deleteUserShouldSuccess() throws Exception {
        mockMvc.perform(delete("/v1/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void extractUsersShouldSuccess() throws Exception {
        var users = new ArrayList<User>();
        for (int i = 1; i <= 18; i++) {
            User user = User.builder().id((long) i).name("test" + i).age(i).build();
            users.add(user);
        }
        var mvcResult = mockMvc.perform(get("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(ResultResponse.success(users)));
    }
}

@SuppressWarnings("SpellCheckingInspection")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock
class UserControllerWireMockProgrammingTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SuppressWarnings("unused")
    @InjectWireMock
    private WireMockServer wireMock;
    @Value("${wiremock.server.baseUrl}")
    private String wiremockBaseUrl;


    @Test
    public void createUserShouldSuccess() throws Exception {
        User user = User.builder().name("test").age(18).build();
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": true,
                                    "code": 200,
                                    "msg": "Success",
                                    "data": {
                                        "id": 1,
                                        "name": "test",
                                        "age": 18
                                    }
                                }
                                """)));
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.age").value(18));
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    void createUserShouldFailedWhenRemoteServiceNotAvailable() throws Exception {
        User user = User.builder().name("test").age(18).build();
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                     "success": false,
                                     "code": 500,
                                     "msg": "Connection refused: getsockopt executing POST ${wiremockBaseUrl}/v1/user",
                                     "data": null
                                 }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [POST] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#createUser(User)]: [{
                             "success": false,
                             "code": 500,
                             "msg": "Connection refused: getsockopt executing POST ${wiremockBaseUrl}/v1/user",
                             "data": null
                         }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    void createUserShouldFailedWhenRemoteServiceOccurredError() throws Exception {
        User user = User.builder().name("test").age(18).build();
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "[500] during [POST] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#createUser(User)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [POST] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#createUser(User)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "[500] during [POST] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#createUser(User)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    public void retrieveUserShouldSuccess() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": true,
                                    "code": 200,
                                    "msg": "Success",
                                    "data": {
                                        "id": 1,
                                        "name": "test",
                                        "age": 18
                                    }
                                }
                                """)));
        mockMvc.perform(get("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.age").value(18));
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void retrieveUserShouldFailedWhenRemoteServiceNotAvailable() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "Connection refused: getsockopt executing GET ${wiremockBaseUrl}/v1/user?id=1",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(get("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [GET] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#retrieveUser(Long)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "Connection refused: getsockopt executing GET ${wiremockBaseUrl}/v1/user?id=1",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void retrieveUserShouldFailedWhenRemoteServiceOccurredError() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "[500] during [GET] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#retrieveUser(Long)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(get("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [GET] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#retrieveUser(Long)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "[500] during [GET] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#retrieveUser(Long)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void updateUserShouldSuccess() throws Exception {
        User user = User.builder().id(1L).build();
        wireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.ok("""
                                {
                                    "success": true,
                                    "code": 200,
                                    "msg": "Success",
                                    "data": {
                                        "id": 1,
                                        "name": "updated",
                                        "age": null
                                    }
                                }
                                """)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
        mockMvc.perform(put("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("updated"));
        wireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    public void updateUserWhenRemoteServiceNotAvailable() throws Exception {
        User user = User.builder().id(1L).build();
        wireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "Connection refused: getsockopt executing PUT ${wiremockBaseUrl}/v1/user",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(put("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [PUT] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#updateUser(User)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "Connection refused: getsockopt executing PUT ${wiremockBaseUrl}/v1/user",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    public void updateUserWhenRemoteServiceOccurredError() throws Exception {
        User user = User.builder().id(1L).build();
        wireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user)))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "[500] during [PUT] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#updateUser(User)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(put("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [PUT] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#updateUser(User)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "[500] during [PUT] to [${wiremockBaseUrl}/v1/user] [UserRemoteService#updateUser(User)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/v1/user"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(user))));
    }

    @Test
    public void deleteUserShouldSuccess() throws Exception {
        wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.okJson("""
                        {
                            "success": true,
                            "code": 200,
                            "msg": "Success",
                            "data": null
                        }
                        """)));
        mockMvc.perform(delete("/v1/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void deleteUserWhenRemoteServiceNotAvailable() throws Exception {
        wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                     "success": false,
                                     "code": 500,
                                     "msg": "Connection refused: getsockopt executing DELETE ${wiremockBaseUrl}/v1/user?id=1",
                                     "data": null
                                 }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(delete("/v1/user"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [DELETE] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#deleteUser(Long)]: [{
                             "success": false,
                             "code": 500,
                             "msg": "Connection refused: getsockopt executing DELETE ${wiremockBaseUrl}/v1/user?id=1",
                             "data": null
                         }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void deleteUserWhenRemoteServiceOccurredError() throws Exception {
        wireMock.stubFor(WireMock.delete(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1"))
                .willReturn(WireMock.serverError()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "success": false,
                                    "code": 500,
                                    "msg": "[500] during [DELETE] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#deleteUser(Long)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                                    "data": null
                                }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(delete("/v1/user"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("""
                        [500 Server Error] during [DELETE] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#deleteUser(Long)]: [{
                            "success": false,
                            "code": 500,
                            "msg": "[500] during [DELETE] to [${wiremockBaseUrl}/v1/user?id=1] [UserRemoteService#deleteUser(Long)]: [{"success":false,"code":500,"msg":"Internal Server Error","data":null}]",
                            "data": null
                        }
                        ]""".replace("${wiremockBaseUrl}", wiremockBaseUrl)))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/user"))
                .withQueryParam("id", WireMock.equalTo("1")));
    }

    @Test
    public void extractUsersShouldSuccess() throws Exception {
        var users = new ArrayList<User>();
        for (int i = 1; i <= 18; i++) {
            User user = User.builder().id((long) i).name("test" + i).age(i).build();
            users.add(user);
        }
        wireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/users"))
                .willReturn(WireMock.okForContentType(MediaType.APPLICATION_JSON_VALUE, """
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
                        """)));
        var mvcResult = mockMvc.perform(get("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(ResultResponse.success(users)));
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/users")));
    }

    @Test
    public void extractUsersShouldFailedWhenRemoteServiceNotAvailable() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/users"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                     "success": false,
                                     "code": 500,
                                     "msg": "Connection refused: getsockopt executing POST ${wiremockBaseUrl}/v1/users",
                                     "data": null
                                 }
                                """.replace("${wiremockBaseUrl}", wiremockBaseUrl))));
        mockMvc.perform(get("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/users")));
    }

    @Test
    public void extractUsersShouldFailedWhenRemoteServiceOccurredError() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/v1/users"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "timestamp": "2025-04-06T11:50:40.070+00:00",
                                  "status": 500,
                                  "error": "Internal Server Error",
                                  "path": "/v1/users"
                                }
                                """)));
        mockMvc.perform(get("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.data").doesNotExist());
        wireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/users")));
    }
}