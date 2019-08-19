package no.nav.data.catalog.backend.app.elasticsearch;

import no.nav.data.catalog.backend.app.AppStarter;
import no.nav.data.catalog.backend.app.IntegrationTestBase;
import no.nav.data.catalog.backend.app.IntegrationTestConfig;
import no.nav.data.catalog.backend.app.codelist.CodeResponse;
import no.nav.data.catalog.backend.app.codelist.CodelistService;
import no.nav.data.catalog.backend.app.codelist.ListName;
import no.nav.data.catalog.backend.app.common.utils.JsonUtils;
import no.nav.data.catalog.backend.app.dataset.Dataset;
import no.nav.data.catalog.backend.app.dataset.DatasetData;
import no.nav.data.catalog.backend.app.dataset.DatasetResponse;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRepository;
import no.nav.data.catalog.backend.app.policy.PolicyResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchDocument.newDatasetDocumentId;
import static no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus.SYNCED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {IntegrationTestConfig.class, AppStarter.class})
public class ElasticsearchDatasetServiceIT extends IntegrationTestBase {

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "desc";
    private static final String CATEGORY_CODE = "cat1";
    private static final String CATEGORY_DESCRIPTION = "cat1desc";


    @Autowired
    private ElasticsearchDatasetService service;

    @Autowired
    private DatasetRepository repository;

    @Autowired
    private ElasticsearchRepository esRepository;

    @Autowired
    protected CodelistService codelistService;

    private static Map<ListName, Map<String, String>> codelists;

    @ClassRule
    public static FixedElasticsearchContainer container = new FixedElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.6.1");

    @Before
    public void setUp() {
        repository.deleteAll();
        initializeCodelists();
        policyStubbing();
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    private void initializeCodelists() {
        codelists = CodelistService.codelists;
        codelists.get(ListName.CATEGORY).put(CATEGORY_CODE, CATEGORY_DESCRIPTION);
    }

    @Test
    public void syncNewDatasetsToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(1L));
        String json = esRepository.getById(newDatasetDocumentId("elasticSearchId"));
        assertDataset(json);
        Dataset dataset = repository.findAll().get(0);
        assertThat(dataset.getElasticsearchStatus(), is(SYNCED));
    }

    @Test
    public void syncUpdatedDatasetsToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(1L));

        assertThat(repository.findAll().size(), is(1));
        Dataset dataset = repository.findAll().get(0);
        dataset.setElasticsearchStatus(ElasticsearchStatus.TO_BE_UPDATED);
        repository.save(dataset);
        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(1L));
        String json = esRepository.getById(newDatasetDocumentId("elasticSearchId"));
        assertDataset(json);

        assertThat(repository.findAll().size(), is(1));
        dataset = repository.findAll().get(0);
        assertThat(dataset.getElasticsearchStatus(), is(SYNCED));
    }

    @Test
    public void syncNotExistingUpdatedDatasetsToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_UPDATED);
        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(1L));
        String json = esRepository.getById(newDatasetDocumentId("elasticSearchId"));
        assertDataset(json);

        assertThat(repository.findAll().size(), is(1));
        Dataset dataset = repository.findAll().get(0);
        assertThat(dataset.getElasticsearchStatus(), is(SYNCED));
    }


    @Test
    public void syncDeletedDatasetsToES() throws Exception {
        createTestData(ElasticsearchStatus.TO_BE_CREATED);
        service.synchToElasticsearch();
        // Let indexing finish
        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(1L));

        assertThat(repository.findAll().size(), is(1));
        Dataset dataset = repository.findAll().get(0);
        dataset.setElasticsearchStatus(ElasticsearchStatus.TO_BE_DELETED);
        repository.save(dataset);
        String json = esRepository.getById(newDatasetDocumentId("elasticSearchId"));
        assertDataset(json);

        service.synchToElasticsearch();

        Thread.sleep(1000L);
        assertThat(esRepository.getAllDatasets().getHits().totalHits, is(0L));
        assertThat(repository.findAll().size(), is(0));
    }

    private void createTestData(ElasticsearchStatus esStatus) {
        Dataset dataset = Dataset.builder()
                .id(DATASET_ID_1)
                .elasticsearchId("elasticSearchId")
                .elasticsearchStatus(esStatus)
                .datasetData(DatasetData.builder()
                        .title(TITLE)
                        .description(DESCRIPTION)
                        .categories(Collections.singletonList(CATEGORY_CODE))
                        .pi(true)
                        .build())
                .build();
        repository.save(dataset);
    }

    private void assertDataset(String json) {
        var dataset = JsonUtils.toObject(json, DatasetResponse.class);

        assertThat(dataset.getTitle(), is(TITLE));
        assertThat(dataset.getDescription(), is(DESCRIPTION));
        assertThat(dataset.getPi(), is(true));
        assertThat(dataset.getCategories(), is(Collections.singletonList(new CodeResponse(CATEGORY_CODE, CATEGORY_DESCRIPTION))));
        List<PolicyResponse> policies = dataset.getPolicies();
        assertThat(policies.size(), is(2));
        assertPolicies0(policies.get(0));
        assertPolicies1(policies.get(1));
    }

    private void assertPolicies0(PolicyResponse policy) {
        assertThat(policy.getPolicyId(), is(1L));
        assertThat(policy.getPurpose(), is(new CodeResponse("KTR", "Kontroll")));
        assertThat(policy.getLegalBasisDescription(), is("LB description"));
    }

    private void assertPolicies1(PolicyResponse policy) {
        assertThat(policy.getPolicyId(), is(2L));
        assertThat(policy.getPurpose(), is(new CodeResponse("AAP", "Arbeidsavklaringspenger")));
        assertThat(policy.getLegalBasisDescription(), is("Ftrl. § 11-20"));
    }
}