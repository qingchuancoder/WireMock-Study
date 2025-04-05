package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.User;
import org.example.response.ResultResponse;
import org.example.service.UserRemoteService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserRemoteService userRemoteService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> createUser() {
        var user = User.builder().name("test").age(18).build();
        return userRemoteService.createUser(user);
    }

    @GetMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> retrieveUser() {
        return userRemoteService.retrieveUser(1L);
    }

    @PutMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> updateUser() {
        var user = User.builder().id(1L).build();
        return userRemoteService.updateUser(user);
    }

    @DeleteMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<Void>> deleteUser() {
        return userRemoteService.deleteUser(1L);
    }

    @GetMapping(value = "users", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<List<User>>> extractUsers() throws IOException {
        var userList = new ArrayList<User>();
        try (Response response = userRemoteService.extractUsers()) {
            var inputStream = response.body().asInputStream();
            var reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
                var user = objectMapper.readValue(line, User.class);
                userList.add(user);
            }
        }
        return ResponseEntity.ok(ResultResponse.success(userList));
    }
}