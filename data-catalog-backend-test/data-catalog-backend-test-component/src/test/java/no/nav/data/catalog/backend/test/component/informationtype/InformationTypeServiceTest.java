package no.nav.data.catalog.backend.test.component.informationtype;

import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.TO_BE_CREATED;
import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.TO_BE_DELETED;
import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.TO_BE_UPDATED;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.CATEGORY_CODE;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.CATEGORY_DESCRIPTION;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.DESCRIPTION;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.NAME;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.PRODUCER_CODE_LIST;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.PRODUCER_CODE_STRING;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.PRODUCER_DESCRIPTION_LIST;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.SYSTEM_CODE;
import static no.nav.data.catalog.backend.test.component.informationtype.TestdataInformationTypes.SYSTEM_DESCRIPTION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.data.catalog.backend.app.codelist.CodelistRepository;
import no.nav.data.catalog.backend.app.codelist.CodelistService;
import no.nav.data.catalog.backend.app.codelist.ListName;
import no.nav.data.catalog.backend.app.common.exceptions.ValidationException;
import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchRepository;
import no.nav.data.catalog.backend.app.informationtype.InformationType;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRepository;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRequest;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import no.nav.data.catalog.backend.test.component.ComponentTestConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ComponentTestConfig.class)
@ActiveProfiles("test")
@EnableJpaRepositories(repositoryBaseClass = CodelistRepository.class)
public class InformationTypeServiceTest {

	@Mock
	private InformationTypeRepository informationTypeRepository;

	@Mock
	private ElasticsearchRepository elasticsearchRepository;

	@InjectMocks
	private InformationTypeService informationTypeService;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static HashMap<ListName, HashMap<String, String>> codelists;
	private static InformationType informationType;


	@Before
	public void init() {
		codelists = CodelistService.codelists;
		codelists.get(ListName.CATEGORY).put(CATEGORY_CODE, CATEGORY_DESCRIPTION);
		codelists.get(ListName.PRODUCER).put(PRODUCER_CODE_LIST.get(0), PRODUCER_DESCRIPTION_LIST.get(0));
		codelists.get(ListName.PRODUCER).put(PRODUCER_CODE_LIST.get(1), PRODUCER_DESCRIPTION_LIST.get(1));
		codelists.get(ListName.SYSTEM).put(SYSTEM_CODE, SYSTEM_DESCRIPTION);

		informationType = InformationType.builder()
				.id(1L)
				.name(NAME)
				.description(DESCRIPTION)
				.categoryCode(CATEGORY_CODE)
				.producerCode(PRODUCER_CODE_STRING)
				.systemCode(SYSTEM_CODE)
				.personalData(true)
				.elasticsearchId("esId")
				.elasticsearchStatus(TO_BE_CREATED)
				.build();
		informationType.setCreatedBy("testCreatedBy");
		informationType.setCreatedDate(new Date());
		informationType.setLastModifiedBy(null);
		informationType.setLastModifiedDate(null);
	}

	@Test
	public void shouldSyncCreatedInformationTypes() {
		List<InformationType> informationTypes = new ArrayList<>();
		informationTypes.add(informationType);
		when(informationTypeRepository.findByElasticsearchStatus(TO_BE_CREATED)).thenReturn(Optional.of(informationTypes));

		informationTypeService.synchToElasticsearch();
		verify(elasticsearchRepository, times(1)).insertInformationType(anyMap());
		verify(elasticsearchRepository, times(0)).updateInformationTypeById(anyString(), anyMap());
		verify(elasticsearchRepository, times(0)).deleteInformationTypeById(anyString());
		verify(informationTypeRepository, times(1)).save(any(InformationType.class));
		verify(informationTypeRepository, times(0)).deleteById(anyLong());
	}

	@Test
	public void shouldSyncUpdatedInformationTypes() {
		List<InformationType> informationTypes = new ArrayList<>();
		informationTypes.add(informationType);
		when(informationTypeRepository.findByElasticsearchStatus(TO_BE_UPDATED)).thenReturn(Optional.of(informationTypes));

		informationTypeService.synchToElasticsearch();
		verify(elasticsearchRepository, times(0)).insertInformationType(anyMap());
		verify(elasticsearchRepository, times(1)).updateInformationTypeById(any(), anyMap());
		verify(elasticsearchRepository, times(0)).deleteInformationTypeById(anyString());
		verify(informationTypeRepository, times(1)).save(any(InformationType.class));
		verify(informationTypeRepository, times(0)).deleteById(anyLong());
	}

	@Test
	public void shouldSyncDeletedInformationTypes() {
		List<InformationType> informationTypes = new ArrayList<>();
		informationTypes.add(informationType);
		when(informationTypeRepository.findByElasticsearchStatus(TO_BE_DELETED)).thenReturn(Optional.of(informationTypes));

		informationTypeService.synchToElasticsearch();
		verify(elasticsearchRepository, times(0)).insertInformationType(anyMap());
		verify(elasticsearchRepository, times(0)).updateInformationTypeById(any(), anyMap());
		verify(elasticsearchRepository, times(1)).deleteInformationTypeById(any());
		verify(informationTypeRepository, times(0)).save(any(InformationType.class));
		verify(informationTypeRepository, times(1)).deleteById(any());
	}

	@Test
	public void validateRequestsCreate_shouldValidateOneInsertRequest() {
		informationTypeService.validateRequests(createListOfOneRequest("Name"), false);
	}

