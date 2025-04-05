package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.data.User;
import org.example.response.ResultResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;

@RestController
@RequestMapping("/v1")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final ObjectMapper objectMapper;

    @PostMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> create(@RequestBody @Validated User user) {
        log.info("create user: {}", user);
        var data = user.setId(1L);
        return ResponseEntity.ok(ResultResponse.success(data));
    }

    @GetMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> retrieve(@RequestParam Long id) {
        log.info("get user info, id: {}", id);
        var user = User.builder().id(1L).name("test").age(18).build();
        return ResponseEntity.ok(ResultResponse.success(user));
    }

    @PutMapping(value = "user", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<User>> update(@RequestBody @Validated User user) {
        log.info("update user info, user: {}", user);
        return ResponseEntity.ok(ResultResponse.success(user.setId(1L).setName("updated")));
    }

    @DeleteMapping(value = "user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResultResponse<Void>> delete(@RequestParam Long id) {
        log.info("delete user, id: {}", id);
        return ResponseEntity.ok(ResultResponse.success());
    }

    @GetMapping(value = "users", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StreamingResponseBody> extract() {
        log.info("extract user info");
        var users = new ArrayList<User>();
        for (int i = 1; i <= 18; i++) {
            var user = User.builder().id((long) i).name("test" + i).age(i).build();
            users.add(user);
        }
        StreamingResponseBody streamingResponseBody = outputStream -> {
            for (User user : users) {
                outputStream.write(objectMapper.writeValueAsBytes(user));
                outputStream.write("\n".getBytes());
            }
        };
        return ResponseEntity.ok(streamingResponseBody);
    }
}