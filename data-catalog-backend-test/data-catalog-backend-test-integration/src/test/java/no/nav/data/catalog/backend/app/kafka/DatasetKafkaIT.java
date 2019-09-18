package no.nav.data.catalog.backend.app.kafka;

import no.nav.data.catalog.backend.app.IntegrationTestBase;
import no.nav.data.catalog.backend.app.avro.dataset.ContentTypeSchema;
import no.nav.data.catalog.backend.app.avro.dataset.DatasetRecord;
import no.nav.data.catalog.backend.app.avro.distributionchannel.DistributionChannelSchema;
import no.nav.data.catalog.backend.app.avro.distributionchannel.DistributionChannelType;
import no.nav.data.catalog.backend.app.dataset.DatasetService;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRepository;
import org.awaitility.Duration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class DatasetKafkaIT extends IntegrationTestBase {

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private KafkaTemplate<String, DatasetRecord> kafkaTemplate;

    @Test
    public void consumeDatasetWithKafka() {
        DatasetRecord datasetRecord = createDatasetRecord();
        kafkaTemplate.send(topicProperties.getDataset(), datasetRecord.getTitle(), datasetRecord);

        await().atMost(Duration.TEN_SECONDS).untilAsserted(() -> assertThat(datasetRepository.findByTitle(datasetRecord.getTitle()).isPresent()));
    }

    private DatasetRecord createDatasetRecord() {
        return DatasetRecord.newBuilder()
                .setContentType(ContentTypeSchema.DATASET)
                .setTitle("DatasetTitle")
                .setDescription("DatasetDescription")
                .setCategories(List.of("CV"))
                .setProvenances(Collections.emptyList())
                .setPii(true)
                .setKeywords(Collections.emptyList())
                .setThemes(Collections.emptyList())
                .setAccessRights("accessRights")
                .setSpatial("spatial")
                .setHaspart(Collections.emptyList())
                .setDistributionChannels(List.of(DistributionChannelSchema.newBuilder()
                        .setName("ArbeidsforholdDistribusjon")
                        .setType(DistributionChannelType.REST)
                        .build()))
                .setIssued(String.valueOf(System.currentTimeMillis()))
                .setPublisher("publisher")
                .build();
    }
}
