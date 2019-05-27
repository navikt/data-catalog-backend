CREATE SCHEMA IF NOT EXISTS BACKEND_SCHEMA;

CREATE SEQUENCE IF NOT EXISTS BACKEND_SCHEMA.SEQ_INFORMATION_TYPE;
CREATE SEQUENCE IF NOT EXISTS BACKEND_SCHEMA.SEQ_CODELIST;


CREATE TABLE IF NOT EXISTS BACKEND_SCHEMA.CODELIST
(
    LIST_NAME          VARCHAR(100) NOT NULL,
    CODE               VARCHAR(100) NOT NULL,
    DESCRIPTION        VARCHAR(500) NOT NULL,
    CREATED_BY         VARCHAR(200) NOT NULL,
    CREATED_DATE       TIMESTAMP    NOT NULL,
    LAST_MODIFIED_BY   VARCHAR(200),
    LAST_MODIFIED_DATE TIMESTAMP,
    UNIQUE (LIST_NAME, CODE)
);

CREATE TABLE IF NOT EXISTS BACKEND_SCHEMA.INFORMATION_TYPE
(
    INFORMATION_TYPE_ID  INTEGER DEFAULT nextval('BACKEND_SCHEMA.SEQ_INFORMATION_TYPE') PRIMARY KEY,
    NAME                 VARCHAR(100) UNIQUE NOT NULL,
    DESCRIPTION          TEXT                NOT NULL,
    CATEGORY_CODE        TEXT                NOT NULL,
    PRODUCER_CODE        TEXT                NOT NULL,
    SYSTEM_CODE          TEXT                NOT NULL,
    PERSONAL_DATA        BOOLEAN             NOT NULL,
    ELASTICSEARCH_ID     TEXT,
    ELASTICSEARCH_STATUS TEXT                NOT NULL,
    CREATED_BY           VARCHAR(200)        NOT NULL,
    CREATED_DATE         TIMESTAMP           NOT NULL,
    LAST_MODIFIED_BY     VARCHAR(200),
    LAST_MODIFIED_DATE   TIMESTAMP
);
