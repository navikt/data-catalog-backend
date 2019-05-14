package no.nav.data.catalog.backend.test.integration;

import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.SYNCED;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.CATEGORY_ID_PERSONALIA;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.DESCRIPTION;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.INFORMATION_NAME;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.PRODUCER_ID_SKATTEETATEN;
import static no.nav.data.catalog.backend.test.integration.TestData.TestData.SYSTEM_ID_AA_REG;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.data.catalog.backend.app.AppStarter;
import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchRepository;
import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus;
import no.nav.data.catalog.backend.app.informationtype.InformationType;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRepository;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import no.nav.data.catalog.backend.test.component.FixedElasticsearchContainer;
import org.junit.Before;
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
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {IntegrationTestConfig.class, AppStarter.class})
@ActiveProfiles("itest")
@ContextConfiguration(initializers = {InformationTypeServiceIT.Initializer.class})
public class InformationTypeServiceIT {
    @Autowired
    private InformationTypeService service;

    @Autowired
    private InformationTypeRepository repository;

    @Autowired
    private ElasticsearchRepository esRepository;

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
    public void syncNewInformationTypesToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(1L));
        Map<String, Object> esMap = esRepository.getInformationTypeById("elasticSearchId");
        assertInformationType(esMap);
        InformationType informationType = repository.findAll().get(0);
        assertThat(informationType.getElasticsearchStatus(), is(SYNCED));
    }

    @Test
    public void syncUpdatedInformationTypesToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(1L));

        assertThat(repository.findAll().size(), is(1));
        InformationType informationType = repository.findAll().get(0);
        informationType.setElasticsearchStatus(ElasticsearchStatus.TO_BE_UPDATED);
        repository.save(informationType);
        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(1L));
        Map<String, Object> esMap = esRepository.getInformationTypeById("elasticSearchId");
        assertInformationType(esMap);
        assertThat(repository.findAll().size(), is(1));
        informationType = repository.findAll().get(0);
        assertThat(informationType.getElasticsearchStatus(), is(SYNCED));
    }

    @Test
    public void syncNotExistingUpdatedInformationTypesToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_UPDATED);
        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(1L));
        Map<String, Object> esMap = esRepository.getInformationTypeById("elasticSearchId");
        assertInformationType(esMap);
        assertThat(repository.findAll().size(), is(1));
        InformationType informationType = repository.findAll().get(0);
        assertThat(informationType.getElasticsearchStatus(), is(SYNCED));
    }


    @Test
    public void syncDeletedInformationTypesToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(1L));

        assertThat(repository.findAll().size(), is(1));
        InformationType informationType = repository.findAll().get(0);
        informationType.setElasticsearchStatus(ElasticsearchStatus.TO_BE_DELETED);
        repository.save(informationType);
        Map<String, Object> esMap = esRepository.getInformationTypeById("elasticSearchId");
        assertInformationType(esMap);

        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllInformationTypes().getHits().totalHits, is(0L));
        assertThat(repository.findAll().size(), is(0));
    }

    private void createTestData(ElasticsearchStatus esStatus) {
        InformationType informationType = InformationType.builder()
                .elasticsearchId("elasticSearchId")
                .category(CATEGORY_ID_PERSONALIA)
                .system(SYSTEM_ID_AA_REG)
                .elasticsearchStatus(esStatus)
                .name(INFORMATION_NAME)
                .description(DESCRIPTION)
                .producer(PRODUCER_ID_SKATTEETATEN)
                .personalData(true).build();
        repository.save(informationType);
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

    private void assertInformationType(Map<String, Object> esMap) {
        assertThat(esMap.get("producer"), is(PRODUCER_ID_SKATTEETATEN.intValue()));
        assertThat(esMap.get("system"), is(SYSTEM_ID_AA_REG.intValue()));
        assertThat(esMap.get("personalData"), is(true));
        assertThat(esMap.get("name"), is(INFORMATION_NAME));
        assertThat(esMap.get("description"), is(DESCRIPTION));
        assertThat(esMap.get("category"), is(CATEGORY_ID_PERSONALIA.intValue()));
    }
}
