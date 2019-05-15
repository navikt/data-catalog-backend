package no.nav.data.catalog.backend.test.integration.TestData;

import no.nav.data.catalog.backend.app.codelist.CodelistRequest;
import no.nav.data.catalog.backend.app.codelist.ListName;

public class TestData {

	public static final Long CATEGORY_ID_PERSONALIA = 7L;
	public static final String CATEGORY = "PERSONALIA";
	public static final Long PRODUCER_ID_SKATTEETATEN = 1L;
	public static final String PRODUCER = "SKATTEETATEN";
	public static final Long SYSTEM_ID_AA_REG = 32L;
	public static final String SYSTEM = "AA_REG";
	public static final String INFORMATION_NAME = "InformationName";
	public static final String DESCRIPTION = "InformationDescription";

	public static final ListName CODELIST_LIST = ListName.PRODUCER;
	public static final String CODELIST_CODE = "TEST_CODE";
	public static final String CODELIST_DESCRIPTION = "Test description";

	public static CodelistRequest createRequest() {
		return CodelistRequest.builder()
				.list(CODELIST_LIST)
				.code(CODELIST_CODE)
				.description(CODELIST_DESCRIPTION)
				.build();
	}
}
