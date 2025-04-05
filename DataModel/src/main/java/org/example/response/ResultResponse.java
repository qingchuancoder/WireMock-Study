package org.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@SuppressWarnings("unused")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse<T> implements Serializable {
    private Boolean success;
    private Integer code;
    private String msg;
    private T data;

    public static <T> ResultResponse<T> success(Integer code, String msg, T data) {
        return new ResultResponse<>(true, code, msg, data);
    }

    public static <T> ResultResponse<T> success(String msg, T data) {
        return success(200, msg, data);
    }

    public static <T> ResultResponse<T> success(String msg) {
        return success(200, msg, null);
    }

    public static <T> ResultResponse<T> success(T data) {
        return success(200, "Success", data);
    }

    public static <T> ResultResponse<T> success() {
        return success(200, "Success", null);
    }

    public static <T> ResultResponse<T> fail(Integer code, String msg, T data) {
        return new ResultResponse<>(false, code, msg, data);
    }

    public static <T> ResultResponse<T> fail(Integer code, String msg) {
        return fail(code, msg, null);
    }

    public static <T> ResultResponse<T> fail(String msg, T data) {
        return fail(500, msg, data);
    }

    public static <T> ResultResponse<T> fail(String msg) {
        return fail(500, msg, null);
    }
}