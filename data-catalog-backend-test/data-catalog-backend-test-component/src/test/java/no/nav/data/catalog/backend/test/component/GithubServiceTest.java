package no.nav.data.catalog.backend.test.component;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.data.catalog.backend.app.github.GithubConsumer;
import no.nav.data.catalog.backend.app.github.GithubService;
import no.nav.data.catalog.backend.app.github.domain.GithubFile;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ComponentTestConfig.class)
@ActiveProfiles("test")
public class GithubServiceTest {

    @Mock
    private GithubConsumer consumerMock;

    @Mock
	private InformationTypeService informationTypeService;

    @InjectMocks
    private GithubService service;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
	public void getSingleInformationTypeRequest() throws Exception {
        byte[] content;
        InputStream in = getClass().getResourceAsStream("/files/InformationType.json");
        content = in.readAllBytes();

        when(consumerMock.getFile(anyString())).thenReturn(new GithubFile("filename.json", "filpath", "sha", 1L, "url", "html_url", "git_url", "download_url", "file", Base64.encodeBase64String(content), "base64"));
        service.handle("testdataIkkeSlett/singleRow.json");
        //Give elasticsearch a few seconds to index documents
		verify(informationTypeService, times(1)).createInformationType(any());
    }

    @Test
	public void getMultipleInformationTypeRequests() throws Exception {
        byte[] content;
        InputStream in = getClass().getResourceAsStream("/files/InformationTypes.json");
        content = in.readAllBytes();

        when(consumerMock.getFile(anyString())).thenReturn(new GithubFile("filename.json", "filpath", "sha", 1L, "url", "html_url", "git_url", "download_url", "file", Base64.encodeBase64String(content), "base64"));
        service.handle("testdataIkkeSlett/multipleRows.json");
		verify(informationTypeService, times(6)).createInformationType(any());
    }

    @Test
	public void getInformationTypeNotAFile() throws Exception {
        byte[] content;
        InputStream in = getClass().getResourceAsStream("/files/InformationTypes.json");
        content = in.readAllBytes();

        when(consumerMock.getFile(anyString())).thenReturn(new GithubFile("filename.json", "filpath", "sha", 1L, "url", "html_url", "git_url", "download_url", "directory", Base64.encodeBase64String(content), "base64"));
        service.handle("testdataIkkeSlett/singleRow.json");
        //Give elasticsearch a few seconds to index documents
		verify(informationTypeService, times(0)).createInformationType(any());
    }

    @Test
	public void getInformationTypeNotBase64Encoded() throws Exception {
        byte[] content;
        InputStream in = getClass().getResourceAsStream("/files/InformationTypes.json");
        content = in.readAllBytes();

        when(consumerMock.getFile(anyString())).thenReturn(new GithubFile("filename.json", "filpath", "sha", 1L, "url", "html_url", "git_url", "download_url", "file", Base64.encodeBase64String(content), "whaat"));
        service.handle("testdataIkkeSlett/singleRow.json");
        //Give elasticsearch a few seconds to index documents
		verify(informationTypeService, times(0)).createInformationType(any());
    }
}
