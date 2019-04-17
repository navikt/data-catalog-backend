package no.nav.data.catalog.backend.test.integration;

import static org.junit.Assert.assertEquals;

import no.nav.data.catalog.backend.app.github.GithubConsumer;
import no.nav.data.catalog.backend.app.github.GithubService;
import no.nav.data.catalog.backend.app.informationtype.InformationType;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import org.elasticsearch.ElasticsearchStatusException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestConfig.class)
@ActiveProfiles("test")
public class GetFromGithubSaveElasticsearchIT {

    @Autowired
    private GithubConsumer consumerMock;

//    @Autowired
//    private RecordService recordService;

    @Autowired
	private InformationTypeService informationTypeService;

    @Autowired
    private GithubService service;

	//TODO: Fix testcontainers for elasticsearch and postgres
//    @ClassRule
//    public static FixedElasticsearchContainer elasticsearchTestContainer =
//            new FixedElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.6.1");

//    @ClassRule
//    public static FixedHostPortGenericContainer postgreSQLContainer = new FixedHostPortGenericContainer<>("postgres:latest")
//            .withEnv("POSTGRES_USER", "test")
//            .withEnv("POSTGRES_PASSWORD", "test")
//            .withEnv("POSTGRES_DB", "testDb")
//            .withFixedExposedPort(54321, 5432);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


	@Test
    public void retriveAndSaveMultipleDataset() throws Exception {
//        System.setProperty("spring.profiles.active", "test");

//        try (PostgreSQLContainer postgres = new PostgreSQLContainer<>()
//                .withInitScript("classpath:db/migration.V1.0__initialize_schema_and_tables.sql")
//                .withInitScript("classpath:db/migration.V1.1__load_codelist.sql")) {
//            postgres.start();
//
//            deleteAllFromElasticsearch();
//            byte[] content = null;
//            try (InputStream in = getClass().getResourceAsStream("/files/InformationTypes.json")) {
//                content = in.readAllBytes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            service.handle("testdataIkkeSlett/multipleRows.json");
//            //Give elasticsearch a few seconds to index documents
//            Thread.sleep(2000L);
////        List<Record> recordList = recordService.getAllRecords();
//            List<InformationType> informationTypeList = informationTypeService.getAllInformationTypes();
//            assertEquals(6, informationTypeList.size());
//        }
//    }



        deleteAllFromElasticsearch();
        byte[] content = null;
        try (InputStream in = getClass().getResourceAsStream("/files/InformationTypes.json")) {
            content = in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        service.handle("testdataIkkeSlett/multipleRows.json");
        //Give elasticsearch a few seconds to index documents
        Thread.sleep(2000L);
		List<InformationType> informationTypeList = informationTypeService.getAllInformationTypes();
		assertEquals(6, informationTypeList.size());
    }

    private void deleteAllFromElasticsearch() {
		List<InformationType> informationTypeList = new ArrayList<>();
        try {
			informationTypeList = informationTypeService.getAllInformationTypes();
        } catch (ElasticsearchStatusException e) {
            if (!e.getMessage().contains("no such index")) {
                throw e;
            }
        }
		if (!informationTypeList.isEmpty()) {
			informationTypeList.forEach(informationType ->
					informationTypeService.setInformationTypeToBeDeletedById(informationType.getInformationTypeId()));
        }
		informationTypeService.synchToElasticsearch();
    }
}
