package org.example.service;

import feign.Response;
import org.example.data.User;
import org.example.response.ResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Service
@FeignClient(name = "user-service", path = "/v1")
public interface UserRemoteService {

    @PostMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> createUser(@RequestBody User user);

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> retrieveUser(@RequestParam Long id);

    @PutMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<User>> updateUser(@RequestBody User user);

    @DeleteMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ResultResponse<Void>> deleteUser(@RequestParam Long id);

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    Response extractUsers();
}