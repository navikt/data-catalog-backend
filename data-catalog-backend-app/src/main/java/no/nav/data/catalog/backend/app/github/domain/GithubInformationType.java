package no.nav.data.catalog.backend.app.github.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import no.nav.data.catalog.backend.app.informationtype.domain.InformationCategoryCode;

@Data
@Builder
public class GithubInformationType {

	private String name;
	private String description;
	private InformationCategoryCode category;
	private String sensitivity;
	private String ownership;
	private String sourceOfRecord;
	private String storageTime;
	private String qualityOfData;
	private Boolean personalData;

	@JsonCreator
	public GithubInformationType(
			@JsonProperty(value = "name", required = true) String name,
			@JsonProperty(value = "description", required = true) String description,
			@JsonProperty(value = "category") InformationCategoryCode category,
			@JsonProperty(value = "sensitivity") String sensitivity,
			@JsonProperty(value = "ownership") String ownership,
			@JsonProperty(value = "sourceOfRecord") String sourceOfRecord,
			@JsonProperty(value = "storageTime") String storageTime,
			@JsonProperty(value = "qualityOfData") String qualityOfData,
			@JsonProperty(value = "personalData", required = true) Boolean personalData) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.sensitivity = sensitivity;
		this.ownership = ownership;
		this.sourceOfRecord = sourceOfRecord;
		this.storageTime = storageTime;
		this.qualityOfData = qualityOfData;
		this.personalData = personalData;
	}
}