package no.nav.data.catalog.backend.test.integration;

import static no.nav.data.catalog.backend.app.codelist.CodelistService.codelists;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CODELIST_CODE;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CODELIST_LIST;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.createRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.data.catalog.backend.app.AppStarter;
import no.nav.data.catalog.backend.app.codelist.CodelistRequest;
import no.nav.data.catalog.backend.app.codelist.CodelistService;
import no.nav.data.catalog.backend.app.codelist.ListName;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {IntegrationTestConfig.class, AppStarter.class})
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = {CodelistControllerIT.Initializer.class})
public class CodelistControllerIT {

	private final String CODELIST_URL = "/backend/codelist";

	@Autowired
	protected TestRestTemplate restTemplate;

	@Autowired
	private CodelistService service;

	@ClassRule
	public static PostgreSQLContainer postgreSQLContainer =
			(PostgreSQLContainer) new PostgreSQLContainer("postgres:10.4")
					.withDatabaseName("sampledb")
					.withUsername("sampleuser")
					.withPassword("samplepwd")
					.withStartupTimeout(Duration.ofSeconds(600));

	@Test
	public void findAll_shouldReturnCodelists() {
		ResponseEntity<Map> responseEntity = restTemplate.exchange(
				CODELIST_URL, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody().size(), is(3));

		Arrays.stream(ListName.values())
				.forEach(listName -> assertThat(responseEntity.getBody()
						.get(listName.toString()), is(codelists.get(listName))));
	}

	@Test
	public void getCodelistByListName_shouldReturnCodesAndDescriptionForListName() {
		String url = CODELIST_URL + "/SYSTEM";

		ResponseEntity<Map> responseEntity = restTemplate.exchange(
				url, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody(), is(codelists.get(ListName.SYSTEM)));
	}

	@Test
	public void getDescriptionByListNameAndCode_shouldReturnDescriptionForCodeAndListName() {
		String code = "ARBEIDSGIVER";
		String url = CODELIST_URL + "/" + CODELIST_LIST + "/" + code;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				url, HttpMethod.GET, HttpEntity.EMPTY, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(responseEntity.getBody(), is(codelists.get(ListName.PRODUCER).get(code)));
	}

	@Test
	public void save_shouldSaveNewCodelist() {
		CodelistRequest request = createRequest();
		int currentCodelistSize = codelists.get(request.getList()).size();
		assertNull(codelists.get(request.getList()).get(request.getCode()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				CODELIST_URL, HttpMethod.POST, new HttpEntity<>(request), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
		assertThat(codelists.get(request.getList()).size(), is(currentCodelistSize + 1));
		assertFalse(codelists.get(request.getList()).get(request.getCode()).isEmpty());
		assertTrue(responseEntity.getBody().contains(codelists.get(request.getList()).get(request.getCode())));

		resetRepository(request);
	}

	@Test
	public void update_shouldUpdateCodelist() {
		CodelistRequest request = createRequest();
		service.save(request);

		CodelistRequest updateRequest = CodelistRequest.builder()
				.list(CODELIST_LIST)
				.code(CODELIST_CODE)
				.description("Updated codelist")
				.build();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				CODELIST_URL, HttpMethod.PUT, new HttpEntity<>(updateRequest), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
		assertThat(codelists.get(CODELIST_LIST).get(CODELIST_CODE), is(updateRequest.getDescription()));

		resetRepository(request);
	}


	@Test
	public void delete_shouldDeleteCodelist() {
		CodelistRequest request = createRequest();
		service.save(request);
		assertNotNull(codelists.get(CODELIST_LIST).get(CODELIST_CODE));

		String url = CODELIST_URL + "/" + CODELIST_LIST + "/" + CODELIST_CODE;

		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertNull(codelists.get(CODELIST_LIST).get(CODELIST_CODE));
	}

	private void resetRepository(CodelistRequest request) {
		service.delete(request.getList(), request.getCode());
	}

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
					"spring.datasource.username=" + postgreSQLContainer.getUsername(),
					"spring.datasource.password=" + postgreSQLContainer.getPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
