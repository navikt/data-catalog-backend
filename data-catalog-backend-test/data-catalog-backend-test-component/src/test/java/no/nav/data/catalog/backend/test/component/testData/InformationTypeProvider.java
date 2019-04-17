package no.nav.data.catalog.backend.test.component.testData;


import static no.nav.data.catalog.backend.app.common.elasticsearch.ElasticsearchStatus.TO_BE_CREATED;
import static no.nav.data.catalog.backend.app.common.elasticsearch.ElasticsearchStatus.TO_BE_DELETED;
import static no.nav.data.catalog.backend.app.common.elasticsearch.ElasticsearchStatus.TO_BE_UPDATED;

import no.nav.data.catalog.backend.app.informationtype.InformationType;

public class InformationTypeProvider {

	private static final String elasticsearchId_sivilstand = "Wf76gGkBCYXyZRynmzUR";
	private static final String elasticsearchId_bostedsadresse = "BDWf76gBCYXyZRynmzUR";  //TODO: Fill in
	private static final String elasticsearchId_oppholdsadresse = "KdWf76gBCYXyZRynmzUR";  //TODO: Fill in

	private static final InformationType sivilstand_created = InformationType.builder()
			.informationTypeId(1L)
			.informationTypeName("Sivilstand")
			.description("En overordnet kategori som beskriver en persons relasjon til en annen person.")
			.informationCategory("PERSONALIA")
			.informationProducer("BRUKER")
			.informationSystem("AA_REG")
			.dateCreated("2019-01-12")
			.createdBy("Kari Nordmann")
			.dateLastUpdated(null)
			.updatedBy(null)
			.jsonString(null)
			.elasticsearchId(elasticsearchId_sivilstand)
			.elasticsearchStatus(TO_BE_CREATED.toString())
			.build();

	private static final InformationType bostedsadresse_updated = InformationType.builder()
			.informationTypeId(2L)
			.informationTypeName("Bostedsadresse")
			.description("En overordnet kategori som beskriver en persons relasjon til en annen person.")
			.informationCategory("FAMILIERELASJONER")
			.informationProducer("SKATTEETATEN")
			.informationSystem("TPS")
			.dateCreated("2019-01-12")
			.createdBy("Kari Nordmann")
			.dateLastUpdated("2019-04-16")
			.updatedBy("Ola Nordmann")
			.jsonString(null) //TODO: Fill in
			.elasticsearchId(elasticsearchId_bostedsadresse)
			.elasticsearchStatus(TO_BE_UPDATED.toString())
			.build();

	private static final InformationType oppholdsadresse_deleted = InformationType.builder()
			.informationTypeId(3L)
			.informationTypeName("Oppholdsadresse")
			.description("Angir hvor en bruker (BEGREP-20) oppholder seg når brukeren har en postadresse (BEGREP-658). Denne adressetypen brukes i fagsystemet Arena og erstatter de to adressetypene midlertidig og bolig")
			.informationCategory("KONTAKTOPPLYSNINGER")
			.informationProducer("SKATTEETATEN")
			.informationSystem("TPS")
			.dateCreated("2018-02-28")
			.createdBy("Kari Nordmann")
			.dateLastUpdated("2019-04-16")
			.updatedBy("Ola Nordmann")
			.jsonString(null) //TODO: Fill in
			.elasticsearchId(elasticsearchId_oppholdsadresse)
			.elasticsearchStatus(TO_BE_DELETED.toString())
			.build();


	public static String getElasticsearchId_sivilstand() {
		return elasticsearchId_sivilstand;
	}

	public static String getSivilstandJsonString() {
		return "{\n" +
				"    \"informationTypeName\": \"Sivilstand\",\n" +
				"    \"informationCategory\" : \"PERSONALIA\",\n" +
				"    \"informationProducer\" : \"SKATTEETATEN\", \n" +
				"    \"informationSystem\" : \"TPS\",\n" +
				"    \"description\" : \"En overordnet kategori som beskriver en persons relasjon til en annen person.\",\n" +
				"\t\"personalData\": true,\n" +
				"    \"createdBy\": \"Mårten Elmgren\"\n" +
				"}";
	}