	@Test
	public void validateRequestsCreate_shouldThrowValidationException_becauseRequestIsEmpty() {
		InformationTypeRequest request = InformationTypeRequest.builder().build();
		try {
			informationTypeService.validateRequests(List.of(request), false);
		} catch (ValidationException e) {
			HashMap validationMap = e.get().get("Request nr:1");
			assertThat(validationMap.size(), is(5));
			assertThat(validationMap.get("name"), is("Name must have a non-empty value"));
			assertThat(validationMap.get("personalData"), is("PersonalData cannot be null"));
			assertThat(validationMap.get("producerCode"), is("The list of producerCodes was null"));
			assertThat(validationMap.get("categoryCode"), is("The categoryCode was null"));
			assertThat(validationMap.get("systemCode"), is("The systemCode was null"));
		}
	}

	@Test
	public void validateRequestsCreate_shouldThrowValidationException_becauseProducerListContainsUnknownCode() {
		List<InformationTypeRequest> requests = createListOfOneRequest("Name");
		requests.get(0).setProducerCode(List.of("UnknownProducerCode"));

		try {
			informationTypeService.validateRequests(requests, false);
		} catch (ValidationException e) {
			HashMap validationMap = e.get().get("Request nr:1");
			assertThat(validationMap.size(), is(1));
			assertThat(validationMap.get("producerCode"), is("The code UNKNOWNPRODUCERCODE was not found in the codelist(PRODUCER)"));
		}
	}

	@Test
	public void validateRequestsCreate_shouldThrowValidationException_becauseNamedIsNotUniqueInRepository() {
		InformationTypeRequest request = InformationTypeRequest.builder()
				.categoryCode(CATEGORY_CODE)
				.name("NotUniqueName")
				.systemCode(SYSTEM_CODE)
				.producerCode(PRODUCER_CODE_LIST)
				.personalData(true)
				.build();

		when(informationTypeRepository.findByName(anyString())).thenReturn(Optional.of(new InformationType().convertFromRequest(request, false)));
		try {
			informationTypeService.validateRequests(createListOfOneRequest("NotUniqueName"), false);
		} catch (ValidationException e) {
			HashMap validationMap = e.get().get("Request nr:1");
			assertThat(validationMap.size(), is(1));
			assertThat(validationMap.get("name"), is("The name NotUniqueName is already used by an existing Informationtype"));
		}
	}

	@Test
	public void validateRequestsCreate_shouldValidate20Request() {
		informationTypeService.validateRequests(createRequests(20), false);
	}

	@Test
	public void validateRequestsCreate_shouldThrowValidationException_becauseNamedIsNotUniqueInRequest() {
		List<InformationTypeRequest> requests = createRequests(19);

		InformationTypeRequest notUniqueNameRequest = createOneRequest(requests.get(10).getName());
		requests.add(notUniqueNameRequest);

		when(informationTypeRepository.findByName(notUniqueNameRequest.getName())).thenReturn(Optional.empty());

		try {
			informationTypeService.validateRequests(requests, false);
		} catch (ValidationException e) {
			HashMap validationMap = e.get().get("Request nr:20");
			assertThat(validationMap.size(), is(1));
			assertThat(validationMap.get("nameNotUniqueInThisRequest"), is("The name RequestNr:11 is not unique because it is already used in this request (see request nr:11)"));
		}
	}

	@Test
	public void validateRequestUpdate_shouldValidateRequest() {
		List<InformationTypeRequest> requests = createListOfOneRequest("name");
		InformationType informationType = new InformationType().convertFromRequest(requests.get(0), false);

		when(informationTypeRepository.findByName("Name")).thenReturn(Optional.of(informationType));

		informationTypeService.validateRequests(requests, true);
	}

	@Test
	public void validateRequestsUpdate_shouldThrowValidationException_becauseRequestIsEmpty() {
		InformationTypeRequest request = InformationTypeRequest.builder().build();
		try {
			informationTypeService.validateRequests(List.of(request), true);
		} catch (ValidationException e) {
			HashMap validationMap = e.get().get("Request nr:1");
			assertThat(validationMap.size(), is(5));
			assertThat(validationMap.get("name"), is("Name must have a non-empty value"));
			assertThat(validationMap.get("personalData"), is("PersonalData cannot be null"));
			assertThat(validationMap.get("producerCode"), is("The list of producerCodes was null"));
			assertThat(validationMap.get("categoryCode"), is("The categoryCode was null"));
			assertThat(validationMap.get("systemCode"), is("The systemCode was null"));
		}
	}

	@Test
	public void validateRequestUpdate_shouldValidate20Request() {
		List<InformationTypeRequest> requests = createRequests(3);

		when(informationTypeRepository.findByName("RequestNr:1")).thenReturn(Optional.of(new InformationType().convertFromRequest(requests
				.get(0), false)));
		when(informationTypeRepository.findByName("RequestNr:2")).thenReturn(Optional.of(new InformationType().convertFromRequest(requests
				.get(1), false)));
		when(informationTypeRepository.findByName("RequestNr:3")).thenReturn(Optional.of(new InformationType().convertFromRequest(requests
				.get(2), false)));

		requests.forEach(request -> request.setDescription("Updated Description"));

		informationTypeService.validateRequests(requests, true);
	}

	private List<InformationTypeRequest> createListOfOneRequest(String name) {
		return List.of(createOneRequest(name));
	}

	private InformationTypeRequest createOneRequest(String name) {
		return InformationTypeRequest.builder()
				.categoryCode(CATEGORY_CODE)
				.name(name)
				.systemCode(SYSTEM_CODE)
				.producerCode(PRODUCER_CODE_LIST)
				.personalData(true)
				.build();
	}

	private List<InformationTypeRequest> createRequests(int nrOfRequests) {
		return IntStream.rangeClosed(1, nrOfRequests)
				.mapToObj(i -> createOneRequest("RequestNr:" + i))
				.collect(Collectors.toList());
	}
}