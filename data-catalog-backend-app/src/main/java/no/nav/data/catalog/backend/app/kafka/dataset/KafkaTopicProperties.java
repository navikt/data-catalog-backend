package no.nav.data.catalog.backend.app.kafka.dataset;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("kafka.topics")
public class KafkaTopicProperties {

    private String dataset;
}
