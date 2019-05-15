package no.nav.data.catalog.backend.test.integration;

import static no.nav.data.catalog.backend.app.codelist.CodelistService.codelists;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CODELIST_CODE;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CODELIST_DESCRIPTION;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CODELIST_LIST;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.createRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.data.catalog.backend.app.AppStarter;
import no.nav.data.catalog.backend.app.codelist.CodelistRepository;
import no.nav.data.catalog.backend.app.codelist.CodelistRequest;
import no.nav.data.catalog.backend.app.codelist.CodelistService;
import no.nav.data.catalog.backend.app.codelist.ListName;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {IntegrationTestConfig.class, AppStarter.class})
@ActiveProfiles("itest")
@ContextConfiguration(initializers = {CodelistServiceIT.Initializer.class})
public class CodelistServiceIT {

	@Autowired
	private CodelistService service;

	@Autowired
	private CodelistRepository repository;

	@ClassRule
	public static PostgreSQLContainer postgreSQLContainer =
			(PostgreSQLContainer) new PostgreSQLContainer("postgres:10.4")
					.withDatabaseName("sampledb")
					.withUsername("sampleuser")
					.withPassword("samplepwd")
					.withStartupTimeout(Duration.ofSeconds(600));

	@Test
	public void save_shouldSaveNewCodelist() {
		CodelistRequest request = createRequest();
		int currentRepSize = repository.findAll().size();
		int currentCodelistsSize = codelists.get(CODELIST_LIST).size();

		service.save(request);

		assertThat(repository.findAll().size(), is(currentRepSize + 1));
		assertTrue(repository.findByListAndCode(CODELIST_LIST, CODELIST_CODE).isPresent());
		assertThat(codelists.get(CODELIST_LIST).size(), is(currentCodelistsSize + 1));
		assertFalse(codelists.get(CODELIST_LIST).get(CODELIST_CODE).isEmpty());

		resetRepository(request);
	}

	@Test
	public void update_shouldUpdateCodelist(){
		service.save(createRequest());

		CodelistRequest updatedRequest = CodelistRequest.builder()
				.list(CODELIST_LIST)
				.code(CODELIST_CODE)
				.description("Updated codelist")
				.build();

		service.update(updatedRequest);

		assertThat(codelists.get(CODELIST_LIST).get(CODELIST_CODE), is(updatedRequest.getDescription()));
		assertThat(repository.findByListAndCode(CODELIST_LIST, CODELIST_CODE).get().getDescription(), is(updatedRequest.getDescription()));

		resetRepository(updatedRequest);
	}

	@Test
	public void delete_shouldDeleteCodelist(){
		CodelistRequest request = createRequest();
		int currentRepSize = repository.findAll().size();
		int currentCodelistsSize = codelists.get(request.getList()).size();

		service.save(request);
		assertThat(repository.findAll().size(), is(currentRepSize + 1));
		assertThat(codelists.get(request.getList()).size(), is(currentCodelistsSize + 1));

		service.delete(request.getList(), request.getCode());

		assertThat(repository.findAll().size(), is(currentRepSize));
		assertFalse(repository.findByListAndCode(CODELIST_LIST, CODELIST_CODE).isPresent());
		assertThat(codelists.get(CODELIST_LIST).size(), is(currentCodelistsSize));
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
