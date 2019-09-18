package no.nav.data.catalog.backend.app.kafka;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {

    public SchemaRegistryContainer(String version, KafkaContainer kafka) {
        super("confluentinc/cp-schema-registry:" + version);
        withExposedPorts(8081);
        withNetwork(kafka.getNetwork());
        dependsOn(kafka);
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafka.getNetworkAliases().get(0) + ":9092");
    }

    public String getAddress() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(8081);
    }
}