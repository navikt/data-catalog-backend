package no.nav.data.catalog.backend.app;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import no.nav.data.catalog.backend.app.util.EnableWiremock;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@EnableWiremock
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {IntegrationTestConfig.class, AppStarter.class})
@ContextConfiguration(initializers = {PostgresTestContainer.Initializer.class})
public abstract class IntegrationTestBase {

    protected static final UUID DATASET_ID_1 = UUID.fromString("acab158d-67ef-4030-a3c2-195e993f18d2");

    @ClassRule
    public static PostgresTestContainer postgreSQLContainer = PostgresTestContainer.getInstance();

    protected void policyStubbing() {
        stubFor(get(urlPathEqualTo("/policy/policy"))
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
    }
}