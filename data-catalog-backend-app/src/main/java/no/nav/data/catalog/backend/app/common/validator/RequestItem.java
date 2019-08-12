package no.nav.data.catalog.backend.app.common.validator;

import java.util.Map;

public interface RequestItem {
    String getItemType();

    String identifyingFields();

    Map<String, String> validateThatNoFieldsAreNullOrEmpty();

    void toUpperCaseAndTrim();
}