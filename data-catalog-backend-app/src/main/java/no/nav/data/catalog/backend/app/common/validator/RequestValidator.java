package no.nav.data.catalog.backend.app.common.validator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.common.exceptions.ValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class RequestValidator<T extends RequestItem> {
    private RequestError requestErrors;
    private List<T> listOfRequests;

    public RequestValidator(List<T> listOfRequests) {
        this.requestErrors = new RequestError();
        this.listOfRequests = listOfRequests;
    }

    public void validateListNotNullOrEmpty() {
        if (listOfRequests == null || listOfRequests.isEmpty()) {
            log.error("The request was not accepted because it is null or empty");
            throw new ValidationException("The request was not accepted because it is null or empty");
        }
    }

    public void validateNoDuplicatesInRequest() {
        if (duplicatesInRequest()) {
            recordDuplicatesInRequest();
        }
    }

    private boolean duplicatesInRequest() {
        Set requestSet = Set.copyOf(listOfRequests);
        return requestSet.size() < listOfRequests.size();
    }

    private void recordDuplicatesInRequest() {
        Map<String, Integer> itemIdentifierToRequestIndex = new HashMap<>();

        AtomicInteger requestIndex = new AtomicInteger();
        listOfRequests.forEach(request -> {
            requestIndex.incrementAndGet();
            String itemIdentifier = request.identifyingFields();
            if (itemIdentifierToRequestIndex.containsKey(itemIdentifier)) {
                requestErrors.addError(new ValidationError("Request:" + requestIndex, "DuplicateError",
                        String.format("The %s %s is not unique because it has already been used in this request (see request:%s)",
                                request.getItemType(), itemIdentifier, itemIdentifierToRequestIndex.get(itemIdentifier)
                        )));
            } else {
                itemIdentifierToRequestIndex.put(itemIdentifier, requestIndex.intValue());
            }
        });
    }

    public void wrapUpValidation() {
        if (requestErrors.hasErrors()) {
            log.error("The request was not accepted. The following errors occurred during validation: {}", requestErrors);
            throw new ValidationException(requestErrors, "The request was not accepted. The following errors occurred during validation: ");
        }
    }
}
