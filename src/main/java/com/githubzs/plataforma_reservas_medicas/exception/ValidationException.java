package com.githubzs.plataforma_reservas_medicas.exception;

import java.util.List;
import com.githubzs.plataforma_reservas_medicas.api.error.ErrorResponse;

public class ValidationException extends BusinessException {

    private final List<ErrorResponse.FieldViolation> violations;

    public ValidationException(String message, List<ErrorResponse.FieldViolation> violations) {
        super(message);
        this.violations = violations;
    }
    
    public List<ErrorResponse.FieldViolation> getViolations() {
        return violations;
    }
    
}
