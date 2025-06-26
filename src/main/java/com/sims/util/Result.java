package com.sims.util;

import lombok.Data;

@Data
public class Result<T> {
    private String code;
    private String message;
    private T data;

    public static Result<Void> ok() {
        Result<Void> result = new Result<>();
        result.code = "200";
        result.message = "操作成功";
        return result;
    }

    public static<T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = "200";
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static Result<Void> error() {
        Result<Void> result = new Result<>();
        result.code = "500";
        result.message = "操作失败";
        return result;
    }

    public static Result<Void> error(String message) {
        Result<Void> result = new Result<>();
        result.code = "500";
        result.message = message;
        return result;
    }

    public static Result<Void> error(String code, String message) {
        Result<Void> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
