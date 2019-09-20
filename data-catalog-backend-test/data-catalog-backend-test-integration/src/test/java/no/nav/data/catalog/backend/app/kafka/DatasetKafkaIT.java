package no.nav.data.catalog.backend.app.kafka;

import no.nav.data.catalog.backend.app.IntegrationTestBase;
import no.nav.data.catalog.backend.app.avro.dataset.ContentTypeSchema;
import no.nav.data.catalog.backend.app.avro.dataset.DatasetRecord;
import no.nav.data.catalog.backend.app.avro.distributionchannel.DistributionChannelSchema;
import no.nav.data.catalog.backend.app.avro.distributionchannel.DistributionChannelType;
import no.nav.data.catalog.backend.app.codelist.CodelistRequest;
import no.nav.data.catalog.backend.app.dataset.ContentType;
import no.nav.data.catalog.backend.app.dataset.DatacatalogMaster;
import no.nav.data.catalog.backend.app.dataset.DatasetData;
import no.nav.data.catalog.backend.app.dataset.DatasetService;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRepository;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannelShort;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
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
    private LocalDateTime fixedLocalDateTime;

    @Before
    public void setUp() {
        createCodelistItemArbeidsforhold();
        fixedLocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
    }

    @Test
    public void saveDatasetWithKafkaConsumer() {
        DatasetRecord datasetRecord = createDatasetRecordWithTitle("DatasetTitle");

        sendRecordToKafka(datasetRecord);

        await().atMost(Duration.TEN_SECONDS).untilAsserted(() -> assertThat(datasetRepository.count()).isEqualTo(1L));

        // assert mapping
        DatasetData actualDatasetData = datasetRepository.findByTitle("DatasetTitle").get().getDatasetData();
        DatasetData expectedDatasetData = createDatasetDataWithTitle("DatasetTitle");

        assertThat(actualDatasetData).isEqualTo(expectedDatasetData);
    }

    private void sendRecordToKafka(DatasetRecord datasetRecord) {
        kafkaTemplate.send(topicProperties.getDataset(), datasetRecord.getTitle(), datasetRecord);
    }

    private void createCodelistItemArbeidsforhold() {
        codelistService.save(List.of(CodelistRequest.builder()
                .list("CATEGORY")
                .code("ARBEIDSFORHOLD")
                .description("Arbeidsforhold")
                .build()));
    }

    private DatasetRecord createDatasetRecordWithTitle(String title) {
        return DatasetRecord.newBuilder()
                .setContentType(ContentTypeSchema.DATASET)
                .setTitle(title)
                .setDescription("DatasetDescription")
                .setCategories(List.of("ARBEIDSFORHOLD"))
                .setProvenances(Collections.emptyList())
                .setPii(true)
                .setKeywords(Collections.emptyList())
                .setThemes(Collections.emptyList())
                .setAccessRights("accessRights")
                .setSpatial("spatial")
                .setHaspart(Collections.emptyList())
                .setDistributionChannels(List.of(DistributionChannelSchema.newBuilder()
                        .setName("ArbeidsforholdDistribusjon")
                        .setType(DistributionChannelType.KAFKA)
                        .build()))
                .setIssued(fixedLocalDateTime.toString())
                .setPublisher("publisher")
                .build();
    }

    private DatasetData createDatasetDataWithTitle(String title) {
        return DatasetData.builder()
                .contentType(ContentType.DATASET)
                .title(title)
                .description("DatasetDescription")
                .categories(List.of("ARBEIDSFORHOLD"))
                .provenances(Collections.emptyList())
                .pi(true)
                .keywords(Collections.emptyList())
                .themes(Collections.emptyList())
                .accessRights("accessRights")
                .spatial("spatial")
                .haspart(Collections.emptyList())
                .distributionChannels(List.of(DistributionChannelShort.builder()
                        .name("ArbeidsforholdDistribusjon")
                        .type("KAFKA")
                        .build()))
                .issued(fixedLocalDateTime)
                .publisher("publisher")
                .datacatalogMaster(DatacatalogMaster.KAFKA)
                .build();
    }
}
