package no.nav.data.catalog.backend.test.integration;

import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.TO_BE_DELETED;
import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.TO_BE_UPDATED;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CATEGORY;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CATEGORY_ID_PERSONALIA;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.DESCRIPTION;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.INFORMATION_NAME;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.PRODUCER;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.PRODUCER_ID_SKATTEETATEN;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.SYSTEM;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.SYSTEM_ID_AA_REG;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.data.catalog.backend.app.AppStarter;
import no.nav.data.catalog.backend.app.informationtype.InformationType;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRepository;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRequest;
import no.nav.data.catalog.backend.test.component.FixedElasticsearchContainer;
import org.junit.Before;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {IntegrationTestConfig.class, AppStarter.class})
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = {InformationTypeControllerIT.Initializer.class})
public class InformationTypeControllerIT {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected InformationTypeRepository repository;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer =
            (PostgreSQLContainer) new PostgreSQLContainer("postgres:10.4")
                    .withDatabaseName("sampledb")
                    .withUsername("sampleuser")
                    .withPassword("samplepwd")
                    .withStartupTimeout(Duration.ofSeconds(600));

    @ClassRule
    public static FixedElasticsearchContainer container = new FixedElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.4.1");

    @Before
    public void init() {
        repository.deleteAll();
    }

    @Test
    public void createInformationType() {
        InformationTypeRequest request = createRequest();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/backend/informationtype", HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        assertThat(repository.findAll().size(), is(1));
        assertInformationType(repository.findByName(INFORMATION_NAME).get());
    }

    @Test
    public void getInformationTypes() {
        InformationTypeRequest request = createRequest();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/backend/informationtype", HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        assertThat(repository.findAll().size(), is(1));
        assertInformationType(repository.findByName(INFORMATION_NAME).get());
        ResponseEntity<List<InformationType>> response = restTemplate.exchange(
                "/backend/informationtype", HttpMethod.GET, new HttpEntity<>(request), new ParameterizedTypeReference<List<InformationType>>(){});
        assertThat(response.getBody().size(), is(1));
        assertInformationType(response.getBody().get(0));
    }

    @Test
    public void updateInformationType() {
        InformationTypeRequest request = createRequest();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/backend/informationtype", HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        assertThat(repository.findAll().size(), is(1));

        InformationType storedInformationType = repository.findByName(INFORMATION_NAME).get();
        request.setDescription(DESCRIPTION + "UPDATED");
        responseEntity = restTemplate.exchange(
                "/backend/informationtype/" + storedInformationType.getId(), HttpMethod.PUT, new HttpEntity<>(request), String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        assertThat(repository.findAll().size(), is(1));
        storedInformationType = repository.findByName(INFORMATION_NAME).get();
        assertThat(storedInformationType.getDescription(), is(DESCRIPTION + "UPDATED"));
        assertThat(storedInformationType.getElasticsearchStatus(), is(TO_BE_UPDATED));
    }

    @Test
    public void deleteInformationType() throws Exception {
        InformationTypeRequest request = createRequest();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/backend/informationtype", HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        assertThat(repository.findAll().size(), is(1));

        InformationType storedInformationType = repository.findByName(INFORMATION_NAME).get();
        responseEntity = restTemplate.exchange(
                "/backend/informationtype/" + storedInformationType.getId(), HttpMethod.DELETE, new HttpEntity<>(request), String.class);
        assertThat(repository.findAll().size(), is(1));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.ACCEPTED));
        storedInformationType = repository.findByName(INFORMATION_NAME).get();
        assertThat(storedInformationType.getElasticsearchStatus(), is(TO_BE_DELETED));
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

    private void assertInformationType(InformationType informationType) {
        assertThat(informationType.getProducer(), is(PRODUCER_ID_SKATTEETATEN));
        assertThat(informationType.getSystem(), is(SYSTEM_ID_AA_REG));
        assertThat(informationType.isPersonalData(), is(true));
        assertThat(informationType.getName(), is(INFORMATION_NAME));
        assertThat(informationType.getDescription(), is(DESCRIPTION));
        assertThat(informationType.getCategory(), is(CATEGORY_ID_PERSONALIA));
    }

    private InformationTypeRequest createRequest() {
        return InformationTypeRequest.builder()
                .category(CATEGORY)
                .description(DESCRIPTION)
                .name(INFORMATION_NAME)
                .personalData(true)
                .system(SYSTEM)
                .producer(PRODUCER).build();
    }
}
