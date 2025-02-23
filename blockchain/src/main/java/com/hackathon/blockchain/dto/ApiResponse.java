package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 🔥 Ignora campos nulos
public class ApiResponse {

    @JsonProperty("http_status")
    private HttpStatus httpStatus;

    @JsonProperty("code_status")
    private Long codeStatus;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ApiResponseBuilder badRequest(String message) {
        return createResponse(HttpStatus.BAD_REQUEST, 400L, message, List.of());
    }

    public static ApiResponseBuilder unauthorized(String message) {
        return createResponse(HttpStatus.UNAUTHORIZED, 401L, message, List.of());
    }
    public static ApiResponseBuilder loginFailed() {
        return createResponse(HttpStatus.BAD_REQUEST, 401L, "❌ Invalid credentials", List.of());
    }
    public static ApiResponseBuilder methodNotAllowed(String method, List<String> supportedMethods) {
        String message = String.format("El método %s no está permitido. Métodos soportados: %s", method, String.join(", ", supportedMethods));
        return createResponse(HttpStatus.METHOD_NOT_ALLOWED, 405L, message, supportedMethods);
    }
    public static ApiResponseBuilder forbidden(String message) {
        return createResponse(HttpStatus.FORBIDDEN, 403L, message, List.of());
    }

    public static ApiResponseBuilder notFound(String message) {
        return createResponse(HttpStatus.NOT_FOUND, 404L, message, List.of());
    }

    public static ApiResponseBuilder conflict(String message, List<String> errors) {
        return createResponse(HttpStatus.CONFLICT, 409L, message, errors);
    }

    public static ApiResponseBuilder internalServerError(String message) {
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, 500L, message, List.of());
    }
    public static ApiResponseBuilder failedRequestMessage(String message) {
        return createResponseSingleMessage(message);
    }
    private static ApiResponseBuilder createResponse(HttpStatus httpStatus, Long codeStatus, String message, List<String> errors) {
        return ApiResponse.builder()
                .httpStatus(httpStatus)
                .codeStatus(codeStatus)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now());
    }
    private static ApiResponseBuilder createResponseSingleMessage(String message) {
        return ApiResponse.builder()
                .message(message);
    }


}
