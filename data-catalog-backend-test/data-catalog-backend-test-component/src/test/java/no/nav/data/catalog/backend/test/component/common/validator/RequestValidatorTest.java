package no.nav.data.catalog.backend.test.component.common.validator;

import no.nav.data.catalog.backend.app.codelist.CodelistRequest;
import no.nav.data.catalog.backend.app.common.exceptions.ValidationException;
import no.nav.data.catalog.backend.app.common.validator.RequestValidator;
import no.nav.data.catalog.backend.test.component.codelist.CodelistStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorTest {

    private RequestValidator<CodelistRequest> requestValidator;

    @Before
    public void setUp() {
        CodelistStub.initializeCodelistAndStub();
    }

    @Test
    public void validateListNotNullOrEmpty_shouldThrowValidationException_whenListOfRequestsIsNull() {
        requestValidator = new RequestValidator<>(null);
        try {
            requestValidator.validateListNotNullOrEmpty();
        } catch (ValidationException e) {
            assertThat(e.getLocalizedMessage(), is("The request was not accepted because it is null or empty"));
        }
    }

    @Test
    public void validateListNotNullOrEmpty_shouldThrowValidationException_withEmptyListOfRequests() {
        List<CodelistRequest> requests = Collections.emptyList();
        requestValidator = new RequestValidator<>(requests);
        try {
            requestValidator.validateListNotNullOrEmpty();
        } catch (ValidationException e) {
            assertThat(e.getLocalizedMessage(), is("The request was not accepted because it is null or empty"));
        }
    }

    @Test
    public void validateNoDuplicatesInRequest_shouldThrowValidationException_withDuplicatedItemInRequest() {
        List<CodelistRequest> requests = new ArrayList<>();
        requests.add(CodelistRequest.builder()
                .listName("PRODUCER")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("SYSTEM")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("CATEGORY")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("PRODUCER")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requestValidator = new RequestValidator<>(requests);

        try {
            requestValidator.validateNoDuplicatesInRequest();
        } catch (ValidationException e) {
            assertThat(e.get().size(), is(1));
            assertThat(e.get()
                    .toErrorString(), is("Request:4 -- Duplicate -- The codelist PRODUCER-TEST is not unique because it has already been used in this request (see request:1)"));
        }
    }

    @Test
    public void wrapUpValidation_shouldValidateWithoutAnyErrors() {
        List<CodelistRequest> requests = new ArrayList<>();
        requests.add(CodelistRequest.builder()
                .listName("PRODUCER")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("SYSTEM")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("CATEGORY")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());

        requestValidator = new RequestValidator<>(requests);
        requestValidator.validateListNotNullOrEmpty();
        requestValidator.validateNoDuplicatesInRequest();
        requestValidator.wrapUpValidation();
    }

    @Test
    public void wrapUpValidation__shouldThrowValidationExceptionAndStoreErrorWithAnRequestIndex() {
        List<CodelistRequest> requests = new ArrayList<>();
        requests.add(CodelistRequest.builder()
                .listName("PRODUCER")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("SYSTEM")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("CATEGORY")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requests.add(CodelistRequest.builder()
                .listName("PRODUCER")
                .code("TEST")
                .description("Informasjon oppgitt av tester")
                .build());
        requestValidator = new RequestValidator<>(requests);

        try {
            requestValidator.validateNoDuplicatesInRequest();
            requestValidator.wrapUpValidation();
        } catch (ValidationException e) {
            assertThat(e.get().size(), is(1));
            assertThat(e.get()
                    .toErrorString(), is("Request:4 -- Duplicate -- The codelist PRODUCER-TEST is not unique because it has already been used in this request (see request:1)"));
            assertThat(e.getLocalizedMessage(), containsString("The request was not accepted. The following errors occurred during validation: "));
        }
    }
}
