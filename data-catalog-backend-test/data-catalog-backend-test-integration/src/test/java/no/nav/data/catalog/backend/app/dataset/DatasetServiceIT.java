package no.nav.data.catalog.backend.app.dataset;

import no.nav.data.catalog.backend.app.elasticsearch.ElasticsearchStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatasetServiceIT extends AbstractDatasetIT {

    @Before
    public void setUp() {
        datasetRepository.deleteAll();
        datasetRepository.saveAll(Arrays.asList(dataset111, dataset11, dataset12, dataset1, unrelated));
        entityManager.clear();
    }

    @After
    public void tearDown() {
        datasetRepository.deleteAll();
    }

    @Test
    public void testLoad() {
        assertThat(datasetRepository.getOne(unrelated.getId()).getId(), is(unrelated.getId()));
    }

    @Test
    public void getDatasetResponseTree() {
        DatasetResponse datasetResponse = datasetService.findDatasetWithAllDescendants(dataset1.getId());

        assertThat(datasetResponse.getId(), is(dataset1.getId()));
        assertThat(datasetResponse.getChildren(), hasSize(2));
        DatasetResponse datasetResponse11 = findChildByTitle(datasetResponse, "11");
        DatasetResponse datasetResponse12 = findChildByTitle(datasetResponse, "12");
        assertThat(datasetResponse11.getChildren(), hasSize(1));
        assertThat(datasetResponse12.getChildren(), hasSize(0));
        assertThat(findChildByTitle(datasetResponse11, "111").getChildren(), hasSize(0));
    }

    @Test
    public void findRootDataset() {
        Page<DatasetResponse> allRootDatasets = datasetService.findAllRootDatasets(true, PageRequest.of(0, 20));
        assertThat(allRootDatasets.getContent(), hasSize(2));

        ElasticsearchStatus elasticsearchStatus = ElasticsearchStatus.TO_BE_CREATED;
        datasetRepository.findAll(Example.of(Dataset.builder().elasticsearchStatus(elasticsearchStatus).build()));
    }

    private DatasetResponse findChildByTitle(DatasetResponse dataset, String title) {
        Optional<DatasetResponse> optional = dataset.getChildren().stream().filter(ds -> ds.getTitle().equals(title)).findFirst();
        assertTrue(title + " child missing from " + dataset.getTitle(), optional.isPresent());
        return optional.get();
    }

}