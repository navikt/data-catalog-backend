-- PRODUCER
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'SKATTEETATEN', 'Skatteetaten', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'BRUKER', 'Informasjon oppgitt av bruker', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'UTLENDINGSDIREKTORATET', 'Utlendingsdirektoratet', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'UTENLANDS_TRYGDEMYNDIGHET', 'Utenlands trygdemyndighet', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'ARBEIDSGIVER', 'Arbeidsgiver', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('PRODUCER', 'REVISOR', 'Revisor', 'Flyway', now());

-- CATEGORY
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'PERSONALIA', 'Personalia', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'KONTAKTOPPLYSNINGER', 'Adresse og kontaktopplysninger', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'FAMILIERELASJONER', 'Familierelasjoner', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'FULLMEKTIG', 'Verge og fullmektig', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'FOLKETRYGD', 'Opphold og medlemskap i folketrygden', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'ARBEIDSFORHOLD', 'Arbeidsforhold', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'UTDANNING', 'Utdanning', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'CV', 'CV', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'SKOLE_BARNEHAGE', 'Opplysninger om skole og barnehageplass', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'INNTEKT_YTELSER', 'Inntekt, trygde- og pensjonsytelser', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'SKATTEOPPLYSNINGER', 'Skatteopplysninger', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'OKONOMISKE_OPPLYSNINGER', 'Andre økonomiske opplysninger', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'HELSEOPPLYSNNGER', 'Helseopplysninger', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'INSTITUSJONSOPPHOLD', 'Institusjonsopphold', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'LOVOVERTREDELSER', 'Straffedommer og lovovertredelser', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'BIOMETRISKE_KJENNETEGN', 'Biometriske kjennetegn som kan identifisere enkeltperson', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'MEDIER_KJENNETEGN', 'Personportrett, video- og lydopptak som kan identifisere enkeltperson', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'MOTORVOGNOPPLYSNINGER', 'Motorvognopplysninger', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'TRO_ETNISITET', 'Opplysninger om trossamfunn og etnisitet', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'VERNEPLIKT', 'Opplysninger om verneplikt', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('CATEGORY', 'REISEVEI', 'Opplysninger om reisevei', 'Flyway', now());

-- SYSTEM
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'TPS', 'Tjenestebasert PersondataSystem', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'INST', 'Institusjonsopphold', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'POPP', 'Pensjonfaglig opptjeningsregiste', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'TP', 'Tjenestepensjon', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'AA_REG', 'Arbeidsgiver / Arbeidstaker register', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'OPPDRAG', 'Oppdragssystemet', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'PESYS', 'Pensjonssystem', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'ARENA', 'Arbeidsrelatert saksbehandlingsystem', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'INFOTRYGD', 'System for saksbehandling av ytelser og trygd', 'Flyway', now());
INSERT INTO BACKEND_SCHEMA.CODELIST(LIST_NAME, CODE, DESCRIPTION, CREATED_BY, CREATED_DATE)
VALUES ('SYSTEM', 'BISYS', 'Bidragsløsningen', 'Flyway', now());

