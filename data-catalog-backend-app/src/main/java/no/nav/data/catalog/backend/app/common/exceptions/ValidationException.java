package no.nav.data.catalog.backend.app.common.exceptions;

import no.nav.data.catalog.backend.app.common.validator.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private final List<ValidationError> validationErrors;

    public ValidationException(String message) {
        this(Collections.emptyList(), message);
    }

    public ValidationException(List<ValidationError> validationErrors, String message) {
        super(message + " " + validationErrors);
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> get() {
        return validationErrors;
    }

    public String toErrorString() {
        return validationErrors.stream().map(ValidationError::toString).collect(Collectors.joining(", "));
    }
}