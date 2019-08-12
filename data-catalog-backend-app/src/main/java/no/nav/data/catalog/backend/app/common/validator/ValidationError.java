package no.nav.data.catalog.backend.app.common.validator;

public class ValidationError {
    private String reference;
    private String errorType;
    private String errorMessage;

    public ValidationError(String reference, String errorType, String errorMessage) {
        this.reference = reference;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public String toErrorString() {
        return String.format("%s -- %s -- %s", reference, errorType, errorMessage);
    }
}
