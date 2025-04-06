package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.response.ResultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerGlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultResponse<String> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResultResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
}