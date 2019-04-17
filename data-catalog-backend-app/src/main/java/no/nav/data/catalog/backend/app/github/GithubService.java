package no.nav.data.catalog.backend.app.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.data.catalog.backend.app.common.exceptions.DataCatalogBackendTechnicalException;
import no.nav.data.catalog.backend.app.github.domain.GithubFile;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeRequest;
import no.nav.data.catalog.backend.app.informationtype.InformationTypeService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class GithubService {

	@Autowired
	private GithubConsumer restConsumer;

//    @Autowired
//    private RecordService recordService;

	@Autowired
	private InformationTypeService informationTypeService;

	public void handle(String filename) {
//        List<Record> records = mapToObject(restConsumer.getFile(filename));
//        saveRequests(records);
		List<InformationTypeRequest> informationTypeRequests = mapToObject(restConsumer.getFile(filename));
		saveRequests(informationTypeRequests);
	}

	private List<InformationTypeRequest> mapToObject(GithubFile file) {
		byte[] content = null;
		if (file != null && "file".equals(file.getType())) {
			if ("base64".equals(file.getEncoding())) {
				content = Base64.decodeBase64(file.getContent().getBytes());
			}
		}

		if (content != null && content.length > 0) {
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = new String(content, StandardCharsets.UTF_8).trim();

			// make array
			if (!jsonString.startsWith("[")) {
				jsonString = "[" + jsonString + "]";
			}
			try {
				return mapper.readValue(jsonString, new TypeReference<List<InformationTypeRequest>>() {
				});
			} catch (IOException e) {
				throw new DataCatalogBackendTechnicalException(String.format("Error occurred during parse of Json in file %s from github ", file
						.getName()), e);
			}
		}

		return null;
	}

	private void saveRequests(List<InformationTypeRequest> informationTypeRequests) {

		if (informationTypeRequests != null) {
			informationTypeRequests.forEach(request -> {
				informationTypeService.createInformationType(request); //TODO: Write in batch - not as individual inserts. Add transactions
			});
		}
	}
}
