package com.githubzs.plataforma_reservas_medicas.exception;

import java.util.List;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError;

public class ValidationException extends BusinessException {

    private final List<ApiError.FieldViolation> violations;

    public ValidationException(String message, List<ApiError.FieldViolation> violations) {
        super(message);
        this.violations = violations;
    }
    
    public List<ApiError.FieldViolation> getViolations() {
        return violations;
    }
    
}
