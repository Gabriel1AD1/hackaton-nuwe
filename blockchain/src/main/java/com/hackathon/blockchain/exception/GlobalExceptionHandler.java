package com.hackathon.blockchain.exception;

import com.hackathon.blockchain.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ApiResponse response = ApiResponse.notFound(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EntityAlreadyException.class)
    public ResponseEntity<ApiResponse> handleEntityAlreadyExists(EntityAlreadyException ex) {
        ApiResponse response = ApiResponse.conflict(ex.getMessage(), List.of()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handle(UnauthorizedException ex) {
        ApiResponse response = ApiResponse.unauthorized(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {
        ApiResponse response = ApiResponse.internalServerError("Ha ocurrido un error interno: " + ex.getMessage()).build();
        return ResponseEntity.internalServerError().body(response);
    }

}
