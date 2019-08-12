package no.nav.data.catalog.backend.app.codelist;

import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.common.exceptions.CodelistNotFoundException;
import no.nav.data.catalog.backend.app.common.validator.RequestValidator;
import no.nav.data.catalog.backend.app.common.validator.ValidationError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

@Slf4j
@Service
public class CodelistService {

    private CodelistRepository repository;

    public static final Map<ListName, Map<String, String>> codelists = new EnumMap<>(ListName.class);

    public CodelistService(CodelistRepository repository) {
        this.repository = repository;
        initListNames();
    }

    @PostConstruct
    public void refreshCache() {
        List<Codelist> allCodelists = repository.findAll();
        initListNames();
        allCodelists.forEach(codelist -> codelists.get(codelist.getListName())
                .put(codelist.getCode(), codelist.getDescription()));
    }

    private void initListNames() {
        Stream.of(ListName.values()).forEach(listName -> codelists.put(listName, new HashMap<>()));
    }

    public List<Codelist> save(List<CodelistRequest> requests) {
        requests.forEach(request -> codelists.get(ListName.valueOf(request.getListName()))
                .put(request.getCode(), request.getDescription()));
        return repository.saveAll(requests.stream()
                .map(CodelistRequest::convert)
                .collect(Collectors.toList()));
    }

    public List<Codelist> update(List<CodelistRequest> requests) {
        requests.forEach(request -> codelists.get(ListName.valueOf(request.getListName()))
                .put(request.getCode(), request.getDescription()));
        return repository.saveAll(requests.stream()
                .map(this::updateDescriptionInRepository)
                .collect(Collectors.toList()));
    }

    private Codelist updateDescriptionInRepository(CodelistRequest request) {
        Optional<Codelist> optionalCodelist = repository.findByListNameAndCodeAsStrings(request.getListName(), request.getCode());
        if (optionalCodelist.isPresent()) {
            Codelist codelist = optionalCodelist.get();
            codelist.setDescription(request.getDescription());
            return codelist;
        }
        log.error("Cannot find codelist with code={} in list={}", request.getCode(), request.getListName());
        throw new CodelistNotFoundException(String.format(
                "Cannot find codelist with code=%s in list=%s", request.getCode(), request.getListName()));
    }

    public void delete(ListName name, String code) {
        Optional<Codelist> toDelete = repository.findByListAndCode(name, code);
        if (toDelete.isPresent()) {
            repository.delete(toDelete.get());
            codelists.get(name).remove(code);
        } else {
            log.error("Cannot find a codelist to delete with code={} and listName={}", code, name);
            throw new IllegalArgumentException(
                    String.format("Cannot find a codelist to delete with code=%s and listName=%s", code, name));
        }
    }

    public void validateListNameExists(String listName) {
        if (!listNameExists(listName)) {
            log.error("Codelist with listName={} does not exits", listName);
            throw new CodelistNotFoundException(String.format("Codelist with listName=%s does not exist", listName));
        }
    }

    public void validateListNameAndCodeExists(String listName, String code) {
        validateListNameExists(listName);
        if (!codelists.get(ListName.valueOf(listName.toUpperCase())).containsKey(code.toUpperCase())) {
            log.error("The code={} does not exist in the list={}.", code, listName);
            throw new CodelistNotFoundException(String.format("The code=%s does not exist in the list=%s.", code, listName));
        }
    }

    public boolean listNameExists(String listName) {
        Optional<ListName> optionalListName = Arrays.stream(ListName.values())
                .filter(x -> x.toString().equalsIgnoreCase(listName))
                .findFirst();
        return optionalListName.isPresent();
    }

    public void validate(List<CodelistRequest> listOfRequests, boolean isUpdate) {
        RequestValidator requestValidator = new RequestValidator<>(listOfRequests);

        requestValidator.validateListNotNullOrEmpty();
        requestValidator.validateNoDuplicatesInRequest();
        requestValidator.addErrorIfPresent(validateFields(listOfRequests, isUpdate));
        requestValidator.wrapUpValidation();

    }

    private List<ValidationError> validateFields(List<CodelistRequest> listOfRequests, boolean isUpdate) {
        AtomicInteger index = new AtomicInteger();
        List<ValidationError> validationErrors = new ArrayList<>();

        listOfRequests.forEach(request -> {
            index.incrementAndGet();
            String reference = "Request:" + index.toString();

            List<ValidationError> errorsInCurrentRequest = new ArrayList<>(request.validateThatNoFieldsAreNullOrEmpty(reference));

            if (errorsInCurrentRequest.isEmpty()) {  // to avoid NPE
                errorsInCurrentRequest.addAll(validateThatAllFieldsHaveValidValues(request, isUpdate, reference));
            }

            if (!errorsInCurrentRequest.isEmpty()) {
                validationErrors.addAll(errorsInCurrentRequest);
            }
        });
        return validationErrors;
    }

    private List<ValidationError> validateThatAllFieldsHaveValidValues(CodelistRequest request, boolean isUpdate, String reference) {
        request.toUpperCaseAndTrim();
        List<ValidationError> validationErrors = new ArrayList<>();

        if (!listNameExists(request.getListName())) {
            validationErrors.add(new ValidationError(reference, "nonExistingField",
                    String.format("Codelist with listName=%s does not exits", request.getListName())));
        } else {
            if (creatingExistingCode(request, isUpdate)) {
                validationErrors.add(new ValidationError(reference, "creatingExistingCode",
                        String.format("The code %s already exists in the codelist(%s) and therefore cannot be created", request.getCode(), request
                                .getListName())));
            } else if (updatingNonExistingCode(request, isUpdate)) {
                validationErrors.add(new ValidationError(reference, "updatingNonExistingCode",
                        String.format("The code %s does not exist in the codelist(%s) and therefore cannot be updated", request.getCode(), request
                                .getListName())));
            }
        }
        return validationErrors;
    }

    private boolean creatingExistingCode(CodelistRequest request, boolean isUpdate) {
        return !isUpdate && codelists.get(ListName.valueOf(request.getListName())).containsKey(request.getCode());
    }

    private boolean updatingNonExistingCode(CodelistRequest request, boolean isUpdate) {
        return isUpdate && codelists.get(ListName.valueOf(request.getListName())).get(request.getCode()) == null;
    }

}