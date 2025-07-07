package com.sims.handle;

import com.sims.handle.Exception.CourseException;
import com.sims.handle.Exception.DataBaseException;
import com.sims.handle.Exception.LoginException;
import com.sims.handle.Exception.RegisterException;
import com.sims.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.CommunicationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler
    public Result<Void> ExceptionHandler(CommunicationException e) {
        return Result.error("501", e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(CourseException e) {
        return Result.error("502", e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(DataBaseException e) {
        return Result.error("503", e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(LoginException e) {
        return Result.error("504", e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(RegisterException e) {
        return Result.error("505", e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(IllegalAccessException e) {
        return Result.error("506",e.getMessage());
    }

    @ExceptionHandler
    public Result<Void> ExceptionHandler(Exception e) {
        log.error(e.getMessage());
        return Result.error(e.getMessage());
    }

}
