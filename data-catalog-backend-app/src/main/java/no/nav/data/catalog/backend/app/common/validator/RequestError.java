package no.nav.data.catalog.backend.app.common.validator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RequestError {
    private List<ValidationError> validationErrors;

    public RequestError() {
        this.validationErrors = new ArrayList<>();
    }

    public void addError(ValidationError error) {
        validationErrors.add(error);
    }

    public String toErrorString() {
        return validationErrors.stream().map(ValidationError::toErrorString).collect(Collectors.joining());
    }

    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }

    public int size() {
        return validationErrors.size();
    }
}