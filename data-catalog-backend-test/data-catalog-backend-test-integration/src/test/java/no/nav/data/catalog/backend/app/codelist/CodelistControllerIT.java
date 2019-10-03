package no.nav.data.catalog.backend.app.codelist;

import no.nav.data.catalog.backend.app.IntegrationTestBase;
import no.nav.data.catalog.backend.app.PostgresTestContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CodelistControllerIT extends IntegrationTestBase {

    private static final ParameterizedTypeReference<List<Codelist>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private CodelistService service;

    @Autowired
    private CodelistRepository repository;

    @ClassRule
    public static PostgresTestContainer postgreSQLContainer = PostgresTestContainer.getInstance();

    @Before
    public void setUp() {
        service.refreshCache();
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void findAll_shouldReturnOneCodelists() {
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                "/codelist", HttpMethod.GET, HttpEntity.EMPTY, Map.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().size(), is(3));

        Arrays.stream(ListName.values())
                .forEach(listName -> assertThat(responseEntity.getBody()
                        .get(listName.toString()), is(CodelistCache.getAsMap(listName))));
    }

    @Test
    public void getCodelistByListName_shouldReturnCodesAndDescriptionForListName() {
        String url = "/codelist/PROVENANCE";

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(CodelistCache.getAsMap(ListName.PROVENANCE)));
    }

    @Test
    public void getDescriptionByListNameAndCode_shouldReturnDescriptionForCodeAndListName() {
        CodelistCache.set(Codelist.builder().list(ListName.PROVENANCE).code("TEST_CODE").description("Test description").build());
        String url = "/codelist/PROVENANCE/TEST_CODE";

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(CodelistCache.getAsMap(ListName.PROVENANCE).get("TEST_CODE")));
    }

    @Test
    public void save_shouldSaveNewCodelist() {
        String code = "Save Code";
        String description = "Test description";
        List<CodelistRequest> requests = createRequest("PROVENANCE", code, description);
        assertFalse(CodelistCache.contains(ListName.PROVENANCE, code));

        ResponseEntity<List<Codelist>> responseEntity = restTemplate.exchange(
                "/codelist", HttpMethod.POST, new HttpEntity<>(requests), RESPONSE_TYPE);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        Codelist codelist = responseEntity.getBody().get(0);
        assertThat(codelist.getDescription(), is(description));
        assertThat(codelist.getCode(), is(code));

        assertTrue(CodelistCache.contains(ListName.PROVENANCE, code));
        Codelist savecode = CodelistService.getCodelist(ListName.PROVENANCE, "savecode");
        assertThat(savecode.getCode(), is(code));
        assertThat(savecode.getNormalizedCode(), is("SAVECODE"));
        assertThat(savecode.getDescription(), is(description));
    }

    @Test
    public void save_shouldSave20Codelist() {
        List<CodelistRequest> requests = createNrOfRequests("shouldSave20Codelists", 20);

        ResponseEntity<List<Codelist>> responseEntity = restTemplate.exchange(
                "/codelist", HttpMethod.POST, new HttpEntity<>(requests), RESPONSE_TYPE);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(CodelistCache.getAsMap(ListName.PROVENANCE).size(), is(20));
    }

    @Test
    public void update_shouldUpdateOneCodelist() {
        String code = "UPDATE_CODE";
        service.save(createRequest("PROVENANCE", code, "Test description"));

        List<CodelistRequest> updatedCodelists = createRequest("PROVENANCE", code, "Updated codelists");

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/codelist", HttpMethod.PUT, new HttpEntity<>(updatedCodelists), String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(CodelistCache.getAsMap(ListName.PROVENANCE).get(code), is(updatedCodelists.get(0).getDescription()));
    }

    @Test
    public void update_shouldUpdate20Codelists() {
        List<CodelistRequest> requests = createNrOfRequests("shouldUpdate20Codelists", 20);
        restTemplate.exchange(
                "/codelist", HttpMethod.POST, new HttpEntity<>(requests), new ParameterizedTypeReference<List<Codelist>>() {
                });

        requests.forEach(request -> request.setDescription("  Updated codelists  "));
        ResponseEntity<List<Codelist>> responseEntity = restTemplate.exchange(
                "/codelist", HttpMethod.PUT, new HttpEntity<>(requests), new ParameterizedTypeReference<List<Codelist>>() {
                });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(CodelistCache.getAsMap(ListName.PROVENANCE).size(), is(20));
        Collection<String> descriptionList = CodelistCache.getAsMap(ListName.PROVENANCE).values();
        descriptionList.forEach(description -> assertThat(description, is("Updated codelists")));
    }


    @Test
    public void delete_shouldDeleteCodelist() {
        String code = "DELETE_CODE";
        List<CodelistRequest> requests = createRequest("PROVENANCE", code, "Test description");
        service.save(requests);
        assertNotNull(CodelistCache.getAsMap(ListName.PROVENANCE).get(code));

        String url = "/codelist/PROVENANCE/DELETE_CODE";

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertFalse(CodelistCache.contains(ListName.PROVENANCE, code));
    }

    private List<CodelistRequest> createNrOfRequests(String code, int nrOfRequests) {
        return IntStream.rangeClosed(1, nrOfRequests)
                .mapToObj(i -> createOneRequest("PROVENANCE", code + "_nr_" + i, "Test description"))
                .collect(Collectors.toList());

    }

    private CodelistRequest createOneRequest(String listName, String code, String description) {
        return CodelistRequest.builder()
                .list(listName)
                .code(code)
                .description(description)
                .build();
    }

    private List<CodelistRequest> createRequest(String listName, String code, String description) {
        return List.of(createOneRequest(listName, code, description));
    }

}
