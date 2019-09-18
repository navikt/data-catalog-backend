package no.nav.data.catalog.backend.app;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import no.nav.data.catalog.backend.app.IntegrationTestBase.Initializer;
import no.nav.data.catalog.backend.app.avro.dataset.DatasetRecord;
import no.nav.data.catalog.backend.app.codelist.CodelistService;
import no.nav.data.catalog.backend.app.common.nais.LeaderElectionService;
import no.nav.data.catalog.backend.app.common.utils.JsonUtils;
import no.nav.data.catalog.backend.app.dataset.repo.DatasetRepository;
import no.nav.data.catalog.backend.app.distributionchannel.DistributionChannelRepository;
import no.nav.data.catalog.backend.app.kafka.SchemaRegistryContainer;
import no.nav.data.catalog.backend.app.kafka.dataset.KafkaTopicProperties;
import no.nav.data.catalog.backend.app.system.SystemRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.KafkaContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {AppStarter.class})
@ContextConfiguration(initializers = {Initializer.class, PostgresTestContainer.Initializer.class})
public abstract class IntegrationTestBase {

    private static final int WIREMOCK_PORT = SocketUtils.findAvailableTcpPort();
    public static final int ELASTICSEARCH_PORT = SocketUtils.findAvailableTcpPort();
    private static final String CONFLUENT_VERSION = "5.3.0";

    protected static final UUID DATASET_ID_1 = UUID.fromString("acab158d-67ef-4030-a3c2-195e993f18d2");

    @ClassRule
    public static PostgresTestContainer postgreSQLContainer = PostgresTestContainer.getInstance();
    @ClassRule
    public static WireMockClassRule wiremock = new WireMockClassRule(WIREMOCK_PORT);
    @ClassRule
    public static KafkaContainer kafkaContainer = new KafkaContainer(CONFLUENT_VERSION);
    @ClassRule
    public static SchemaRegistryContainer schemaRegistryContainer = new SchemaRegistryContainer(CONFLUENT_VERSION, kafkaContainer);
    @Autowired
    protected TransactionTemplate transactionTemplate;
    @Autowired
    protected DistributionChannelRepository distributionChannelRepository;
    @Autowired
    protected SystemRepository systemRepository;
    @Autowired
    protected DatasetRepository datasetRepository;
    @Autowired
    protected CodelistService codelistService;
    @Autowired
    protected KafkaTopicProperties topicProperties;
    @Value("${spring.kafka.consumer.group-id}")
    protected String groupId;

    @Before
    public void setUpAbstract() throws Exception {
        wiremock.stubFor(get("/elector").willReturn(okJson(JsonUtils.toJson(LeaderElectionService.getHostInfo()))));
    }

    @After
    public void deleteRepositories() {
        datasetRepository.deleteAll();
        distributionChannelRepository.deleteAll();
        systemRepository.deleteAll();
    }

    protected void policyStubbing() {
        wiremock.stubFor(get(urlPathEqualTo("/policy/policy"))
                .withQueryParam("datasetId", equalTo(DATASET_ID_1.toString()))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(ContentTypeHeader.KEY, "application/json")
                        .withBody("{\"content\":["
                                + "{\"policyId\":1,\"legalBasisDescription\":\"LB description\",\"purpose\":{\"code\":\"KTR\",\"description\":\"Kontroll\"}}"
                                + ",{\"policyId\":2,\"legalBasisDescription\":\"Ftrl. § 11-20\",\"purpose\":{\"code\":\"AAP\",\"description\":\"Arbeidsavklaringspenger\"}}"
                                + "],"
                                + "\"pageable\":{\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"offset\":0,\"pageSize\":20,\"pageNumber\":0,\"unpaged\":false,\"paged\":true},"
                                + "\"last\":false,\"totalPages\":2,\"totalElements\":2,\"size\":10,\"number\":0,"
                                + "\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"first\":true,\"numberOfElements\":2,\"empty\":false}")
                ));
        wiremock.stubFor(delete(urlPathMatching("/policy/policy")).withQueryParam("datasetId", matching("[0-9a-f\\-]{36}")).willReturn(ok()));
    }

    private ProducerFactory<String, DatasetRecord> producerFactory() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.senderProps(kafkaContainer.getBootstrapServers()));
        configs.put("specific.avro.reader", "true");
        configs.put("schema.registry.url", schemaRegistryContainer.getAddress());
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, groupId);

        return new DefaultKafkaProducerFactory<>(configs);//, (Serializer<String>) null, (Serializer<DatasetRecord>) null);
    }

    public KafkaTemplate<String, DatasetRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "elasticsearch.port=" + ELASTICSEARCH_PORT,
                    "wiremock.server.port=" + WIREMOCK_PORT,
                    "KAFKA_BOOTSTRAP_SERVERS=" + kafkaContainer.getBootstrapServers(),
                    "KAFKA_SCHEMA_REGISTRY_URL=" + schemaRegistryContainer.getAddress()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
