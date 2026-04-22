package com.githubzs.plataforma_reservas_medicas.api.error;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.*;
import com.githubzs.plataforma_reservas_medicas.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        var body = ApiError.of(HttpStatus.NOT_FOUND, exception.getMessage(), request.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException exception, WebRequest request) {
        var body = ApiError.of(HttpStatus.CONFLICT, exception.getMessage(), request.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(ValidationException exception, WebRequest request) {
        var body = ApiError.of(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request.getDescription(false), exception.getViolations());
        return ResponseEntity.unprocessableContent().body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception, WebRequest request) {
        var body = ApiError.of(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getDescription(false), List.of());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        var violations = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage())).toList();
        var body = ApiError.of(HttpStatus.BAD_REQUEST,
                "Validation failed",
                req.getDescription(false),
                violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, WebRequest req) {
        var violations = ex.getConstraintViolations().stream()
                .map(cv -> new ApiError.FieldViolation(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        var body = ApiError.of(HttpStatus.BAD_REQUEST, "Constraint violation", req.getDescription(false), violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.CONFLICT, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