	public static String getBostadsadresseJsonString() {
		return "{\n" +
				"    \"informationTypeName\": \"Bostedsadresse\",\n" +
				"    \"informationCategory\" : \"FAMILIERELASJONER\",\n" +
				"    \"informationProducer\" : \"BRUKER\", \n" +
				"    \"informationSystem\" : \"AA_REG\",\n" +
				"    \"description\" : \"En kortere forklaring for geografisk adresse.\",\n" +
				"    \"personalData\": true,\n" +
				"\t\"createdBy\": \"Mårten Elmgren\"\n" +
				"}";
	}

	public static String getOppholdsadresseJsonString() {
		return "{\n" +
				"    \"informationTypeName\": \"Oppholdsadresse\",\n" +
				"    \"informationCategory\" : \"KONTAKTOPPLYSNINGER\",\n" +
				"    \"informationProducer\" : \"SKATTEETATEN\", \n" +
				"    \"informationSystem\" : \"TPS\",\n" +
				"    \"description\" : \"Angir hvor en bruker (BEGREP-20) oppholder seg når brukeren har en postadresse (BEGREP-658). Denne adressetypen brukes i fagsystemet Arena og erstatter de to adressetypene midlertidig og bolig\",\n" +
				"    \"personalData\": true,\n" +
				"\t\"createdBy\": \"Mårten Elmgren\"\n" +
				"}";
	}

//	public static String getInntektJsonString() {
//		return "{\n" +
//				"      \"name\": \"Inntekt\",\n" +
//				"      \"description\": \"Inntekt rapportert inn via a-ordningen, ferdiglignet PGI fra Opptjeningsregisteret eller inntekter oppgitt av bruker som beskriver brukers inntekt forut for uttak av ytelsen.\",\n" +
//				"      \"category\": \"INNTEKT_TRYGDE_OG_PENSJONSYTELSER\",\n" +
//				"      \"sensitivity\": \"5\",\n" +
//				"      \"ownership\": \"Ytelsesavdelingen\",\n" +
//				"      \"sourceOfRecord\": \"Folkeregisteret\",\n" +
//				"      \"qualityOfData\": \"God\",\n" +
//				"      \"personalData\": true\n" +
//				"    }";
//	}
//
//	public static String getFaltyJsonString() {
//		return "{\n" +
//				"      \"description\": \"Inntekt rapportert inn via a-ordningen, ferdiglignet PGI fra Opptjeningsregisteret eller inntekter oppgitt av bruker som beskriver brukers inntekt forut for uttak av ytelsen.\",\n" +
//				"      \"category\": \"INNTEKT_TRYGDE_OG_PENSJONSYTELSER\",\n" +
//				"      \"sensitivity\": \"5\",\n" +
//				"      \"ownership\": \"Ytelsesavdelingen\",\n" +
//				"      \"sourceOfRecord\": \"Folkeregisteret\",\n" +
//				"      \"qualityOfData\": \"God\",\n" +
//				"      \"personalData\": true\n" +
//				"    }";
//	}
//
//	public static String getUpdateJsonString() {
//		return "{\n" +
//				"\t\"name\" : \"UpdateTest\",\n" +
//				"\t\"description\" : \"Test at update funker\",\n" +
//				"\t\"qualityOfData\": \"Superb\"\n" +
//				"}";
//	}
//
//	public static Record getSivilstandRecord() {
//		return sivilstand;
//	}
//
//	public static Record getInntektRecord() {
//		return inntekt;
//	}
//
//	public static RecordResponse getInntektResponse() {
//		Record record = getInntektRecord();
//		return RecordResponse.builder()
//				.id(record.getId())
//				.status(String.format("Created a new record with id=%s", record.getId()))
//				.build();
//	}
//
//	public static RecordResponse getUpdatedResponse() {
//		Record record = getInntektRecord();
//		return RecordResponse.builder()
//				.id(record.getId())
//				.status(String.format("Updated record with id=%s", record.getId()))
//				.build();
//	}
//
//	public static RecordResponse getDeleteResponse() {
//		String id = getElasticsearchId_sivilstand();
//		return RecordResponse.builder()
//				.id(id)
//				.status(String.format("Deleted record with id=%s", id))
//				.build();
//	}
//
//	public static List<Record> getAllRecords() {
//		List<Record> records = new ArrayList<>();
//		records.add(getSivilstandRecord());
//		records.add(getInntektRecord());
//		return records;
//	}
}

