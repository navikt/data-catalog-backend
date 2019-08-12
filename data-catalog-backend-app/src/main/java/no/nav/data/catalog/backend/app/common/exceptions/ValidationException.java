package no.nav.data.catalog.backend.app.common.exceptions;

import no.nav.data.catalog.backend.app.common.validator.RequestError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private RequestError requestError;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(RequestError requestError) {
        this.requestError = requestError;
    }

    public ValidationException(RequestError requestError, String message) {
        super(message + " " + requestError);
        this.requestError = requestError;
    }

    public RequestError get() {
        return requestError;
    }
}
