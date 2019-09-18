package no.nav.data.catalog.backend.app.kafka.dataset;

import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.avro.dataset.DatasetRecord;
import no.nav.data.catalog.backend.app.avro.distributionchannel.DistributionChannelSchema;
import no.nav.data.catalog.backend.app.common.validator.ValidationError;
import no.nav.data.catalog.backend.app.dataset.DatacatalogMaster;
import no.nav.data.catalog.backend.app.dataset.DatasetRequest;
import no.nav.data.catalog.backend.app.dataset.DatasetService;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannelShort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DatasetConsumer {

    private final DatasetService datasetService;

    public DatasetConsumer(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @KafkaListener(topics = "${kafka.topics.dataset}")
    public void onMessage(ConsumerRecord<String, DatasetRecord> record, Acknowledgment ack) {
        log.info("Received message containing datasetRecord {}", record);

        DatasetRequest datasetRequest = mapRecordToRequest(record.value());
        log.info("Successfully mapped datasetRecord to datasetRequest");
        boolean isUpdate = datasetService.existingDatasetTitle(datasetRequest.getTitle());
        DatasetRequest.initiateRequests(List.of(datasetRequest), isUpdate, DatacatalogMaster.KAFKA);

        log.info("Validating datasetRequest");
        List<ValidationError> validationErrors = datasetService.validateRequestsAndReturnErrors(List.of(datasetRequest));
        if (validationErrors.isEmpty()) {
            processDatasetRequest(datasetRequest, isUpdate);
            log.info("Successfully processed datasetRequest");
            ack.acknowledge();
        } else {
            log.error("The request was not accepted. The following errors occurred during validation: {}", validationErrors);
            //TODO: Handle validationError for kafka-messages
        }
    }


    private DatasetRequest mapRecordToRequest(DatasetRecord datasetRecord) {
        return DatasetRequest.builder()
                .contentType(datasetRecord.getContentType().toString())
                .title(datasetRecord.getTitle())
                .description(datasetRecord.getDescription())
                .categories(datasetRecord.getCategories())
                .provenances(datasetRecord.getProvenances())
                .pi(datasetRecord.getPii().toString())
                .keywords(datasetRecord.getKeywords())
                .themes(datasetRecord.getThemes())
                .accessRights(datasetRecord.getAccessRights())
                .spatial(datasetRecord.getSpatial())
                .haspart(datasetRecord.getHaspart())
                .distributionChannels(mapDistributionChannelSchemaToDistributionChannelShort(datasetRecord.getDistributionChannels()))
                .issued(datasetRecord.getIssued())
                .publisher(datasetRecord.getPublisher())
                .build();
    }

    private List<DistributionChannelShort> mapDistributionChannelSchemaToDistributionChannelShort(List<DistributionChannelSchema> distributionChannels) {
        return distributionChannels.stream()
                .map(schema -> DistributionChannelShort.builder()
                        .name(schema.getName())
                        .type(schema.getType().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private void processDatasetRequest(DatasetRequest datasetRequest, boolean isUpdate) {
        if (isUpdate) {
            log.info("updating datasetRequest: '{}'", datasetRequest);
            datasetService.update(datasetRequest);
        } else {
            log.info("creating a new dataset for datasetRequest: '{}'", datasetRequest);
            datasetService.save(datasetRequest, DatacatalogMaster.KAFKA);
        }
    }
}
