package no.nav.data.catalog.backend.app.codelist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.data.catalog.backend.app.common.validator.RequestItem;
import no.nav.data.catalog.backend.app.common.validator.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodelistRequest implements RequestItem {

    private String listName;
	private String code;
	private String description;
    private static final String ITEM_TYPE = "codelist";

	public Codelist convert() {
		return Codelist.builder()
                .listName(ListName.valueOf(listName))
				.code(code)
				.description(description)
				.build();
	}

    @Override
    public String getItemType() {
        return ITEM_TYPE;
    }

    @Override
    public String identifyingFields() {
        return listName + "-" + code;
    }

    @Override
    public List<ValidationError> validateThatNoFieldsAreNullOrEmpty(String reference) {
        final String ERROR_TYPE = "fieldIsNullOrMissing";
        String errorMessage = "The %s was null or missing";
        List<ValidationError> validationErrors = new ArrayList<>();

        if (listName == null || listName.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "listName")));
        }
        if (code == null || code.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "code")));
        }
        if (description == null || description.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "description")));
        }
        return validationErrors;
    }

    @Override
    public void toUpperCaseAndTrim() {
        setListName(this.listName.toUpperCase().trim());
		setCode(this.code.toUpperCase().trim());
		setDescription(this.description.trim());
	}


}
