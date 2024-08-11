package com.example.harbor_employee.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;


@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> entityNotFoundHandler(EntityNotFoundException e) {
        log.error("Handler EntityNotFoundException message : " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> IllegalArguHandler(IllegalArgumentException e) {
        log.error("Handler IllegalArgumentException message : " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> IOExceptionArguHandler(IOException e) {
        log.error("Handler IOException message : " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> NoSuchElementHandler(NoSuchElementException e) {
        log.error("Handler IOException message : " + e.getMessage());
        return ErrorResponseDto.makeMessage(HttpStatus.BAD_REQUEST, e.getMessage());
    }

}
