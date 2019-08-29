package no.nav.data.catalog.backend.app.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.data.catalog.backend.app.common.exceptions.DataCatalogBackendTechnicalException;
import no.nav.data.catalog.backend.app.common.utils.JsonUtils;
import no.nav.data.catalog.backend.app.common.validator.ValidationError;
import no.nav.data.catalog.backend.app.github.GithubReference;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.egit.github.core.RepositoryContents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.eclipse.egit.github.core.RepositoryContents.ENCODING_BASE64;
import static org.eclipse.egit.github.core.RepositoryContents.TYPE_FILE;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetRequest {

    private String title;
    private String description;
    private List<String> categories;
    private List<String> provenances;
    private String pi;
    private String issued;
    private List<String> keywords;
    private String theme;
    private String accessRights;
    private String publisher;
    private String spatial;
    private String haspart;
    private List<String> distributionChannels;

    @JsonIgnore
    private GithubReference githubReference;

    public static List<DatasetRequest> convertFromGithubFile(RepositoryContents file) {
        byte[] content = null;
        if (file != null && TYPE_FILE.equals(file.getType()) && ENCODING_BASE64.equals(file.getEncoding())) {
            content = Base64.decodeBase64(file.getContent().getBytes());
        }

        if (content != null && content.length > 0) {
            String jsonString = new String(content, StandardCharsets.UTF_8).trim();

            // make array
            if (!jsonString.startsWith("[")) {
                jsonString = "[" + jsonString + "]";
            }
            try {
                List<DatasetRequest> datasetRequests = JsonUtils.readValue(jsonString, new TypeReference<>() {
                });
                AtomicInteger i = new AtomicInteger(0);
                datasetRequests.forEach(request -> request.setGithubReference(new GithubReference(request.getTitle(), file.getPath(), i
                        .incrementAndGet())));
                return datasetRequests;
            } catch (IOException e) {
                String error = String.format("Error occurred during parse of Json in file %s from github ", file.getPath());
                log.error(error, e);
                throw new DataCatalogBackendTechnicalException(error, e);
            }
        }

        return Collections.emptyList();
    }

    @JsonIgnore
    public Optional<String> getRequestReference() {
        return githubReference == null ? Optional.empty() : Optional.ofNullable(githubReference.toString());
    }

    public String getReference(DatasetMaster master, String requestIndex) {
        switch (master) {
            case GITHUB:
                return getRequestReference().orElse("");
            case REST:
                return "Request:" + requestIndex;
            case KAFKA:
                return "Kafka";
            default:
                throw new IllegalStateException("Unexpected value: " + master);
        }
    }

    public List<ValidationError> validateThatNoFieldsAreNullOrEmpty(String reference) {
        final String ERROR_TYPE = "fieldIsNullOrMissing";
        String errorMessage = "The %s was null or missing";
        List<ValidationError> validationErrors = new ArrayList<>();

        if (title == null || title.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "title")));
        }
        if (description == null || description.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "description")));
        }
        if (categories == null || categories.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "categories")));
        }
        if (provenances == null || provenances.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "provenances")));
        }
        if (pi == null) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "pi")));
        }
        if (issued == null) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "issued")));
        }
        if (keywords == null || keywords.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "keywords")));
        }
        if (theme == null || theme.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "theme")));
        }
        if (accessRights == null || accessRights.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "accessRights")));
        }
        if (publisher == null || publisher.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "publisher")));
        }
        if (spatial == null || spatial.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "spatial")));
        }
        if (haspart == null || haspart.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "haspart")));
        }
        if (distributionChannels == null || distributionChannels.isEmpty()) {
            validationErrors.add(new ValidationError(reference, ERROR_TYPE, String.format(errorMessage, "distributionChannels")));
        }
        return validationErrors;
    }

    public void toUpperCaseAndTrim() {
        setTitle(this.title.toUpperCase().trim());
        setDescription(this.description.toUpperCase().trim());
        setCategories(this.categories.stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()));
        setProvenances(this.provenances.stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()));
        setPi(this.pi.trim());
        setIssued(this.issued.trim());
        setKeywords(this.keywords.stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()));
        setTheme(this.theme.toUpperCase().trim());
        setAccessRights(this.accessRights.toUpperCase().trim());
        setPublisher(this.publisher.toUpperCase().trim());
        setSpatial(this.spatial.toUpperCase().trim());
        setHaspart(this.haspart.toUpperCase().trim());
        setDistributionChannels(this.distributionChannels.stream()
                .map(String::toUpperCase)
                .map(String::trim)
                .collect(Collectors.toList()));
    }
}
