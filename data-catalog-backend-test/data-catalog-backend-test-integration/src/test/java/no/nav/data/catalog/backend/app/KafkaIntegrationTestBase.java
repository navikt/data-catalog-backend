package no.nav.data.catalog.backend.app;

import no.nav.data.catalog.backend.app.kafka.KafkaContainer;
import no.nav.data.catalog.backend.app.kafka.SchemaRegistryContainer;

public abstract class KafkaIntegrationTestBase extends IntegrationTestBase {

    private static final String CONFLUENT_VERSION = "5.3.0";

    private static KafkaContainer kafkaContainer = new KafkaContainer(CONFLUENT_VERSION);
    private static SchemaRegistryContainer schemaRegistryContainer = new SchemaRegistryContainer(CONFLUENT_VERSION, kafkaContainer);

    static {
        kafkaContainer.start();
        schemaRegistryContainer.start();
    }

}
