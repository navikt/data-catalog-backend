package no.nav.data.catalog.backend.app.common.validator;

import java.util.List;

public interface RequestItem {
    String getItemType();

    String identifyingFields();

    List<ValidationError> validateThatNoFieldsAreNullOrEmpty(String reference);

    void toUpperCaseAndTrim();
}